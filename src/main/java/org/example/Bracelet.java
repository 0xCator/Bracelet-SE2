package org.example;


import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONException;


public class Bracelet implements Runnable{

    private final int MIN_VALUE = 1;
    private final int MAX_VALUE = 250;
    private static final Random random = new Random();
    private String name;
    private String broker = "tcp://bracelet@broker.emqx.io:1883";
    private String topic  = "test";
    private MqttClient client;
    private MqttMessage mqttMessage;
    private JSONObject obj;
    public Bracelet(String name){
        this.name = name;
    }


    @Override
    public void run() {
        try {
            client = new MqttClient(broker, MqttClient.generateClientId());
            client.connect();
        } catch (Exception e) {}
        for (;;) {
            int randomValue = generateRandomValue();
            try {
                obj = new JSONObject();
                obj.put("name", name);
                obj.put("value", randomValue);
                String msg = obj.toString();
                mqttMessage = new MqttMessage(msg.getBytes());
                client.publish(topic, mqttMessage);
                Thread.sleep(1000);
            } catch (Exception e ) {
                e.printStackTrace();
            }
        }
    }

    private int generateRandomValue() {
        return random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE;
    }



}
