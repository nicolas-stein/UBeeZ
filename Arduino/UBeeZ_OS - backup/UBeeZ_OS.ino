#include "UBeez_Bluetooth.h"

using namespace std;

//Capteurs
float temperatures[4] = {-7.838374, 18.120670, 16.622211, 22.796104};
float humidite[2] = {44.834743, 28.589741}; //humidite[0] : inside ; humidite[1] : outside
float poid = 76.799828;
float batterie = 3.678913;

bool shouldStartBLE = false;

void setup() {
  // put your setup code here, to run once:
  pinMode(LEDB, OUTPUT);
  pinMode(LEDR, OUTPUT);
  pinMode(LEDG, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LEDR, HIGH);
  digitalWrite(LEDG, HIGH);
  digitalWrite(LEDB, HIGH);
  digitalWrite(LED_BUILTIN, LOW);
  SerialUSB.begin(9600);

  pinMode(BLE_BUTTON_PIN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(BLE_BUTTON_PIN), scheduleBLEstart, FALLING);

  setupBLE();
  enableBLE();
}

void loop() {
  // put your main code here, to run repeatedly:
  rtos::ThisThread::sleep_for(std::chrono::seconds(30));  //A tester : consommation avec différents temps (1s, 5s, 30s, 10min)
  if(shouldStartBLE){
    enableBLE();
    shouldStartBLE = false;
    digitalWrite(LED_BUILTIN, LOW);
  }
}

void scheduleBLEstart(){
  shouldStartBLE = true;
  digitalWrite(LED_BUILTIN, HIGH);
}
