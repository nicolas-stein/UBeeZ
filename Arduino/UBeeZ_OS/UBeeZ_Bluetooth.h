#ifndef UBEEZ_BLUETOOTH
#define UBEEZ_BLUETOOTH

#include <ArduinoBLE.h>

#define BLE_UNUSED_TIMEOUT std::chrono::seconds(30)
#define BLE_CONNECTED_TIMEOUT std::chrono::seconds(60*15)
#define BLE_POLL_INTERVAL std::chrono::milliseconds(10)
#define BLE_UPDATE_VALUES_INTERVAL std::chrono::seconds(1)
#define BLE_BUTTON_PIN 2  //Digital pin 2 (D2)
#define BLE_ALWAYS_ACTIVE false
#define BLE_DEBUG false

void blePeripheralConnectHandler(BLEDevice central);
void blePeripheralDisconnectHandler(BLEDevice central);
void setupBLE();
void enableBLE();
void disableBLE();
void pollBLE();
void startBLE();
void updateBLEValues();
void syncDelayCharacteristicWritten(BLEDevice central, BLECharacteristic characteristic);
void localisationCharacteristicWritten(BLEDevice central, BLECharacteristic characteristic);

#endif
