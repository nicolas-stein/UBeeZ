#include "UBeeZ_Temperature_humidity_sensor.h"
#include <DHT.h>
#include <MaximWire.h>
#include "rtos.h"

MaximWire::Bus temperature_bus(PIN_TEMPERATURE_BUS);
MaximWire::DS18B20 temperature_device_1(ADDR_TEMPERATURE_1);
MaximWire::DS18B20 temperature_device_2(ADDR_TEMPERATURE_2);

DHT dht22_1(DHT22_1_PIN, DHT11);
DHT dht22_2(DHT22_2_PIN, DHT22);

float temperatures[4] = {NAN, NAN, NAN, NAN};
float humidites[2] = {NAN, NAN};

void setupTemperatureHumidity(){
    dht22_1.begin();
    dht22_2.begin();
}

void updateTemperatureHumidity(){
  temperature_device_1.Update(temperature_bus);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(100));
  temperatures[1] = temperature_device_1.GetTemperature<float>(temperature_bus);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(100));
  temperature_device_2.Update(temperature_bus);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(100));
  temperatures[2] = temperature_device_2.GetTemperature<float>(temperature_bus);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(100));

  temperatures[0] = dht22_1.readTemperature();
  temperatures[3] = dht22_2.readTemperature();

  humidites[0] = dht22_1.readHumidity();
  humidites[1] = dht22_2.readHumidity();
}

float getTemperatureSensor(int index){  //Retourne température en °C
  if(index>=0 && index <4){
    return temperatures[index];
  }
  return NAN;
}

float getHumiditeSensor(int index){ //Retourne humidité entre 0 et 100
  if(index>=0 && index <2){
    return humidites[index];
  }
  return NAN;
}
