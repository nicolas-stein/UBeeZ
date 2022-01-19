/*
 * Bibliothèques utilisées :
 * - ArduinoBLE by Arduino
 * - Adafruit Unified Sensor by Adafruit
 * - DHT sensor library by Adafruit
 * - HX711 Arduino library by Bogdan Necula
 * - MaximWire by xeno
 * - NanoBLEFlashPrefs by Dirk
 */
 
#include "UBeeZ_LED.h"
#include "UBeez_Bluetooth.h"
#include "UBeeZ_Temperature_humidity_sensor.h"
#include "UBeeZ_Sigfox.h"
#include "UBeeZ_Battery_weight_light.h"
#include "UBeeZ_Prefs.h"

#define DISABLE_SIGFOX true

using namespace std;

void setup() {
  // put your setup code here, to run once:
  SerialUSB.begin(9600);
  delay(1000);
  digitalWrite(PIN_ENABLE_I2C_PULLUP, LOW);
  digitalWrite(PIN_ENABLE_SENSORS_3V3, LOW);

  setupLedRGB();
  setupPrefs();
  if(!DISABLE_SIGFOX){
    setupSigfox();
  }
  setupTemperatureHumidity();
  setupBatteryLight();
  setupWeight();
  setupBLE();
  if(BLE_ALWAYS_ACTIVE){
    startBLE();
  }
}

void loop() {
  // put your main code here, to run repeatedly:
  if(!DISABLE_SIGFOX){
    sendPayload();
  }
  rtos::ThisThread::sleep_for(std::chrono::seconds(getPrefs()->sigfoxSyncDelay));
}
