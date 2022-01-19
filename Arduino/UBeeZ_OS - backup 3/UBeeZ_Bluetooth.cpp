#include "UBeeZ_Bluetooth.h"
#include <mbed.h>
#include "UBeeZ_LED.h"
#include "UBeeZ_Battery_weight_light.h"
#include "UBeeZ_Temperature_humidity_sensor.h"
#include "UBeeZ_Prefs.h"

int taskBLEPoll = 0;
int taskBLEDisableUnused = 0;
int taskBLEDisableConnected = 0;
int taskBLEUpdateValues = 0;
bool stopBLEPollThread = false;
bool stopBLEUpdateValuesThread = false;
bool BLEEnabling = false;

events::EventQueue *BLEEventQueue;
rtos::Thread BLEEventThread;

BLEService BLEServiceUBeeZ("181A"); // create service
BLEService BLEServiceUBeeZConfig("180A"); // create service

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

//Bluetooth luminosite
BLECharacteristic BLECharacteristicLuminosite("00002AFB-0000-1000-8000-00805F9B34FB" , BLERead | BLENotify, 3);
uint8_t descriptorLuminosite[7] = {0x7, (uint8_t)(int8_t)-2, 0x31, 0x27, 0x01, 0x0C, 0x01};
BLEDescriptor BLEDescriptorLuminosite("2904", descriptorLuminosite, 7);

//Bluetooth syncDelay
BLEUnsignedShortCharacteristic BLECharacteristicSyncDelay("00002A21-0000-1000-8000-00805F9B34FB" , BLERead | BLEWrite);
uint8_t descriptorSyncDelay[7] = {0x6, 0x00, 0x03, 0x27, 0x1, 0x00, 0x00};
BLEDescriptor BLEDescriptorSyncDelay("2904", descriptorSyncDelay, 7);

//Bluetooth localisation
BLECharacteristic BLECharacteristicLocalisation("00002A67-0000-1000-8000-00805F9B34FB", BLERead | BLEWrite, 12);
uint8_t descriptorLocalisation[4][7] = {{0x1B, 0x00, 0x00, 0x27, 0x01, 0x00, 0x00}, {0x10, (uint8_t)(int8_t)-7, 0x63, 0x27, 0x01, 0x00, 0x00},
{0x10, (uint8_t)(int8_t)-7, 0x63, 0x27, 0x01, 0x00, 0x00}, {0x06, (uint8_t)(int8_t)-2, 0x63, 0x27, 0x01, 0x00, 0x00}};
BLEDescriptor BLEDescriptorLocalisation[4] = {BLEDescriptor("2904", descriptorLocalisation[0], 7), BLEDescriptor("2904", descriptorLocalisation[1], 7),
BLEDescriptor("2904", descriptorLocalisation[2], 7), BLEDescriptor("2904", descriptorLocalisation[3], 7)};

void blePeripheralConnectHandler(BLEDevice central) {
  if(BLE_DEBUG){
    SerialUSB.println("blePeripheralConnectHandler");
  }
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
  if(BLE_DEBUG){
    SerialUSB.println("blePeripheralDisconnectHandler");
  }
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
  if(BLE_DEBUG){
    SerialUSB.println("setupBLE");
  }
  pinMode(BLE_BUTTON_PIN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(BLE_BUTTON_PIN), startBLE, FALLING);
  
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

  BLECharacteristicLuminosite.addDescriptor(BLEDescriptorLuminosite);
  BLEServiceUBeeZ.addCharacteristic(BLECharacteristicLuminosite);

  BLECharacteristicSyncDelay.addDescriptor(BLEDescriptorSyncDelay);
  BLECharacteristicSyncDelay.setEventHandler(BLEWritten, syncDelayCharacteristicWritten);
  BLEServiceUBeeZConfig.addCharacteristic(BLECharacteristicSyncDelay);

  for(int i=0;i<4;i++){
    BLECharacteristicLocalisation.addDescriptor(BLEDescriptorLocalisation[i]);
  }
  BLECharacteristicLocalisation.setEventHandler(BLEWritten, localisationCharacteristicWritten);
  BLEServiceUBeeZConfig.addCharacteristic(BLECharacteristicLocalisation);

  BLEEventQueue = new events::EventQueue(5 * EVENTS_EVENT_SIZE);
  BLEEventThread.start(callback(BLEEventQueue, &events::EventQueue::dispatch_forever));
}

void enableBLE(){
  if(taskBLEPoll!=0){
    BLE.disconnect();
    return;
  }
  if(BLE_DEBUG){
    SerialUSB.println("enableBLE");
  }
  taskBLEPoll = BLEEventQueue->call_in(BLE_POLL_INTERVAL, (void(*)(void))pollBLE);
  taskBLEUpdateValues = BLEEventQueue->call_in(BLE_UPDATE_VALUES_INTERVAL, (void(*)(void))updateBLEValues);
  if(!BLE_ALWAYS_ACTIVE){
    taskBLEDisableUnused = BLEEventQueue->call_in(BLE_UNUSED_TIMEOUT, (void(*)(void))disableBLE);
  }
  
  if(!BLE.begin()){
    int delay_ms = 500;
    led_blink_B(delay_ms);
    led_blink_R(delay_ms);
    disableBLE();
    return;
  }
  else{
    led_blink_B(1000);
    led_stop_R();
  }

  BLE.setLocalName("UBeeZ");
  BLE.setDeviceName("UBeeZ");
  BLE.setAppearance(0x0552);

  BLE.setAdvertisedService(BLEServiceUBeeZ);
  BLE.addService(BLEServiceUBeeZ);
  BLE.addService(BLEServiceUBeeZConfig);

  BLE.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  BLE.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);
  BLE.advertise();
  if(BLE_DEBUG){
    SerialUSB.println("enabled BLE");
  }
}

