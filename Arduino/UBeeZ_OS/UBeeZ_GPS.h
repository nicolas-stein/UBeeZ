#ifndef UBEEZ_GPS
#define UBEEZ_GPS

#define PIN_TPL_DELAY 3
#define PIN_TPL_DONE 13

#define GPS_DEBUG true
#define GPS_UPTIME std::chrono::minutes(1)
#define GPS_ADDITIONNAL_UPTIME std::chrono::minutes(2)
#define GPS_TICK std::chrono::seconds(1)

void setupGPS();
bool get_GPS_location();

bool isGPSEnabled();

#endif
