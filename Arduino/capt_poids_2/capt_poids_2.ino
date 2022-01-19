#include "HX711.h"
#include "rtos.h"
#include "mbed.h"

#define BUTTON_PIN 2
#define PIN_WEIGHT_DOUT 6
#define PIN_WEIGHT_PSCK 7

HX711 weightSensor;

double val = 0;

void setup() {
  // put your setup code here, to run once:
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  SerialUSB.begin(9600);
  weightSensor.begin(PIN_WEIGHT_DOUT, PIN_WEIGHT_PSCK);
  resetTare();
  attachInterrupt(digitalPinToInterrupt(2), setTare, FALLING);
}

void loop() {
  // put your main code here, to run repeatedly:
  /*weightSensor.power_down();
  rtos::ThisThread::sleep_for(std::chrono::seconds(3));
  weightSensor.power_up();
  rtos::ThisThread::sleep_for(std::chrono::seconds(3));*/
  
  if(weightSensor.wait_ready_timeout(1000)){
    SerialUSB.print("Weight units : ");
    SerialUSB.print(weightSensor.get_units(10));
    SerialUSB.print("\ttare : ");
    SerialUSB.print(val);
    SerialUSB.print("\toffset : ");
    SerialUSB.println(weightSensor.get_offset());
  }
  else{
    SerialUSB.println("Sensor not working !");
  }
}

void setTare(){
  double knownWeight = 25.2;
  val = weightSensor.get_value(10)/knownWeight;
  weightSensor.set_scale(val);
}

void resetTare(){
  SerialUSB.println("Reseting tare/scale");
  if(weightSensor.wait_ready_timeout(1000)){
    weightSensor.set_scale();
    weightSensor.tare();
  }
  else{
    SerialUSB.println("Sensor not working !");
  }
}
