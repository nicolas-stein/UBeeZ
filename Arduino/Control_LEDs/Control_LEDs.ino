// How to control the RGB Led and Power Led of the Nano 33 BLE boards.  

void setup() {
 
 // intitialize the digital Pin as an output
  pinMode(LEDR, OUTPUT);
  pinMode(LEDB, OUTPUT);
  pinMode(LEDG, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);

}

// the loop function runs over and over again
void loop() {
  digitalWrite(LEDR, LOW); // turn the LED off by making the voltage LOW
  delay(1000);            // wait for a second
  digitalWrite(LEDG, LOW);
  delay(1000);  
  digitalWrite(LEDB, LOW);
  delay(1000);  
  digitalWrite(LEDR, HIGH); // turn the LED on (HIGH is the voltage level)
  delay(1000);                         
  digitalWrite(LEDG, HIGH);
  delay(1000);  
  digitalWrite(LEDB, HIGH);
  delay(1000);  
  digitalWrite(LED_BUILTIN, HIGH);
  delay(1000);  
  digitalWrite(LED_BUILTIN, LOW);
  delay(1000);  
}
