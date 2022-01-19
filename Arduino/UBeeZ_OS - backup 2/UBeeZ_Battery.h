#ifndef UBEEZ_BATTERY
#define UBEEZ_BATTERY

#include <Arduino.h>

#define PIN_BATTERY A7

void setupBattery();
float getBatteryPercentage();
float getBatteryVoltage();

#endif
