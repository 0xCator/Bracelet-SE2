
const mqtt = require('mqtt');

const url = "tcp://bracelet@broker.emqx.io:1883";
const topic = "bracelet";
const readingsApi = "http://localhost:3000/api/readings";

const client = mqtt.connect(url);

client.on('connect', () => {
    console.log('mqtt connected');
    client.subscribe(topic);
});

client.on('message', (topic, message) => {
    const msgjson = JSON.parse(message);
    const userId  = msgjson.userID;
    console.log(msgjson);
    fetch(readingsApi,{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(msgjson)
    })

    const userReadingsApi = `http://localhost:3000/api/users/${userId}/readings`
    fetch(userReadingsApi, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(msgjson)
    })});
client.on('close', () => {
    console.log('mqtt disconnected');
});

process.on('SIGINT', () => {
    client.end();
    process.exit();
});

