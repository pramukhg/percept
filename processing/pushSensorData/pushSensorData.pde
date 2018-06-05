/*  CSE176A Processing (Input Sample)
 *   This example code demonstrates how to receive data between from the Arduino
 *   to the Processing client via Serial Communication (you can read about Serial Protocol here: 
 *   https://learn.sparkfun.com/tutorials/serial-communication/all)
 *   In this example, we transmit the number 3.14 from the Arduino to the Host PC and print this
 *   number to the processing terminal. THIS REQUIRES THE PROVIDED ARDUINO CODE TO BE
 *   INSTALLED ON THE ARDUINO TO WORK!
 *   
*/

import processing.serial.*;
import http.requests.*;

Serial myPort;  // Create object from Serial class
String val;     // Data received from the serial port -- note that all data transmitted through the Arduino is sent as a string!
float fval;     // Placeholder variable to store the data received from the Arduino.

void setup()
{
  String portName = Serial.list()[0]; //Change the 0 to a 1 or 2 etc. to match your port -- usually the default works.
  myPort = new Serial(this, portName, 9600); //Make sure that the baudrate (default: 9600) matches that of the Arduino.
}

void draw() //The "main" function in processing is "draw"
{
  if ( myPort.available() > 0) //This checks to see if the port in the setup is available.
  {
    val = myPort.readStringUntil('\n'); //Read in the value from the serial port.
    
    if (val != null){            // Note that this processing script runs asynchronously from the Arduino's transmission rate.
                                 // Thus, when Processing looks in the serial buffer, there is a chance that the value stored 
                                 // in "val" might contain nothing.
                                 
                                 // Note that the Arduino sends one LINE at a time, rather than individual variables. Thus, you will
                                 // need a way to parse the incoming data.
                                 
      fval = float(trim(val));   // This is how you convert the string to a usable (float) number.
      
      println(str(fval));        // Print it out in the console
      
      PostRequest post = new PostRequest("http://localhost:3000/postVitals?patientID=" + "91476455-7bdf-b892-406b-2f6a657d3e57", "utf-8");
      //post.addHeader("Content-Type", "application/json");
      post.addData("patientID", "91476455-7bdf-b892-406b-2f6a657d3e57");
      post.addData("temp", str(fval));
      post.addData("heartRate", "70");
      post.send();
      println("Reponse Content: " + post.getContent());
      //println("Reponse Content-Length Header: " + post.getHeader("Content-Length"));
    }
  }
  
}
