
const mqtt = require('mqtt');

const url = "tcp://bracelet@broker.emqx.io:1883";
const topic = "test";

const client = mqtt.connect(url);

client.on('connect', () => {
    console.log('mqtt connected');
    client.subscribe(topic);
});

client.on('message', (topic, message) => {
    const json = JSON.parse(message);
    console.log(json);
});
client.on('close', () => {
    console.log('mqtt disconnected');
});

process.on('SIGINT', () => {
    client.end();
    process.exit();
});

