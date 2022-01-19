#include "UBeeZ_LED.h"
#include "UBeez_Bluetooth.h"
#include "UBeeZ_Temperature_humidity_sensor.h"
#include "UBeeZ_Sigfox.h"
#include "UBeeZ_Battery.h"

using namespace std;

void setup() {
  // put your setup code here, to run once:
  SerialUSB.begin(9600);
  Serial1.begin(9600);

  while(!Serial1.available()){
    Serial1.write("AT\r\n");
    delay(1000);
  }

  setupLedRGB();

  pinMode(BLE_BUTTON_PIN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(BLE_BUTTON_PIN), startBLE, FALLING);
  

  setupBLE();
  setupTemperatureHumidity();
  setupBattery();
  enableBLE();
}

void loop() {
  // put your main code here, to run repeatedly:
  updateTemperatureHumidity();
  updateBLEValues();
  sendPayload();
  rtos::ThisThread::sleep_for(std::chrono::minutes(10));  //A tester : consommation avec différents temps (1s, 5s, 30s, 10min)
}
