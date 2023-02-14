var express = require('express');
var app = express();
var port = process.env.PORTVAL || 3000;
var dbconnection = require('./db').db();
const googleMapsClient = require('@google/maps').createClient({
    key: 'AIzaSyA5K2fbI4KEeQ3fDX4z5RsF5xCP6kWoQx8'
});

app.get('/', function (req, res) {
    res.send('Yay this is a new page.');
})

app.get('/start_journey', function (req, res) {
    console.log(req.query);
    // sendNotification(req.query);

    // dbconnection.query('Select * from test', function (err, result) {
    //     console.log(result);
    //     res.send(result);
    // })

    // // Geocode an address.
    // googleMapsClient.geocode({
    //     address: 'Magdeburg, Germany'
    // }, function (err, response) {
    //     if (!err) {
    //         res.send(response.json.results);
    //     } else {
    //         console.log(err);
    //     }
    // });

    // Directions between src and dest.
    googleMapsClient.directions({
        origin: req.query.source,
        destination: req.query.destination,
        mode: req.query.mode,
    }, function (err, response) {
        if (!err) {
            var data = {
                "distance": response.json.routes[0].legs[0].distance.text,
                "duration": response.json.routes[0].legs[0].duration.text,
                "steps": response.json.routes[0].legs[0].steps,
                "travel_mode": req.query.mode
            }
            res.send(data);
        } else {
            console.log(err);
        }
    });
})

var server = app.listen(3000, function () {
    var host = server.address().address
    var port = server.address().port

    console.log("Example app listening at http://%s:%s", host, port)
})

function sendNotification(params) {
    var FCM = require('fcm-node');
    var serverKey = 'AAAA3xILHvA:APA91bES40ae--DfsgAWmfT73yg5TanTv6zSv3YlHdyElIQPm3uoe_xSPEHmtvzgpB4M_qsdJJCTZfbLS6ARoTFQL9BPyASrfEKbP2M3zwaMhp-ju1LAmAdXMJqil7GAQ3djB7GYefvN'; //put your server key here
    var fcm = new FCM(serverKey);

    var message = { //this may vary according to the message type (single recipient, multicast, topic, et cetera)
        to: params.token,
        // collapse_key: 'green',

        notification: {
            title: 'Title of your push notification',
            body: 'Body of your push notification'
        },

        // data: {  //you can send only notification or only data(or include both)
        //     title: 'Title of your data notification',
        //     body: 'Body of your data notification'
        // }
    };

    fcm.send(message, function (err, response) {
        if (err) {
            console.log("Something has gone wrong!");
        } else {
            console.log("Successfully sent with response: ", response);
        }
    });
}