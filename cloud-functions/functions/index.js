const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//     response.send("Hello from Firebase!");
//     console.log("hello from firebase");
// });

exports.onDataAddedEurop = functions.region('europe-west1').database.ref('dangers/{mUid}').onWrite(async (snap, contexte) => {

    let number = snap.after.child('number').val();
    let type = snap.after.child('type').val() + " around";

    let alt = snap.after.child('location').child('alt').val();
    let long = snap.after.child('location').child('long').val();


    console.log("latitude : " + alt);
    console.log("longitude:  " + long);
    console.log("number : " + number);

    //let rep;
    let registrationTokens = [];

    await admin.database().ref("users").once("value").then((snapshot) => {
        snapshot.forEach((userSnap) => {
            console.log("username de item en cours est : " + userSnap.child("username").val())
            console.log(getDistanceFromLatLonInKm(userSnap.child("location").child('alt').val(),
                userSnap.child("location").child('long').val(), alt, long))

            if (getDistanceFromLatLonInKm(userSnap.child("location").child('alt').val(),
                userSnap.child("location").child('long').val(), alt, long) < 2) {
                console.log("username de item à notifier est : " + userSnap.child("username").val())

                registrationTokens.push(userSnap.child('token').val())
            }


        });

        return registrationTokens;
    })

    // let token = "didWZPl6TfqrPNxe2Un0IO:APA91bEvpqg_5zhDvq5Q4xg8N4WQ0Pzvq8iM-HfEtQ4jyBBlV3TqBAamWx5yhBr5vIcwNulP34stiFN3U1ryDQ4ZksOzEu4ShGX6ZQqc7x9VIXoAWScSqiPy3ODp9b8ncqoTDZAPgmIP";

    let payload = {
        notification: {
            title: "WARNING",
            body: type,
            badge: '1',
            sound: 'default'

        }
    };

    if (typeof registrationTokens !== "undefined" && registrationTokens.length !== null
        && registrationTokens.length > 0) {
        console.log("vous etes ici")

        const promises = [];
        for (let i = 0; i < registrationTokens.length; i++) {
            promises.push(admin.messaging().sendToDevice(registrationTokens[i],payload));
            console.log("message number " +i)
        }
        return Promise.all(promises);

        // return admin.messaging().sendMulticast(message)
        //     .then((response) => {
        //         // Response is a message ID string.
        //         console.log(response.successCount + ' messages were sent successfully');
        //         return response;
        //     })
        //     .catch((error) => {
        //         console.log('Error sending message:', error);
        //     });


    }

});


function getDistanceFromLatLonInKm(lat1, lon1, lat2, lon2) {
    const R = 6371; // Radius of the earth in km
    const dLat = deg2rad(lat2 - lat1);  // deg2rad below
    const dLon = deg2rad(lon2 - lon1);
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2)
    ;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const d = R * c; // Distance in km
    return d;
}

function deg2rad(deg) {
    return deg * (Math.PI / 180)
}

function getMessages(registrationTokens, type) {
    const messages = [];
    for (let i = 0; i < registrationTokens.length; i++) {

        console.log("le token est " + registrationTokens[i]);
        console.log("nombre de token est " + registrationTokens.length);

        console.log(type)
        messages.push({

            "notification": {
                "title": 'WARNING',
                "body": type,
                "badge": '1',
                "sound": 'default'

            },

            "tokens": registrationTokens[i]

        });
    }
    return messages;
}
