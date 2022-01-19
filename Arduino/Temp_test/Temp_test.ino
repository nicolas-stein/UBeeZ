// multiple devices OR single parasite powered device requires external pull up of 1.7~2.2 kOm

#define MAXIMWIRE_EXTERNAL_PULLUP

#include <MaximWire.h>

#define PIN_TEMPERATURE_BUS 9
#define ADDR_TEMPERATURE_1 "280C8016A8013C93"
#define ADDR_TEMPERATURE_2 "2876BF56B5013C95"

MaximWire::Bus temperature_bus(PIN_TEMPERATURE_BUS);
MaximWire::DS18B20 *temperature_device_1 = NULL;
MaximWire::DS18B20 *temperature_device_2 = NULL;

void setup() {
    Serial.begin(9600);
    delay(3000);
    MaximWire::Discovery discovery = temperature_bus.Discover();
    do {
        MaximWire::Address address;
        if (discovery.FindNextDevice(address)) {
            if(address.ToString()==ADDR_TEMPERATURE_1){
              Serial.println("Found device 1");
              temperature_device_1 = new MaximWire::DS18B20(address);
            }
            else if(address.ToString()==ADDR_TEMPERATURE_2){
              Serial.println("Found device 2");
              temperature_device_2 = new MaximWire::DS18B20(address);
            }
        }
    } while (discovery.HaveMore());
}

void loop() {
    delay(1000);
    float temp_1 = temperature_device_1->GetTemperature<float>(temperature_bus);
    float temp_2 = temperature_device_2->GetTemperature<float>(temperature_bus);
    if (!isnan(temp_1)) {
        Serial.print("Temp 1 : ");
        Serial.println(temp_1);
    }
    if (!isnan(temp_2)) {
        Serial.print("Temp 2 : ");
        Serial.println(temp_2);
    }
    temperature_device_1->Update(temperature_bus);
    temperature_device_2->Update(temperature_bus);
}
