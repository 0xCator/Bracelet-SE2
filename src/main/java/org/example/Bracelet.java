package org.example;


import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONException;



public class Bracelet implements Runnable{

    private static final Random random = new Random();
    private final int age = 45;
    private String name;
    private State state = State.NORMAL;
    private int heartRate;
    private int[] bloodPressure;
    private double longitude;
    private double latitude;
    private String broker = "tcp://bracelet@broker.emqx.io:1883";
    private String topic  = "test";
    private MqttClient client;
    private MqttMessage mqttMessage;
    private JSONObject obj;
    public Bracelet(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
    public int getAge(){
        return this.age;
    }

    @Override
    public void run() {
        calcLocation();
        try {
            client = new MqttClient(broker, MqttClient.generateClientId());
            client.connect();
        } catch (Exception e) {}
        for (;;) {
            heartRate = getHeartRate();
            bloodPressure = getBloodPressure();

            try {
                obj = new JSONObject();
                obj.put("name", name);
                obj.put("heartRate", heartRate);
                obj.put("this.state", this.state);
                obj.put("longitude", longitude);
                obj.put("latitude", latitude);
                obj.put("bloodPressure", bloodPressure);
                String msg = obj.toString();
                mqttMessage = new MqttMessage(msg.getBytes());
                client.publish(topic, mqttMessage);
                if(this.state == State.CRITICAL)
                    Thread.sleep(5 * 60 * 1000);
                else
                    Thread.sleep(1000);
                move();
            } catch (Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public void setState(State state){
        this.state = state;
    }
    public State getState(){
        return this.state;
    }

    private int[] getBloodPressure(){
        int systolic = 0;
        int diastolic = 0;
        if(this.state == State.CRITICAL){
            systolic = random.nextInt(180 - 140 + 1) + 140;
            diastolic = random.nextInt(120 - 90 + 1) + 90;
        }else if(this.state == State.WARNING){
            systolic = random.nextInt(140 - 120 + 1) + 120;
            diastolic = random.nextInt(90 - 80 + 1) + 80;
        }else{
            systolic = random.nextInt(120 - 90 + 1) + 90;
            diastolic = random.nextInt(80 - 60 + 1) + 60;
        }
        return new int[]{systolic, diastolic};
    }

    private int getHeartRate(){
        int critical_high = 220 - this.age;
        int warning_high  = critical_high - 10;
        int normal        = (int)(critical_high * 0.85);
        int critical_low  = (int)(critical_high * 0.50); 
        int warning_low   = critical_low  + 10;
        boolean lowOrHigh = random.nextBoolean();
        if(lowOrHigh){
            if(this.state == State.CRITICAL){
                return random.nextInt(220 - critical_high + 1) + critical_high;
            }else if(this.state == State.WARNING){
                return random.nextInt(critical_high - warning_high + 1) + warning_high;
            }else{
                return random.nextInt(warning_high - normal + 1) + normal; 
            }
        }else{
            if(this.state == State.CRITICAL){
                return random.nextInt(critical_low - 30 + 1) + 1;
            }else if(this.state== State.WARNING){
                return random.nextInt(warning_low - critical_low + 1) + critical_low;
            }else{
                return random.nextInt(normal - warning_low + 1) + warning_low; 
            }
        }

    }

    private void move(){
        double[] point1 = {29.97047321608386, 31.257748416161125};
        double[] point2 = {29.953593501536787, 31.256031802501454};
        double[] point3 = {29.95348195204306, 31.285857965513877};
        double[] point4 = {29.96709006690293, 31.285686304147912};

        double minLat = Math.min(point1[0], Math.min(point2[0], Math.min(point3[0], point4[0])));
        double maxLat = Math.max(point1[0], Math.max(point2[0], Math.max(point3[0], point4[0])));
        double minLon = Math.min(point1[1], Math.min(point2[1], Math.min(point3[1], point4[1])));
        double maxLon = Math.max(point1[1], Math.max(point2[1], Math.max(point3[1], point4[1])));

        double range = 0.00001;

        boolean changeLatitude = random.nextBoolean();
        boolean changeLongitude = random.nextBoolean();

        int latitudeSign = random.nextBoolean() ? 1 : -1;
        int longitudeSign = random.nextBoolean() ? 1 : -1;

        this.latitude = latitude + (changeLatitude ? latitudeSign * random.nextDouble() * range : 0);
        this.longitude = longitude + (changeLongitude ? longitudeSign * random.nextDouble() * range : 0);

        this.latitude = Math.max(Math.min(this.latitude, maxLat), minLat);
        this.longitude = Math.max(Math.min(this.longitude, maxLon), minLon);


    }

    private void calcLocation(){
        double[] point1 = {29.97047321608386, 31.257748416161125};
        double[] point2 = {29.953593501536787, 31.256031802501454};
        double[] point3 = {29.95348195204306, 31.285857965513877};
        double[] point4 = {29.96709006690293, 31.285686304147912};

        double minLat = Math.min(point1[0], Math.min(point2[0], Math.min(point3[0], point4[0])));
        double maxLat = Math.max(point1[0], Math.max(point2[0], Math.max(point3[0], point4[0])));
        double minLon = Math.min(point1[1], Math.min(point2[1], Math.min(point3[1], point4[1])));
        double maxLon = Math.max(point1[1], Math.max(point2[1], Math.max(point3[1], point4[1])));
        
        this.latitude = minLat + (maxLat - minLat) * random.nextDouble();
        this.longitude = minLon + (maxLon - minLon) * random.nextDouble();
    }

}
