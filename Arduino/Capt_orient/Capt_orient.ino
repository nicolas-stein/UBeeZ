#include <Arduino_LSM9DS1.h>
#define buttonpin 3
#define ledpin 13

// Modify these offset numbers at will with the outcome of the calibration values  
float offsetX = -6.78;//0;    
float offsetY = -1.95 ;//0;
const int maxcount = 20;       //higher number = more accurate but slower

void setup() {
     pinMode(ledpin, OUTPUT);
     pinMode(buttonpin, INPUT);
     digitalWrite(ledpin, LOW);     
     Serial.begin(115200);
     while(!Serial);                     // wait till the serial monitor connects
     Serial.println("Starting Compass");
     if (IMU.begin()) Serial.println("IMU available and working"); 
     else Serial.println("Failed to initialize IMU!");
}

void loop() {
float x, y, z;
float averX=0 , averY =0;
     //Serial.print("Measuring "); 
     for (int i=1;i<=maxcount;i++){
       while (!IMU.magneticFieldAvailable());
       IMU.readMagneticField(x, y, z);
       averX += x/maxcount;
       averY += y/maxcount;
       digitalWrite(ledpin, LOW);
     } 
     float heading= atan2(averY-offsetY,averX-offsetX)*180/PI +180;
     digitalWrite(ledpin, HIGH);
     //Serial.print(" Compass direction "); 
     Serial.println(heading);
     if (digitalRead(buttonpin)==1) recalibrate();   //button pressed
}
 
void recalibrate(){
float x, y, z, Xmin, Xmax, Ymin, Ymax  ;
boolean ledIsOn = true;
       digitalWrite(ledpin, ledIsOn);
       Serial.println("Recalibrating");
       while (!IMU.magneticFieldAvailable());
       IMU.readMagneticField(Xmin, Ymin, z);     //find initial values
       Xmax = Xmin; Ymax = Ymin;
       //while (digitalRead(buttonpin)==0); //wait till button not pressed       
       Serial.println("Got initial values");
       while (digitalRead(buttonpin)==1)   {              //keep measuring  till button pressed            
          while (!IMU.magneticFieldAvailable());  // wait till new magnetic reading is available
          IMU.readMagneticField(x, y, z);
          Xmax = max (Xmax, x); Xmin = min (Xmin, x);
          Ymax = max (Ymax, y); Ymin = min (Ymin, y);
          digitalWrite(ledpin, ledIsOn = !ledIsOn);  //reverse on/off led
       }
       offsetX= (Xmax + Xmin)/2;
       offsetY= (Ymax + Ymin)/2; 
       Serial.print("New offset X= ");  
       Serial.print(offsetX);
       Serial.print(" Y= ");
       Serial.println(offsetY); 
}
