/*
    Pramukh Govindaraju
    Aakash Kesavarapu
    Chirag Toprani
    CSE 176a - Healthcare Robotics - SP 18
    Processing - pushes sensor data to node
    Sources: Processing documentation, code from L Riek, D Chan from CSE 176A
    Uses Processing HTTP Request library
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
      
      // request instance to send requests for patient 1
      PostRequest post = new PostRequest("http://localhost:3000/postVitals", "utf-8");
      //post.addHeader("Content-Type", "application/json");
      post.addData("patientID", "91476455-7bdf-b892-406b-2f6a657d3e57");
      post.addData("heartRate", str(fval));
      float temp = 99.1;
      post.addData("temp", str(temp));
      println(str(temp));
      post.send();
      println("Reponse Content: " + post.getContent());
      
      // request instance to send requests for patient 2
      PostRequest post2 = new PostRequest("http://localhost:3000/postVitals?patientID=", "utf-8");
      //post.addHeader("Content-Type", "application/json");
      post2.addData("patientID", "7f29e0cf-d344-4a71-ba70-c8def387a17c");
      post2.addData("heartRate", str(fval));
      float temp2 = 98.6;
      post2.addData("temp", str(temp2));
      println(str(temp2));
      post2.send();
      println("Reponse Content2: " + post.getContent());
      
    }
  }
  
}
