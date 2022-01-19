#include "UBeeZ_GPS.h"
#include "UBeeZ_LED.h"
#include "UBeeZ_Prefs.h"
#include <Serial.h>
#include "mbed.h"
#include "rtos.h"
#include <TinyGPS++.h>
#include <chrono>

using namespace rtos;

TinyGPSPlus gps;

std::chrono::time_point<std::chrono::system_clock> end_time;

bool DISABLE_GPS = false;
bool gpsEnabled = false;

void setupGPS(){
  if(GPS_DEBUG){
    SerialUSB.println("setupGPS");
  }
  
  Serial2.begin(9600);
  
  pinMode(PIN_TPL_DELAY, OUTPUT);
  pinMode(PIN_TPL_DONE, OUTPUT);

  digitalWrite(PIN_TPL_DELAY, LOW);
  digitalWrite(PIN_TPL_DONE, LOW);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  digitalWrite(PIN_TPL_DELAY, HIGH);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  digitalWrite(PIN_TPL_DELAY, LOW);

  led_blink_G(100); 
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(200));
  std::chrono::time_point<std::chrono::system_clock> time_out= std::chrono::system_clock::now() + std::chrono::seconds(5);
  while(!Serial2.available() && std::chrono::system_clock::now() < time_out){
    rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  }

  gpsEnabled = std::chrono::system_clock::now() < time_out;
  
  led_stop_G();
  digitalWrite(LEDG, HIGH);
  digitalWrite(PIN_TPL_DONE, HIGH);
  rtos::ThisThread::sleep_for(std::chrono::seconds(1));
  digitalWrite(PIN_TPL_DONE, LOW);
  if(GPS_DEBUG){
    if(gpsEnabled){
      Serial.println("GPS setup completed !");
    }
    else{
      Serial.println("GPS setup failed !");
    }
  }
}

bool get_GPS_location(){
  if(GPS_DEBUG){
    SerialUSB.println("get_GPS_location start");
  }
  if(!isGPSEnabled()){
    if(GPS_DEBUG){
      SerialUSB.println("get_GPS_location failed (gps not enabled)");
    }
    return false;
  }
  digitalWrite(PIN_TPL_DELAY, HIGH);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  digitalWrite(PIN_TPL_DELAY, LOW);
  
  end_time = std::chrono::system_clock::now() + GPS_UPTIME;

  bool stop_GPS = false;
  bool got_GPS_signal = false;
  while(!stop_GPS){
    while(Serial2.available()){
      gps.encode(Serial2.read());
    }
    if(GPS_DEBUG){
      if(gps.satellitesAll.isUpdated()){
        SerialUSB.print("Sat all : ");
        SerialUSB.println(gps.satellitesAll.value());
      }
    }
    
    if(gps.location.isUpdated()){
      double newLat = gps.location.lat();
      double newLng = gps.location.lng();
      if(GPS_DEBUG){
        SerialUSB.println("GOT LOCATION !");
        SerialUSB.print("Lat : ");
        SerialUSB.println(newLat, 6);
        SerialUSB.print("Long : ");
        SerialUSB.println(newLng, 6);
        SerialUSB.print("Sat : ");
        SerialUSB.println(gps.satellites.value());
      }
      getPrefs()->gpsLatitude = newLat;
      getPrefs()->gpsLongitude = newLng;
      writePrefs();
      stop_GPS = true;
      got_GPS_signal = true;
    }
    else if((gps.satellitesAll.value() <= 0 && std::chrono::system_clock::now() >= end_time)
              || (gps.satellitesAll.value() > 0 && std::chrono::system_clock::now() >= end_time+GPS_ADDITIONNAL_UPTIME)){
      if(GPS_DEBUG){
        SerialUSB.println("GPS timeout (no fix) !");
      }
      stop_GPS = true;
    }
    rtos::ThisThread::sleep_for(GPS_TICK);
  }

  
  if(GPS_DEBUG){
    SerialUSB.println("stoping GPS");
  }
  digitalWrite(PIN_TPL_DONE, HIGH);
  rtos::ThisThread::sleep_for(std::chrono::milliseconds(500));
  digitalWrite(PIN_TPL_DONE, LOW);
  if(GPS_DEBUG){
    SerialUSB.println("get_GPS_location done");
  }

  return got_GPS_signal;
}

bool isGPSEnabled(){
  return gpsEnabled;
}
