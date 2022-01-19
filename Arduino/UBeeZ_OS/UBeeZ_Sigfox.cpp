#include "UBeeZ_Sigfox.h"
#include "UBeeZ_LED.h"
#include "UBeeZ_Temperature_humidity_sensor.h"
#include "UBeeZ_Battery_weight_light.h"
#include "UBeeZ_Prefs.h"
#include <Serial.h>
#include "mbed.h"
#include "rtos.h"

void setupSigfox(){
  pinMode(PIN_SIGFOX_RESET, OUTPUT);
  Serial1.begin(9600);
  digitalWrite(PIN_SIGFOX_RESET, HIGH);
}

void sendPayloadCapteurs(){

  //Resume Sigfox
  digitalWrite(PIN_SIGFOX_RESET, LOW);  
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  digitalWrite(PIN_SIGFOX_RESET, HIGH);
  
  led_blink_R(100); 
  while(!Serial1.available()){
    Serial1.write("AT\r\n");
    rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  }
  led_stop_R();
  digitalWrite(LEDR, HIGH);
  
  //SerialUSB.println("Sending Sigfox data");
  /* Payload :
   * Humidité : 6 bit
   * Température : 10 bit
   * Poid : 10 bit
   * Batterie : 8 bit
   */

  //SerialUSB.println("Valeurs converties :\n");

  updateTemperatureHumidity();
  //Range humidité : 0 - 100 ===>>> 0 - 64
  unsigned int convertedHumidite[2];
  float currentHumidity;
  for(int i=0;i<2;i++){
    currentHumidity = getHumiditeSensor(i);
    if(isnan(currentHumidity)){
      convertedHumidite[i] = 63;    //Humidity reading error
    }
    else if(currentHumidity < 0 || currentHumidity > 100){
      convertedHumidite[i] = 62;    //Humidity out of range
    }
    else{
      convertedHumidite[i] =  std::round(currentHumidity*60/100);
    }
    //SerialUSB.println("Humidite %d\t: %u\t%#03x\n", i, convertedHumidite[i], convertedHumidite[i]);
  }

  //Range température : -20.0 - 80.0 ===>>> 0 - 1000
  unsigned int convertedTemperature[4];
  //char strTemp[255];
  float currentTemp;
  for(int i=0;i<4;i++){
    currentTemp = getTemperatureSensor(i);
    if(isnan(currentTemp)){
      convertedTemperature[i] = 1023;   //Temperature reading error
    }
    else if(currentTemp>80 || currentTemp < -20){
      convertedTemperature[i] = 1022;   //Temperature out of range
    }
    else{
      convertedTemperature[i] = std::round((currentTemp+20)*10);
    }
    //sprintf(strTemp, "Temperature %d\t: %f=>%u\t%#05x\n", i, getTemperatureSensor(i), convertedTemperature[i], convertedTemperature[i]);
    //SerialUSB.print(strTemp);
  }

  //Range poid : 0.0 - 100.0 ===>>> 0 - 1000
  unsigned int convertedPoid;
  if(!updateWeight()){
    convertedPoid = 1023;   //Weight reading error
  }
  else if(getWeight() < 0){
    if(getWeight() > -0.5){
      convertedPoid = 0;
    }
    else{
      convertedPoid = 1022;
    }
  }
  else if(getWeight() > 100){
    convertedPoid = 1022;   //Weight out of range
  }
  else{
    convertedPoid = std::round(getWeight()*10);
  }
  //SerialUSB.println("Poid \t\t: %u\t%#05x\n", convertedPoid, convertedPoid);

  //Range batterie : 0 - 5 ===>>> 0 - 500
  unsigned int convertedBatterie;
  float batteryVoltage = getBatteryVoltage();
  if(batteryVoltage > 5 || batteryVoltage < 0){
    convertedBatterie = 511;
  }
  else{
    convertedBatterie = std::round(batteryVoltage*100);
  }
  /*char str[256];
  sprintf(str, "Batterie \t: %u\t%#04x\n\n", convertedBatterie, convertedBatterie);
  SerialUSB.print(str);*/

  //Range luminosite : 0-1000 ===>>> 0 - 1000
  unsigned int convertedLuminosite = getLight();
  if(convertedLuminosite > 1023){
    convertedLuminosite = 1023;
  }

  unsigned char payload[12];
  for(int i=0;i<12;i++){
    payload[i] = 0;
  }
  payload[0] = convertedHumidite[0] << 2 | convertedHumidite[1] >> 4;
  payload[1] = convertedHumidite[1] << 4 | convertedTemperature[0] >> 6;
  payload[2] = convertedTemperature[0] << 2 | convertedTemperature[1] >> 8;
  payload[3] = convertedTemperature[1] & 0xFF;
  payload[4] = convertedTemperature[2] >> 2;
  payload[5] = convertedTemperature[2] << 6 | convertedTemperature[3] >> 4;
  payload[6] = convertedTemperature[3] << 4 | convertedPoid >> 6;
  payload[7] = convertedPoid << 2 | convertedBatterie >> 7;
  payload[8] = convertedBatterie << 1 | convertedLuminosite >> 9;
  payload[9] = convertedLuminosite >> 1;
  payload[10] = convertedLuminosite << 7;

  //SerialUSB.print("AT$SF=");
  
  Serial1.print("AT$SF=");
  char strBuffer[3];
  for(int i=0;i<12;i++){
    sprintf(strBuffer, "%02X", payload[i]);
    //SerialUSB.print(StrBuffer);
    Serial1.print(strBuffer);
  }
  
  //SerialUSB.print("\r\n");
  Serial1.print("\r\n");
  rtos::ThisThread::sleep_for(std::chrono::seconds(10));
  Serial1.print("AT$P=2\r\n");
}

