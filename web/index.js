/*
    Pramukh Govindaraju
    Aakash Kesavarapu
    Chirag Toprani
    CSE 176a - Healthcare Robotics - SP 18
    Node APIs - web server 
    Sources: Firebase documentation, Node Documentation
    QRCode library from Ryan Day - @soldair
*/

// Setup firebase account
var firebase = require('firebase-admin');
var serviceAccount = require('./firebase/serviceAccountKey.json');

firebase.initializeApp({
  credential: firebase.credential.cert(serviceAccount),
  databaseURL: 'https://percept-176a.firebaseio.com'
});

// Use express for routing
var express = require('express')
var app = express();
var path = require('path');
var bodyParser = require('body-parser')
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true}))


var QRCode = require('qrcode')

// generate random guid to use as patient ID
function guid() {
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
    s4() + '-' + s4() + s4() + s4();
} 
function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
        .toString(16)
        .substring(1);
}

// POST API that returns qr code based on patient ID
app.post("/getQR", function(request, response) {
    if (request.body.patientID) {
        QRCode.toDataURL(request.body.patientID, function (err, url) {
            response.json({"qrcode": url})
        })
    }
});

// Post vitals to database
const vitalsToStore = 10;
// storing 2, since, for proof of concept (only had one microcontroller)
var lastIndex_0 = 0;
var lastIndex_1 = 0;
var update = 0;
app.post("/postVitals", function(request, response) {
    currTime = (Math.round(new Date().getTime()/1000));
    // proof of concept uses hardcoded patient ID, since we only had one microcontroller
    if (request.body.patientID) {
        if (request.body.patientID == '7f29e0cf-d344-4a71-ba70-c8def387a17c') {
            lastIndex = lastIndex_0;
            rate = String(parseInt(request.body.heartRate) + 15)
            firebase.database().ref('Patients/' + request.body.patientID + '/vitals_' + lastIndex).set({
                heartRate: rate,
                timestamp: String(currTime)
            });
            lastIndex_0 = (lastIndex_0 + 1) % vitalsToStore;
        }
        else {
            lastIndex = lastIndex_1;
            firebase.database().ref('Patients/' + request.body.patientID + '/vitals_' + lastIndex).set({
                heartRate: request.body.heartRate,
                timestamp: String(currTime)
            });
            lastIndex_1 = (lastIndex_1 + 1) % vitalsToStore;
        }
        // Send successful response
        response.json({"Message": "successfully posted vitals"});
    }
});

// Create web server
var port = process.env.PORT || 3000;
app.get('/', (req, res) => res.send('Welcome to percept'))
app.listen(port, () => console.log('Example app listening on port 3000!'))
app.use(express.static(path.join(__dirname,'static')));
