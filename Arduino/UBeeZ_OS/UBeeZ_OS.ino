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
#include "UBeeZ_GPS.h"

#define DISABLE_SIGFOX false
#define DISABLE_GPS false

using namespace std;

std::chrono::time_point<std::chrono::system_clock> last_gps_sync = std::chrono::system_clock::now()-std::chrono::seconds(getPrefs()->gpsSyncDelay);

void setup() {
  // put your setup code here, to run once:
  set_time(0);
  SerialUSB.begin(9600);
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
  if(!DISABLE_GPS){
    setupGPS();
  }
  
  setupBLE();
  if(BLE_ALWAYS_ACTIVE){
    startBLE();
  }
}

void loop() {
  // put your main code here, to run repeatedly:
  if(!DISABLE_SIGFOX){
    sendPayloadCapteurs();
  }
  if(!DISABLE_GPS && std::chrono::system_clock::now() >= last_gps_sync + std::chrono::seconds(getPrefs()->gpsSyncDelay)){
      if(get_GPS_location()){
        sendPayloadGPS();
      }
      last_gps_sync = std::chrono::system_clock::now();
    }
    rtos::ThisThread::sleep_for(std::chrono::seconds(getPrefs()->sigfoxSyncDelay)); 
}
