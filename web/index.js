var firebase = require('firebase-admin');
var serviceAccount = require('./firebase/serviceAccountKey.json');

firebase.initializeApp({
  credential: firebase.credential.cert(serviceAccount),
  databaseURL: 'https://percept-176a.firebaseio.com'
});

var express = require('express')
var app = express();
var path = require('path');
var bodyParser = require('body-parser')
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true}))

var QRCode = require('qrcode')

function guid() {
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
      s4() + '-' + s4() + s4() + s4();
  }
  
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }

app.post("/getQR", function(request, response) {
    if (request.body.patientID) {
        QRCode.toDataURL(request.body.patientID, function (err, url) {
            response.json({"qrcode": url})
        })
    } else if (request.body.firstName && request.body.lastName) {

    }
    /*if (request.body.firstName && request.body.lastName) {
        patientID = guid()
        firebase.database().ref('Patients/' + patientID).set({
        firstName: request.body.firstName,
        lastName: request.body.lastName
    });
    response.json({"Message": "welcome to percept"})
    }*/
});

const vitalsToStore = 10;
var lastIndex = 0;
app.post("/postVitals", function(request, response) {
    currTime = (Math.round(new Date().getTime()/1000));
    if (request.body.patientID) {
        firebase.database().ref('Patients/' + request.body.patientID + '/vitals_' + lastIndex).set({
            temp: request.body.temp,
            heartRate: request.body.heartRate,
            timestamp: currTime
        });
        lastIndex = (lastIndex + 1) % vitalsToStore;
        response.json({"Message": "successfully posted vitals"});
    }
});

var port = process.env.PORT || 3000;

app.get('/', (req, res) => res.send('Welcome to percept'))
app.listen(port, () => console.log('Example app listening on port 3000!'))
app.use(express.static(path.join(__dirname,'static')));
