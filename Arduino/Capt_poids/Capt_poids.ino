#include "HX711.h"

//Button for sensor's calibration
const int button = 2;

// HX711 circuit wiring
const int LOADCELL_DOUT_PIN = 6;
const int LOADCELL_SCK_PIN = 7;

HX711 weightSensor;

void setup(){
  Serial.begin(38400);

  pinMode(button, INPUT_PULLUP);
  weightSensor.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);

  set_sensor();
}

void set_sensor() {
  Serial.println("==== Début du calibrage du capteur de poids ====");
  Serial.println("Pour démarrer appuyer sur le bouton poussoir.");

  while (digitalRead(button) == HIGH);

  weightSensor.set_scale();
  weightSensor.tare();
  Serial.println("==== Placez un poids CONNU sur le capteur ====");
  Serial.println("\nAttention : Le poids doit être suffisamment conséquent (~40-90kg)");
  Serial.println("\nNE PAS ENLEVER LE POIDS AVANT LA FIN DE L'OPERATION\n");
  Serial.println("Pour continuer appuyer sur le bouton poussoir.");

  while (digitalRead(button) == HIGH);

  float averageTare = 0;
  for (byte i = 0 ; i < 50 ; i++) {
    averageTare += weightSensor.get_units(10);
  }
  averageTare /= 50;
  averageTare /= 25.4;

  Serial.println("Calcul du calibrage terminé.");
  Serial.println("Veuillez retirer le poids positionné sur le capteur.");
  Serial.println("\n==== Fin du calibrage du capteur de poids ====");
  Serial.println("\n==============================================\n");
  Serial.println("========= Initialisation de la pesée =========");
  Serial.println("\nVérifier qu'aucun objet n'est présent sur le capteur");
  Serial.println("Pour continuer appuyer sur le bouton poussoir.");

  while (digitalRead(button) == HIGH);

  Serial.println("Before setting up the scale:");
  Serial.print("read: \t\t");
  Serial.println(weightSensor.read());      // print a raw reading from the ADC

  Serial.print("read average: \t\t");
  Serial.println(weightSensor.read_average(20));   // print the average of 20 readings from the ADC

  Serial.print("get value: \t\t");
  Serial.println(weightSensor.get_value(5));   // print the average of 5 readings from the ADC minus the tare weight (not set yet)

  Serial.print("get units: \t\t");
  Serial.println(weightSensor.get_units(5), 1);  // print the average of 5 readings from the ADC minus tare weight (not set) divided
            // by the SCALE parameter (not set yet)

  weightSensor.set_scale(averageTare);                      // this value is obtained by calibrating the scale with known weights; see the README for details
  weightSensor.tare();               // reset the scale to 0

  Serial.println("After setting up the scale:");

  Serial.print("read: \t\t");
  Serial.println(weightSensor.read());                 // print a raw reading from the ADC

  Serial.print("read average: \t\t");
  Serial.println(weightSensor.read_average(20));       // print the average of 20 readings from the ADC

  Serial.print("get value: \t\t");
  Serial.println(weightSensor.get_value(5));   // print the average of 5 readings from the ADC minus the tare weight, set with tare()

  Serial.print("get units: \t\t");
  Serial.println(weightSensor.get_units(5), 1);        // print the average of 5 readings from the ADC minus tare weight, divided
            // by the SCALE parameter set with set_scale

  Serial.println("\n==== Fin initialisation de la pesée ====");
  Serial.println("\n========================================\n");

  Serial.println("Appuyer sur le bouton pour débuter l'utilisation du capteur");

  while (digitalRead(button) == HIGH);

}

void loop(){
  Serial.print("one reading:\t");
  Serial.print(weightSensor.get_units(), 1);
  Serial.print("\t| average:\t");
  Serial.println(weightSensor.get_units(10), 1);

  weightSensor.power_down();             // put the ADC in sleep mode
  delay(5000);
  weightSensor.power_up();
}
