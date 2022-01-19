#ifndef UBEEZ_BATTERY_WEIGHT_LIGHT
#define UBEEZ_BATTERY_WEIGHT_LIGHT

#define PIN_BATTERY A1
#define PIN_LIGHT_ENABLE 4
#define PIN_LIGHT_READ A6

#define PIN_WEIGHT_DOUT 6
#define PIN_WEIGHT_PSCK 7

#define DEFAULT_WEIGHT_OFFSET 107975
#define DEFAULT_WEIGHT_SCALE 29763

void setupBatteryLight();
void setupWeight();
float getBatteryPercentage();
float getBatteryVoltage();
float getWeight();
bool updateWeight();
int getLight();

#endif
