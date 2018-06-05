/* CSE176A Read Sensor Example Code 
 * This code reads an analog voltage signal from pin A0. This analog voltage is
 * then converted to a digital signal, which is represented by an unsigned 10-bit
 * integer. For instance, if we used a 5V sensor, 0V would map to a numeric value of
 * "0" and 5V would map to a numeric value of 1023.
 *
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
  delay(10000);
}
