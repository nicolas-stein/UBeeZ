#include "rtos.h"

bool b = false;

void setup() {
  // put your setup code here, to run once:
  //serialPC.begin(9600);
  SerialUSB.begin(9600);
  Serial2.begin(9600);
  pinMode(2, INPUT_PULLUP);
  pinMode(LED_PWR, OUTPUT);
  digitalWrite(LED_PWR, LOW);
  //attachInterrupt(digitalPinToInterrupt(2), a, FALLING);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial.available()){
    Serial2.write(Serial.read());
  }
  if(Serial2.available()){
    Serial.write(Serial2.read());
  }

  if(b){
    b = false;
    SerialUSB.println("Sleeping...");
    SerialUSB.print("$PMTK225,4\r\n");
    Serial2.print("$PMTK225,4\r\n");
    
    SerialUSB.print("$PMTK161,0*28\r\n");
    Serial2.print("$PMTK161,0*28\r\n");
    rtos::ThisThread::sleep_for(std::chrono::seconds(30));
  }
}

void a(){
  b = true;
}
