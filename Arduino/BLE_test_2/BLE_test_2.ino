#include <ArduinoBLE.h>
#include <mbed.h>
#include <rtos.h>
#include <mbed_wait_api.h>

using namespace rtos;

//Capteurs
float temperatures[4] = {-7.838374, 18.120670, 16.622211, 22.796104};
float humidite[2] = {44.834743, 28.589741}; //humidite[0] : inside ; humidite[1] : outside
float poid = 76.799828;
float batterie = 3.678913;

//Led RGB
Thread *thread_LEDB_blink;
Thread *thread_LEDR_blink;

int pinLEDB = LEDB;
int pinLEDR = LEDR;

void led_blink_B(int *delay_ms){
  std::chrono::milliseconds delay_chrono(*delay_ms);
  while(true){
    digitalWrite(LEDB, LOW);
    ThisThread::sleep_for(delay_chrono);
    digitalWrite(LEDB, HIGH);
    ThisThread::sleep_for(delay_chrono);
  }
}

void led_blink_R(int *delay_ms){
  std::chrono::milliseconds delay_chrono(*delay_ms);
  while(true){
    digitalWrite(LEDR, LOW);
    ThisThread::sleep_for(delay_chrono);
    digitalWrite(LEDR, HIGH);
    ThisThread::sleep_for(delay_chrono);
  }
}

void led_blink_G(int *delay_ms){
  std::chrono::milliseconds delay_chrono(*delay_ms);
  while(true){
    digitalWrite(LEDG, LOW);
    ThisThread::sleep_for(delay_chrono);
    digitalWrite(LEDG, HIGH);
    ThisThread::sleep_for(delay_chrono);
  }
}

//Bluetooth
mbed::Timeout BLETimeout;

BLEService BLEServiceUBeeZ("181A"); // create service

//Bluetooth temperature
BLEShortCharacteristic BLECharacteristicTemperature[] = {BLEShortCharacteristic("00002A6E-0000-1000-8000-00805F9B34FB", BLERead | BLENotify),
BLEShortCharacteristic("00002A6E-0000-1000-8000-00805F9B34FB", BLERead | BLENotify), BLEShortCharacteristic("00002A6E-0000-1000-8000-00805F9B34FB", BLERead | BLENotify),
BLEShortCharacteristic("00002A6E-0000-1000-8000-00805F9B34FB", BLERead | BLENotify)};
uint8_t descriptorTemperature[4][7] = {{0x0E, (int8_t)-2, 0x2F, 0x27, 0x01, 0x01, 0x00}, {0x0E, (int8_t)-2, 0x2F, 0x27, 0x01, 0x02, 0x00},
{0x0E, (int8_t)-2, 0x2F, 0x27, 0x01, 0x03, 0x00}, {0x0E, (int8_t)-2, 0x2F, 0x27, 0x01, 0x04, 0x00}};
BLEDescriptor BLEDescriptorTemperature[4] = {BLEDescriptor("2904", descriptorTemperature[0], 7), BLEDescriptor("2904", descriptorTemperature[1], 7),
BLEDescriptor("2904", descriptorTemperature[2], 7), BLEDescriptor("2904", descriptorTemperature[3], 7)};

//Bluetooth humidite
BLEUnsignedShortCharacteristic BLECharacteristicHumidite[] = {BLEUnsignedShortCharacteristic("00002A6F-0000-1000-8000-00805F9B34FB", BLERead | BLENotify),
BLEUnsignedShortCharacteristic("00002A6F-0000-1000-8000-00805F9B34FB", BLERead | BLENotify)};
uint8_t descriptorHumidite[2][7] = {{0x06, (int8_t)-2, 0xAD, 0x27, 0x01, 0x0B, 0x01}, {0x06, (int8_t)-2, 0xAD, 0x27, 0x01, 0x0C, 0x01}};
BLEDescriptor BLEDescriptorHumidite[2] = {BLEDescriptor("2904", descriptorHumidite[0], 7), BLEDescriptor("2904", descriptorHumidite[1], 7)};

//Bluetooth poid
BLEUnsignedShortCharacteristic BLECharacteristicPoid("00002A98-0000-1000-8000-00805F9B34FB", BLERead | BLENotify);
uint8_t descriptorPoid[7] = {0x06, (int8_t)-2, 0x02, 0x27, 0x01, 0x00, 0x00};
BLEDescriptor BLEDescriptorPoid("2904", descriptorPoid, 7);

//Bluetooth batterie
BLEByteCharacteristic BLECharacteristicBatterie("00002A19-0000-1000-8000-00805F9B34FB", BLERead | BLENotify);
uint8_t descriptorBatterie[7] = {0x04, 0x00, 0xAD, 0x27, 0x01, 0x00, 0x00};
BLEDescriptor BLEDescriptorBatterie("2904", descriptorBatterie, 7);

void blePeripheralConnectHandler(BLEDevice central) {
  thread_LEDB_blink->terminate();
  digitalWrite(LEDB, LOW);
  BLETimeout.detach();
  BLETimeout.attach(&disableBLE, std::chrono::seconds(60*15));
}

void blePeripheralDisconnectHandler(BLEDevice central) {
  int delay_ms = 1000;
  thread_LEDB_blink = new Thread();
  thread_LEDB_blink->start(mbed::callback(led_blink_B, &delay_ms));
  BLETimeout.detach();
  BLETimeout.attach(&disableBLE, std::chrono::seconds(10));
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
  BLETimeout.attach(&disableBLE, std::chrono::seconds(10));
  if(!BLE.begin()){
    int delay_ms = 500;
    thread_LEDB_blink = new Thread();
    thread_LEDB_blink->start(mbed::callback(led_blink_B, &delay_ms));
    ThisThread::sleep_for(std::chrono::milliseconds(delay_ms));
    thread_LEDR_blink = new Thread();
    thread_LEDR_blink->start(mbed::callback(led_blink_R, &delay_ms));
    return;
  }
  else{
    int delay_ms = 1000;
    thread_LEDB_blink = new Thread();
    thread_LEDB_blink->start(mbed::callback(led_blink_B, &delay_ms));
  }

  BLE.setLocalName("UBeeZ");
  
  BLECharacteristicTemperature[0].writeValue(10.90*100);
  BLECharacteristicTemperature[1].writeValue(20.91*100);
  BLECharacteristicTemperature[2].writeValue(30.92*100);
  BLECharacteristicTemperature[3].writeValue(40.93*100);

  BLECharacteristicHumidite[0].writeValue(50.94*100);
  BLECharacteristicHumidite[1].writeValue(60.95*100);

  BLECharacteristicPoid.writeValue(99.99*200);
  BLECharacteristicBatterie.writeValue((batterie-3.20)*100);

  BLE.setAdvertisedService(BLEServiceUBeeZ);
  BLE.addService(BLEServiceUBeeZ);

  BLE.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  BLE.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  BLE.advertise();
}

void disableBLE(){
  thread_LEDB_blink->terminate();
  if(thread_LEDR_blink!=NULL){
    thread_LEDR_blink->terminate();
  }
  BLE.end();
  digitalWrite(LEDB, HIGH);
  digitalWrite(LEDR, HIGH);
}

void setup() {
  // put your setup code here, to run once:
  pinMode(LEDB, OUTPUT);
  pinMode(LEDR, OUTPUT);
  pinMode(LEDG, OUTPUT);
  digitalWrite(LEDR, HIGH);
  digitalWrite(LEDG, HIGH);
  digitalWrite(LEDB, HIGH);

  setupBLE();
  enableBLE();
}

void loop() {
  // put your main code here, to run repeatedly:
  BLE.poll();
}
