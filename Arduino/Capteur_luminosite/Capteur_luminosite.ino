#define PIN_LIGHT A6
#include "rtos.h"

void setup() {
  // put your setup code here, to run once:
  pinMode(PIN_LIGHT, INPUT);
  SerialUSB.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  int a=0;
  for(int i=0;i<20;i++){
    a+=analogRead(PIN_LIGHT);
    rtos::ThisThread::sleep_for(std::chrono::milliseconds(10));
  }
  a = a/20;
  SerialUSB.println(a);
}
