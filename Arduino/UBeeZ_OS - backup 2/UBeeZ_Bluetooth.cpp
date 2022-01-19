#include "UBeeZ_Bluetooth.h"

int taskBLEPoll = 0;
int taskBLEDisableUnused = 0;
int taskBLEDisableConnected = 0;
bool stopBLEPollThread = false;
bool BLEEnabling = false;

events::EventQueue *BLEEventQueue;
rtos::Thread BLEEventThread;

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
  if(taskBLEDisableUnused !=0){
    BLEEventQueue->cancel(taskBLEDisableUnused);
    taskBLEDisableUnused = 0;
  }
  if(taskBLEDisableConnected == 0 && !BLE_ALWAYS_ACTIVE){
    taskBLEDisableConnected = BLEEventQueue->call_in(BLE_CONNECTED_TIMEOUT, (void(*)(void))disableBLE);
  }
  BLE.stopAdvertise();
}

void blePeripheralDisconnectHandler(BLEDevice central) {
  digitalWrite(LEDB, HIGH);
  led_blink_B(1000);
  if(taskBLEDisableConnected !=0){
    BLEEventQueue->cancel(taskBLEDisableConnected);
    taskBLEDisableConnected = 0;
  }
  if(taskBLEDisableUnused == 0 && !BLE_ALWAYS_ACTIVE){
    taskBLEDisableUnused = BLEEventQueue->call_in(BLE_UNUSED_TIMEOUT, (void(*)(void))disableBLE);
  }
  BLE.advertise();
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

  BLEEventQueue = new events::EventQueue(5 * EVENTS_EVENT_SIZE);
  BLEEventThread.start(callback(BLEEventQueue, &events::EventQueue::dispatch_forever));
}

void enableBLE(){
  if(taskBLEPoll!=0){
    return;
  }
  taskBLEPoll = BLEEventQueue->call_in(BLE_POLL_INTERVAL, (void(*)(void))pollBLE);
  if(!BLE_ALWAYS_ACTIVE){
    taskBLEDisableUnused = BLEEventQueue->call_in(BLE_UNUSED_TIMEOUT, (void(*)(void))disableBLE);
  }
  
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
  
  updateBLEValues();

  BLE.setAdvertisedService(BLEServiceUBeeZ);
  BLE.addService(BLEServiceUBeeZ);

  BLE.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  BLE.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);
  BLE.advertise();
}

void disableBLE(){
  led_stop_B();
  led_stop_R();
  digitalWrite(LEDB, HIGH);
  digitalWrite(LEDR, HIGH);
  if(taskBLEPoll!=0){
    stopBLEPollThread = true;
  }
  if(taskBLEDisableUnused !=0){
    BLEEventQueue->cancel(taskBLEDisableUnused);
    taskBLEDisableUnused = 0;
  }
  if(taskBLEDisableConnected !=0){
    BLEEventQueue->cancel(taskBLEDisableConnected);
    taskBLEDisableConnected = 0;
  }
  BLE.end();
}

void pollBLE(){
  if(stopBLEPollThread){
    taskBLEPoll = 0;
    stopBLEPollThread = false;
  }
  else{
    BLE.poll();
    taskBLEPoll = BLEEventQueue->call_in(BLE_POLL_INTERVAL, (void(*)(void))pollBLE);
  }
}

void startBLE(){
  BLEEventQueue->call((void(*)(void))enableBLE);
}

void updateBLEValues(){
  for(int i=0;i<4;i++){
    float temperature = getTemperatureSensor(i);
    if(isnan(temperature)){
      BLECharacteristicTemperature[i].writeValue(82.4*100);//Temperature reading error
    }
    else{
      BLECharacteristicTemperature[i].writeValue(temperature*100);
    }
  }

  for(int i=0;i<2;i++){
    float humidite = getHumiditeSensor(i);
    if(isnan(humidite)){
      BLECharacteristicHumidite[i].writeValue(0);//Humidite reading error
    }
    else{
      BLECharacteristicHumidite[i].writeValue(humidite*100);
    }
  }

  BLECharacteristicPoid.writeValue(99.99*200);                            //TODO
  BLECharacteristicBatterie.writeValue(getBatteryPercentage()*100);
}
