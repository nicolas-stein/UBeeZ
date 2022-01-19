#ifndef UBEEZ_BLUETOOTH
#define UBEEZ_BLUETOOTH

#define BLE_UNUSED_TIMEOUT_SEC 10
#define BLE_CONNECTED_TIMEOUT_SEC 60*15
#define BLE_BUTTON_PIN 2  //Digital pin 2 (D2)

#include <mbed.h>
#include <ArduinoBLE.h>

void blePeripheralConnectHandler(BLEDevice central);
void blePeripheralDisconnectHandler(BLEDevice central);
void setupBLE();
void enableBLE();
void disableBLE();
void pollBLE();
void startBLE();

#endif