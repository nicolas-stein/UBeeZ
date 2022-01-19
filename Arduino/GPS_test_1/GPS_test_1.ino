#include <TinyGPS++.h>

TinyGPSPlus gps;

void setup() {
  // put your setup code here, to run once:
  //serialPC.begin(9600);
  SerialUSB.begin(9600);
  Serial2.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  while(Serial2.available()){
    gps.encode(Serial2.read());
  }

  if(gps.satellitesAll.isUpdated()){
    SerialUSB.print("Sat all : ");
    SerialUSB.println(gps.satellitesAll.value());
  }
  if(gps.location.isUpdated()){
      SerialUSB.print("Lat : ");
      SerialUSB.println(gps.location.lat(), 6);
      SerialUSB.print("Long : ");
      SerialUSB.println(gps.location.lng(), 6);
      SerialUSB.print("Sat : ");
      SerialUSB.println(gps.satellites.value());
      delay(1000);
    }
}