void disableBLE(){
  if(BLE_DEBUG){
    SerialUSB.println("disableBLE");
  }
  led_stop_B();
  led_stop_R();
  digitalWrite(LEDB, HIGH);
  digitalWrite(LEDR, HIGH);
  if(taskBLEPoll!=0){
    stopBLEPollThread = true;
  }
  if(taskBLEUpdateValues!=0){
    stopBLEUpdateValuesThread = true;
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
  /*if(BLE_DEBUG){
    SerialUSB.println("pollBLE");
  }*/
  if(stopBLEPollThread){
    taskBLEPoll = 0;
    stopBLEPollThread = false;
  }
  else{
    BLE.poll(200);
    taskBLEPoll = BLEEventQueue->call_in(BLE_POLL_INTERVAL, (void(*)(void))pollBLE);
  }
}

void startBLE(){
  BLEEventQueue->call((void(*)(void))enableBLE);
}

void updateBLEValues(){
  if(BLE_DEBUG){
    SerialUSB.println("updateBLEValues");
  }
  if(stopBLEUpdateValuesThread){
    taskBLEUpdateValues = 0;
    stopBLEUpdateValuesThread = false;
    return;
  }
  updateTemperatureHumidity();
  for(int i=0;i<4;i++){
    float temperature = getTemperatureSensor(i);
    if(isnan(temperature)){
      BLECharacteristicTemperature[i].writeValue(82.3*100);//Temperature reading error
    }
    else{
      BLECharacteristicTemperature[i].writeValue(temperature*100);
    }
  }

  for(int i=0;i<2;i++){
    float humidite = getHumiditeSensor(i);
    if(isnan(humidite)){
      BLECharacteristicHumidite[i].writeValue(105*100);//Humidite reading error
    }
    else{
      BLECharacteristicHumidite[i].writeValue(humidite*100);
    }
  }

  if(updateWeight()){
    BLECharacteristicPoid.writeValue(getWeight()*200);
  }
  else{
    BLECharacteristicPoid.writeValue(102.3*200);
  }
  
  BLECharacteristicBatterie.writeValue(getBatteryPercentage());

  float light = getLight();
  
  if(light<=0){//Erreur déconnecté
    light=1023;
  }
  else if(light>1022){
    light=1022;
  }
  light = 11.019*exp(0.0069*light);
  
  BLECharacteristicLuminosite.writeValue((uint32_t)(light*100));

  BLECharacteristicSyncDelay.writeValue(getPrefs()->sigfoxSyncDelay);

  uint8_t localisation[12];
  for(int i=0;i<12;i++){
    localisation[i] = 0;
  }
  localisation[0] = 0x14;
  localisation[1] = 0x10;
  int latitude = 123.4567899*10000000;//getPrefs()->gpsLatitude;
  int longitude = -123.4567899*10000000;//getPrefs()->gpsLongitude;
  unsigned short heading = 69.69*100;//getPrefs()->gpsHeading;
  localisation[2] = latitude;
  localisation[3] = latitude >> 8;
  localisation[4] = latitude >> 16;
  localisation[5] = latitude >> 24;
  localisation[6] = longitude;
  localisation[7] = longitude >> 8;
  localisation[8] = longitude >> 16;
  localisation[9] = longitude >> 24;
  localisation[10] = heading;
  localisation[11] = heading >> 8;
  

  BLECharacteristicLocalisation.writeValue(localisation, 12, true);

  taskBLEUpdateValues = BLEEventQueue->call_in(BLE_UPDATE_VALUES_INTERVAL, (void(*)(void))updateBLEValues);
}

void syncDelayCharacteristicWritten(BLEDevice central, BLECharacteristic characteristic) {
  unsigned short newDelay = (characteristic.value()[1] << 8 | characteristic.value()[0]);
  if(BLE_DEBUG){
    SerialUSB.print("syncDelayCharacteristicWritten new value : ");
    SerialUSB.println(newDelay);
  }
  
  if(newDelay >= 600 || DEBUG_PREFS){
    getPrefs()->sigfoxSyncDelay = newDelay;
  }
  else{
    BLECharacteristicSyncDelay.writeValue(getPrefs()->sigfoxSyncDelay);
  }
  writePrefs();
}

void localisationCharacteristicWritten(BLEDevice central, BLECharacteristic characteristic) {
  double newLatitude = ((double)(characteristic.value()[2] | characteristic.value()[3] << 8 | characteristic.value()[4] << 16 | characteristic.value()[5] << 24)) / 10000000;
  double newLongitude = ((double)(characteristic.value()[6] | characteristic.value()[7] << 8 | characteristic.value()[8] << 16 | characteristic.value()[9] << 24)) / 10000000;
  float newHeading = ((float)(characteristic.value()[10] | characteristic.value()[11] << 8)) / 100;
  
  if(BLE_DEBUG){
    SerialUSB.println("localisationCharacteristicWritten new value : ");
    SerialUSB.print("Latitude : ");
    SerialUSB.println(newLatitude, 7);
    SerialUSB.print("Longitude : ");
    SerialUSB.println(newLongitude, 7);
    SerialUSB.print("Heading : ");
    SerialUSB.println(newHeading, 2);
  }  
}
