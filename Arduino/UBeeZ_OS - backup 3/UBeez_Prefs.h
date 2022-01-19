#ifndef UBEEZ_PREFS
#define UBEEZ_PREFS

#include <NanoBLEFlashPrefs.h>
#include <Arduino.h>
#include <Serial.h>
#include "rtos.h"

#define DEBUG_PREFS true

typedef struct FlashStruct {
  unsigned short sigfoxSyncDelay = 10*60;
  double gpsLatitude = 0;
  double gpsLongitude = 0;
  float gpsHeading = 0;
} FlashPrefs;

void setupPrefs();
FlashPrefs* getPrefs();
void writePrefs();

#endif