void sendPayloadGPS(){
  //Resume Sigfox
  digitalWrite(PIN_SIGFOX_RESET, LOW);  
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  digitalWrite(PIN_SIGFOX_RESET, HIGH);
  
  led_blink_R(100); 
  while(!Serial1.available()){
    Serial1.write("AT\r\n");
    rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  }
  led_stop_R();
  digitalWrite(LEDR, HIGH);

  unsigned int convertedLatitude = std::round((getPrefs()->gpsLatitude+90)*1000);
  //printf("Latitude \t: %u\t%#04x\n", convertedLatitude, convertedLatitude);

  unsigned int convertedLongitude = std::round((getPrefs()->gpsLongitude+180)*1000);
  //printf("Longitude \t: %u\t%#04x\n", convertedLongitude, convertedLongitude);

  unsigned int convertedOrientation = std::round(getPrefs()->gpsHeading);
  //printf("Orientation \t: %u\t%#04x\n\n", convertedOrientation, convertedOrientation);

  unsigned char payload[12];
  for(int i=0;i<12;i++){
    payload[i] = 0;
  }

  payload[0] = convertedLatitude >> 11;
  payload[1] = convertedLatitude >> 3;
  payload[2] = convertedLatitude << 5 | convertedLongitude >> 14;
  payload[3] = convertedLongitude >> 6;
  payload[4] = convertedLongitude << 2 | convertedOrientation >> 7;
  payload[5] = convertedOrientation << 1;

  payload[11] |= 0x1;

  //Sending data
  Serial1.print("AT$SF=");
  char strBuffer[3];
  for(int i=0;i<12;i++){
    sprintf(strBuffer, "%02X", payload[i]);
    //SerialUSB.print(StrBuffer);
    Serial1.print(strBuffer);
  }

  //SerialUSB.print("\r\n");
  Serial1.print("\r\n");
  rtos::ThisThread::sleep_for(std::chrono::seconds(10));
  Serial1.print("AT$P=2\r\n");
}
