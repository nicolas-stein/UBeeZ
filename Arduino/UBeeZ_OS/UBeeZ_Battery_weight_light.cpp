#include "UBeeZ_Battery_weight_light.h"
#include "HX711.h"
#include <Arduino.h>
#include "rtos.h"

HX711 weightSensor;
double weight = NAN;

void setupBatteryLight(){
  pinMode(PIN_BATTERY, INPUT);
  pinMode(PIN_LIGHT_ENABLE, OUTPUT);
  pinMode(PIN_LIGHT_READ, INPUT);
  digitalWrite(PIN_LIGHT_ENABLE, LOW);
}

void setupWeight(){
  weightSensor.begin(PIN_WEIGHT_DOUT, PIN_WEIGHT_PSCK);
  weightSensor.power_down();
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

int getLight(){
  digitalWrite(PIN_LIGHT_ENABLE, HIGH);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(200));
  int a = analogRead(PIN_LIGHT_READ);
  digitalWrite(PIN_LIGHT_ENABLE, LOW);
  return a;
}

bool updateWeight(){
  weightSensor.power_up();
  if(weightSensor.wait_ready_timeout(1000)){
    weightSensor.set_offset(DEFAULT_WEIGHT_OFFSET);
    weightSensor.set_scale(DEFAULT_WEIGHT_SCALE);
    weight = weightSensor.get_units(10);
    weightSensor.power_down();
    return true;
  }
  else{
    weightSensor.power_down();
    return false;
  }
}

float getWeight(){  //Retourne poid en kg
  return weight;
}
