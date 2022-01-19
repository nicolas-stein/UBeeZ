#include "UBeeZ_Battery.h"

void setupBattery(){
  pinMode(PIN_BATTERY, INPUT);
}

float getBatteryPercentage(){
  float batteryPercentage = ((getBatteryVoltage()-2.9) * 100)/1.3;
  if(batteryPercentage > 100){
    return 100;
  }
  else if(batteryPercentage <0){
    return 0;
  }
  return batteryPercentage;
}

float getBatteryVoltage(){
  return ((float)analogRead(PIN_BATTERY) * 6.6)/1023;
}
