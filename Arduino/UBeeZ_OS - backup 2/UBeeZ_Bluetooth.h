#ifndef UBEEZ_BLUETOOTH
#define UBEEZ_BLUETOOTH

#define BLE_UNUSED_TIMEOUT std::chrono::seconds(10)
#define BLE_CONNECTED_TIMEOUT std::chrono::seconds(60*15)
#define BLE_POLL_INTERVAL std::chrono::milliseconds(50)
#define BLE_BUTTON_PIN 2  //Digital pin 2 (D2)
#define BLE_ALWAYS_ACTIVE false

#include <mbed.h>
#include <ArduinoBLE.h>
#include "UBeeZ_LED.h"
#include "UBeeZ_Temperature_humidity_sensor.h"
#include "UBeeZ_Battery.h"

void blePeripheralConnectHandler(BLEDevice central);
void blePeripheralDisconnectHandler(BLEDevice central);
void setupBLE();
void enableBLE();
void disableBLE();
void pollBLE();
void startBLE();
void updateBLEValues();

#endif
