package org.example;


import org.eclipse.paho.client.mqttv3.*;

import java.util.*;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;


import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Bracelet implements Runnable{

    private static final Random random = new Random();
    private int age;
    private State state = State.NORMAL;
    private int heartRate;
    private String patientID;
    private String ID = UUID.randomUUID().toString();
    private String patientToken = null;
    private double longitude;
    private double latitude;
    private String broker = "tcp://bracelet@broker.emqx.io:1883";
    private String topic  = "bracelet";
    private MqttClient client;
    private MqttMessage mqttMessage;
    private JSONObject bracelet;
    private JSONObject bloodPressureJSON;
    private JSONObject locationJSON;

    public Bracelet(String token) throws Exception{
        if(pair(token) == null)
            System.out.println("Invalid token");
        else{
            System.out.println("Bracelet paired successfully");
            fetchAge();
        }
    }

    public String getPatientToken(){
        return this.patientToken;
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
        while(patientToken != null) {
                calcReading();
            try {
                bracelet = new JSONObject();
                bracelet.put("ID", this.ID);
                bracelet.put("userID", this.patientID);
                bracelet.put("heartRate", heartRate);
                bracelet.put("state", this.state.getValue());
                bracelet.put("location", locationJSON);
                bracelet.put("bloodPressure", bloodPressureJSON);
                String msg = bracelet.toString();
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
    public void fetchAge() throws Exception{
        String apiUrl = "http://127.0.0.1:3000/api/users/"+this.patientID;
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(apiUrl);
        httpGet.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(httpGet);
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line ="";
        while((line = br.readLine()) != null){
            JSONObject jsonObject = new JSONObject(line);
            this.age = jsonObject.getJSONObject("userInfo").getInt("age");
        }
    }

    public String pair(String token) throws Exception{

        String apiUrl = "http://127.0.0.1:3000/api/functions/pair";

        HttpClient httpClient = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(apiUrl);
        httpPatch.setHeader("Content-type", "application/json");
        StringEntity stringEntity = new StringEntity("{\"braceletID\": \""+this.ID+"\", \"token\": \""+token+"\"}");
        httpPatch.setEntity(stringEntity);
        HttpResponse response = httpClient.execute(httpPatch);
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line =""; 
        
        while((line = br.readLine()) != null){
            if(line.equals("null") ){
                return null;
            }
            JSONObject jsonObject = new JSONObject(line);
            if(jsonObject.has("_id")){
                this.patientToken = token;
                this.patientID = jsonObject.getString("_id");
                return this.patientToken;
            }
        }
        return null;
    }
    public void setState(State state){
        this.state = state;
    }
    public State getState(){
        return this.state;
    }


    private void getBloodPressure(State state){
        int systolic = 0;
        int diastolic = 0;
        if(state == State.CRITICAL){
            systolic = random.nextInt(180 - 140 + 1) + 140;
            diastolic = random.nextInt(120 - 90 + 1) + 90;
        }else if(state == State.WARNING){
            systolic = random.nextInt(140 - 120 + 1) + 120;
            diastolic = random.nextInt(90 - 80 + 1) + 80;
        }else{
            systolic = random.nextInt(120 - 90 + 1) + 90;
            diastolic = random.nextInt(80 - 60 + 1) + 60;
        }
        bloodPressureJSON = new JSONObject();
        try{
            bloodPressureJSON.put("systolic", systolic);
            bloodPressureJSON.put("diastolic", diastolic);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private int getHeartRate(State state){
        int critical_high = 220 - age;
        int warning_high  = critical_high - 10;
        int normal        = (int)(critical_high * 0.85);
        int critical_low  = (int)(critical_high * 0.50); 
        int warning_low   = critical_low  + 10;
        boolean lowOrHigh = random.nextBoolean();
        if(lowOrHigh){
            if(state == State.CRITICAL){
                return random.nextInt(220 - critical_high + 1) + critical_high;
            }else if(state == State.WARNING){
                return random.nextInt(critical_high - warning_high + 1) + warning_high;
            }else{
                return random.nextInt(warning_high - normal + 1) + normal; 
            }
        }else{
            if(state == State.CRITICAL){
                return random.nextInt(critical_low - 30 + 1) + 1;
            }else if(state== State.WARNING){
                return random.nextInt(warning_low - critical_low + 1) + critical_low;
            }else{
                return random.nextInt(normal - warning_low + 1) + warning_low; 
            }
        }

    }
    private void calcReading(){
        boolean criticalBloodPressure = random.nextBoolean();
        boolean criticalHeartRate = random.nextBoolean();
        if(this.state == State.CRITICAL){

            if(criticalBloodPressure){
                getBloodPressure(State.CRITICAL);
            }else
                getBloodPressure(State.NORMAL);
            if(criticalHeartRate){
                this.heartRate = getHeartRate(State.CRITICAL);
            }else
                this.heartRate = getHeartRate(State.NORMAL);
        }
        else if(this.state == State.WARNING){
            if(criticalBloodPressure){
                getBloodPressure(State.WARNING);
            }else
                getBloodPressure(State.NORMAL);
            if(criticalHeartRate){
                this.heartRate = getHeartRate(State.WARNING);
            }else
                this.heartRate = getHeartRate(State.NORMAL);
        }else{
            getBloodPressure(State.NORMAL);
            this.heartRate = getHeartRate(State.NORMAL);
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
        locationJSON = new JSONObject();
        try{
            locationJSON.put("latitude", this.latitude);
            locationJSON.put("longitude", this.longitude);
        }catch(Exception e){
            e.printStackTrace();
        }
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
        locationJSON = new JSONObject();
        try{
            locationJSON.put("latitude", this.latitude);
            locationJSON.put("longitude", this.longitude);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
