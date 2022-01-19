#include <DHT.h>
#include <DHT_U.h>

#define DHTPIN_1 8       // Digital pin connected to the white DHT sensor
#define DHTPIN_2 8       // Digital pin connected to the white DHT sensor
#define DHTTYPE DHT22     // DHT 22 - AM2302

uint32_t delayMS;

DHT_Unified dht22_1(DHTPIN_1, DHTTYPE); // Declarer le capteur
DHT_Unified dht22_2(DHTPIN_2, DHTTYPE); // Declarer le capteur

void setup() {
  
    Serial.begin(9600);
    // Initialize device.
    dht22_1.begin();
    dht22_2.begin();
    Serial.println(F("DHTxx Unified Sensor Example"));
    // Print temperature sensor details.
    sensor_t sensor_1;
    sensor_t sensor_2;
    
    dht22_1.temperature().getSensor(&sensor_1);
    dht22_2.temperature().getSensor(&sensor_2);
    dht22_1.humidity().getSensor(&sensor_1);
    dht22_2.humidity().getSensor(&sensor_2);
    
    // Set delay between sensor "readings
    delayMS = sensor_1.min_delay / 1000;
    delayMS = sensor_2.min_delay / 1000;
}


void loop() {
  
  // Delay between measurements.
  delay(delayMS);
  
  // Get temperature event and print its value.
  sensors_event_t event_1;
  sensors_event_t event_2;
  dht22_1.temperature().getEvent(&event_1);
  
  if (isnan(event_1.temperature)) {
    Serial.println(F("Error reading temperature dht22_1!"));
  }
  else {
    Serial.print(F("Temperature dht22_1 : "));
    Serial.print(event_1.temperature);
    Serial.println(F("°C"));
  }
  dht22_2.temperature().getEvent(&event_2);
  if (isnan(event_2.temperature)) {
    Serial.println(F("Error reading temperature dht22_2!"));
  }
  else {
    Serial.print(F("Temperature dht22_2 : "));
    Serial.print(event_2.temperature);
    Serial.println(F("°C"));
  }
  // Get humidity event and print its value.
  dht22_1.humidity().getEvent(&event_1);
  if (isnan(event_1.relative_humidity)) {
    Serial.println(F("Error reading humidity!"));
  }
  else {
    Serial.print(F("Humidity dht22_1 : "));
    Serial.print(event_1.relative_humidity);
    Serial.println(F("%"));
  }

  dht22_2.humidity().getEvent(&event_2);
  if (isnan(event_2.relative_humidity)) {
    Serial.println(F("Error reading humidity!"));
  }
  else {
    Serial.print(F("Humidity dht22_2 : "));
    Serial.print(event_2.relative_humidity);
    Serial.println(F("%"));
  }
}
