package org.example;

import java.util.*;


public class Bracelet implements Runnable{

    private final int MIN_VALUE = 1;
    private final int MAX_VALUE = 250;
    private static final Random random = new Random();
    private String name;
    public Bracelet(String name){
        this.name = name;
    }


    @Override
    public void run() {
        for (;;) {
            int randomValue = generateRandomValue();
            System.out.println(name + ": Generated random value: " + randomValue);
            try {
                Thread.sleep(1000); // Simulate some work being done
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int generateRandomValue() {
        return random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE;
    }



}
