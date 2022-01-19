#ifndef UBEEZ_TEMPERATURE_HUMIDITY_SENSOR
#define UBEEZ_TEMPERATURE_HUMIDITY_SENSOR

#define MAXIMWIRE_EXTERNAL_PULLUP

#define ADDR_TEMPERATURE_1 "280C8016A8013C93"
#define ADDR_TEMPERATURE_2 "2876BF56B5013C95"
#define PIN_TEMPERATURE_BUS 10

#define DHT22_1_PIN 8
#define DHT22_2_PIN 9


void setupTemperatureHumidity();
void updateTemperatureHumidity();

float getTemperatureSensor(int index);
float getHumiditeSensor(int index);

#endif
