/*
    Pramukh Govindaraju
    Aakash Kesavarapu
    Chirag Toprani
    CSE 176a - Healthcare Robotics - SP 18
    Read sensor code 
    Sources: Processing documentation, code from L Riek, D Chan from CSE 176A
    Uses Processing HTTP Request library
*/

int sensorPin = A1;   // This maps to pin A0 (Analog Pin 0)
int sensorValue = 0;  // Variable to store the value coming from the sensor

void setup() {
  Serial.begin(9600); // This enables Serial output at a baud rate of 9600.
}

void loop() {
  
  // Read the value from the sensor:
  sensorValue = analogRead(sensorPin);

  // Convert sensor value to voltage
  float voltage= sensorValue * (5 / 1024.0);

  // Convert voltage value to both measurements of temperature
  float C = (voltage - 0.5)*100;
  float F = (C*9.0/5.0)+32;

  // Print temperature values in both Celcius and Farenheit
  Serial.println(F);

  // Delay for output clarity
  delay(500);
}
