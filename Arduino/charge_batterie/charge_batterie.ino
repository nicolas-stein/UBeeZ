
int bat = A7;

void setup() {
  
  Serial.begin(9600);
  Serial.println("Début de la transmission");
  pinMode(A7,INPUT);
  
}

void loop() {
  
  int  charge = analogRead(bat);
  Serial.println(map(charge,511, 659, 15, 100)); // Conversion de la tension de la batterie en pourcentage
  delay(1000);

}
