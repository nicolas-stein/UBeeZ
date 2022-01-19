#include "UBeeZ_Bluetooth.h"
#include "UBeeZ_LED.h"

mbed::Timeout BLETimeout;
rtos::Thread *BLEPollThread = NULL;
bool isBLEPollRunning = false;
bool stopBLEPollThread = false;

BLEService BLEServiceUBeeZ("181A"); // create service

//Bluetooth temperature
BLEShortCharacteristic BLECharacteristicTemperature[] = {BLEShortCharacteristic("00002A6E-0000-1000-8000-00805F9B34FB", BLERead | BLENotify),
BLEShortCharacteristic("00002A6E-0000-1000-8000-00805F9B34FB", BLERead | BLENotify), BLEShortCharacteristic("00002A6E-0000-1000-8000-00805F9B34FB", BLERead | BLENotify),
BLEShortCharacteristic("00002A6E-0000-1000-8000-00805F9B34FB", BLERead | BLENotify)};
uint8_t descriptorTemperature[4][7] = {{0x0E, (uint8_t)(int8_t)-2, 0x2F, 0x27, 0x01, 0x01, 0x00}, {0x0E, (uint8_t)(int8_t)-2, 0x2F, 0x27, 0x01, 0x02, 0x00},
{0x0E, (uint8_t)(int8_t)-2, 0x2F, 0x27, 0x01, 0x03, 0x00}, {0x0E, (uint8_t)(int8_t)-2, 0x2F, 0x27, 0x01, 0x04, 0x00}};
BLEDescriptor BLEDescriptorTemperature[4] = {BLEDescriptor("2904", descriptorTemperature[0], 7), BLEDescriptor("2904", descriptorTemperature[1], 7),
BLEDescriptor("2904", descriptorTemperature[2], 7), BLEDescriptor("2904", descriptorTemperature[3], 7)};

//Bluetooth humidite
BLEUnsignedShortCharacteristic BLECharacteristicHumidite[] = {BLEUnsignedShortCharacteristic("00002A6F-0000-1000-8000-00805F9B34FB", BLERead | BLENotify),
BLEUnsignedShortCharacteristic("00002A6F-0000-1000-8000-00805F9B34FB", BLERead | BLENotify)};
uint8_t descriptorHumidite[2][7] = {{0x06, (uint8_t)(int8_t)-2, 0xAD, 0x27, 0x01, 0x0B, 0x01}, {0x06, (uint8_t)(int8_t)-2, 0xAD, 0x27, 0x01, 0x0C, 0x01}};
BLEDescriptor BLEDescriptorHumidite[2] = {BLEDescriptor("2904", descriptorHumidite[0], 7), BLEDescriptor("2904", descriptorHumidite[1], 7)};

//Bluetooth poid
BLEUnsignedShortCharacteristic BLECharacteristicPoid("00002A98-0000-1000-8000-00805F9B34FB", BLERead | BLENotify);
uint8_t descriptorPoid[7] = {0x06, (uint8_t)(int8_t)-2, 0x02, 0x27, 0x01, 0x00, 0x00};
BLEDescriptor BLEDescriptorPoid("2904", descriptorPoid, 7);

//Bluetooth batterie
BLEByteCharacteristic BLECharacteristicBatterie("00002A19-0000-1000-8000-00805F9B34FB", BLERead | BLENotify);
uint8_t descriptorBatterie[7] = {0x04, 0x00, 0xAD, 0x27, 0x01, 0x00, 0x00};
BLEDescriptor BLEDescriptorBatterie("2904", descriptorBatterie, 7);

void blePeripheralConnectHandler(BLEDevice central) {
  led_stop_B();
  digitalWrite(LEDB, LOW);
  BLETimeout.detach();
  BLETimeout.attach(&disableBLE, std::chrono::seconds(BLE_CONNECTED_TIMEOUT_SEC));
}

void blePeripheralDisconnectHandler(BLEDevice central) {
  digitalWrite(LEDB, HIGH);
  led_blink_B(1000);
  BLETimeout.detach();
  BLETimeout.attach(&disableBLE, std::chrono::seconds(BLE_UNUSED_TIMEOUT_SEC));
}

void setupBLE(){
  for(int i=0;i<4;i++){
    BLECharacteristicTemperature[i].addDescriptor(BLEDescriptorTemperature[i]);
    BLEServiceUBeeZ.addCharacteristic(BLECharacteristicTemperature[i]);
  }
  for(int i=0;i<2;i++){
    BLECharacteristicHumidite[i].addDescriptor(BLEDescriptorHumidite[i]);
    BLEServiceUBeeZ.addCharacteristic(BLECharacteristicHumidite[i]);
  }
  BLECharacteristicPoid.addDescriptor(BLEDescriptorPoid);
  BLEServiceUBeeZ.addCharacteristic(BLECharacteristicPoid);

  BLECharacteristicBatterie.addDescriptor(BLEDescriptorBatterie);
  BLEServiceUBeeZ.addCharacteristic(BLECharacteristicBatterie);
}

void enableBLE(){
  SerialUSB.write("enableBLE\r\n");
  if(isBLEPollRunning){
    return;
  }
  BLETimeout.attach(&disableBLE, std::chrono::seconds(BLE_UNUSED_TIMEOUT_SEC));
  if(!BLE.begin()){
    int delay_ms = 500;
    led_blink_B(delay_ms);
    led_blink_R(delay_ms);
    return;
  }
  else{
    led_blink_B(1000);
  }

  BLE.setLocalName("UBeeZ");
  
  BLECharacteristicTemperature[0].writeValue(10.90*100);
  BLECharacteristicTemperature[1].writeValue(20.91*100);
  BLECharacteristicTemperature[2].writeValue(30.92*100);
  BLECharacteristicTemperature[3].writeValue(40.93*100);

  BLECharacteristicHumidite[0].writeValue(50.94*100);
  BLECharacteristicHumidite[1].writeValue(60.95*100);

  BLECharacteristicPoid.writeValue(99.99*200);
  BLECharacteristicBatterie.writeValue((3.678913-3.20)*100);

  BLE.setAdvertisedService(BLEServiceUBeeZ);
  BLE.addService(BLEServiceUBeeZ);

  BLE.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  BLE.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  BLE.advertise();
  BLEPollThread = new rtos::Thread();
  BLEPollThread->start(mbed::callback(pollBLE));
}

void disableBLE(){
  led_stop_B();
  led_stop_R();
  if(BLEPollThread!=NULL){
    stopBLEPollThread = true;
  }
}

void pollBLE(){
  isBLEPollRunning = true;
  while(!stopBLEPollThread){
    BLE.poll(1000);
  }
  stopBLEPollThread = false;
  isBLEPollRunning = false;
  BLE.end();
}