#include "UBeeZ_Sigfox.h"
#include "UBeeZ_Temperature_humidity_sensor.h"
#include "UBeeZ_Battery.h"
#include <Serial.h>

void sendPayload(){
  SerialUSB.println("Sending Sigfox data");
  /* Payload :
   * Humidité : 6 bit
   * Température : 10 bit
   * Poid : 10 bit
   * Batterie : 8 bit
   */

  //SerialUSB.println("Valeurs converties :\n");

  //Range humidité : 0 - 100 ===>>> 0 - 64
  unsigned int convertedHumidite[2];
  for(int i=0;i<2;i++){
    convertedHumidite[i] =  std::round(getHumiditeSensor(i)*64/100);
    //SerialUSB.println("Humidite %d\t: %u\t%#03x\n", i, convertedHumidite[i], convertedHumidite[i]);
  }

  //Range température : -20.0 - 80.0 ===>>> 0 - 1000
  unsigned int convertedTemperature[4];
  for(int i=0;i<4;i++){
    convertedTemperature[i] = std::round((getTemperatureSensor(i)+20)*10);
    //SerialUSB.println("Temperature %d\t: %u\t%#05x\n", i, convertedTemperature[i], convertedTemperature[i]);
  }

  float rawPoid = 0;

  //Range poid : 0.0 - 100.0 ===>>> 0 - 1000
  unsigned int convertedPoid = std::round(rawPoid*10);
  //SerialUSB.println("Poid \t\t: %u\t%#05x\n", convertedPoid, convertedPoid);

  //Range batterie : 2.44 - 5 ===>>> 0 - 256
  unsigned int convertedBatterie = std::round((getBatteryVoltage()-2.44)*100);
  //SerialUSB.println("Batterie \t: %u\t%#04x\n\n", convertedBatterie, convertedBatterie);

  unsigned char payload[12];
  for(int i=0;i<12;i++){
    payload[i] = 0;
  }
  payload[0] = convertedHumidite[0] << 2 | convertedHumidite[1] >> 4;
  payload[1] = convertedHumidite[1] << 4 | convertedTemperature[0] >> 6;
  payload[2] = convertedTemperature[0] << 2 | convertedTemperature[1] >> 8;
  payload[3] = convertedTemperature[1] & 0x7F;
  payload[4] = convertedTemperature[2] >> 2;
  payload[5] = convertedTemperature[2] << 6 | convertedTemperature[3] >> 4;
  payload[6] = convertedTemperature[3] << 4 | convertedPoid >> 6;
  payload[7] = convertedPoid << 2 | convertedBatterie >> 6;
  payload[8] = convertedBatterie << 2;

  //SerialUSB.print("AT$SF=");
  Serial1.print("AT$SF=");
  char StrBuffer[3];
  for(int i=0;i<12;i++){
    sprintf(StrBuffer, "%02X", payload[i]);
    //SerialUSB.print(StrBuffer);
    Serial1.print(StrBuffer);
  }
  //SerialUSB.print("\r\n");
  Serial1.print("\r\n");
}
