package org.example;

import java.util.*;
import org.eclipse.paho.client.mqttv3.*;
public class App
{
    
    public static void main( String[] args )
    {

        Thread thread1 = new Thread(new Bracelet("Bracelet 1"));
        Thread thread2 = new Thread(new Bracelet("Bracelet 2"));
        Thread thread3 = new Thread(new Bracelet("Bracelet 3"));

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Main thread exiting.");
    }
}
