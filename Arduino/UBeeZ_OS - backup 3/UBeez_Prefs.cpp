#include "UBeeZ_Prefs.h"

NanoBLEFlashPrefs prefsManager;
FlashPrefs prefs;

void setupPrefs(){
  int rc = prefsManager.readPrefs(&prefs, sizeof(prefs));
  if(rc == FDS_SUCCESS){
    if(DEBUG_PREFS){
      SerialUSB.println("Preferences found !");
    }
  }
  else{
    if(DEBUG_PREFS){
      SerialUSB.print("No preferences found. Return code: ");
      SerialUSB.print(rc);
      SerialUSB.print(", ");
      SerialUSB.println(prefsManager.errorString(rc));
    }
  }

  if(DEBUG_PREFS){
    SerialUSB.println("Preferences values :");
    SerialUSB.print("sigfoxSyncDelay : ");
    SerialUSB.println(prefs.sigfoxSyncDelay);
    SerialUSB.print("gpsLatitude : ");
    SerialUSB.println(prefs.gpsLatitude);
    SerialUSB.print("gpsLongitude : ");
    SerialUSB.println(prefs.gpsLongitude);
    SerialUSB.print("gpsHeading : ");
    SerialUSB.println(prefs.gpsHeading);
  }
}

FlashPrefs* getPrefs(){
  return &prefs;
}

void writePrefs(){
  if(DEBUG_PREFS){
    SerialUSB.println("Writting preferences values :");
    SerialUSB.print("sigfoxSyncDelay : ");
    SerialUSB.println(prefs.sigfoxSyncDelay);
    SerialUSB.print("gpsLatitude : ");
    SerialUSB.println(prefs.gpsLatitude);
    SerialUSB.print("gpsLongitude : ");
    SerialUSB.println(prefs.gpsLongitude);
    SerialUSB.print("gpsHeading : ");
    SerialUSB.println(prefs.gpsHeading);
  }
  
  prefsManager.writePrefs(&prefs, sizeof(prefs));
  // Wait until completion
  while (!prefsManager.operationCompleted()) {
    rtos::ThisThread::sleep_for(std::chrono::milliseconds(200));
  }
}
