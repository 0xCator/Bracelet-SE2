package org.example;
public enum State{
    NORMAL(0), WARNING(1), CRITICAL(2);
    private int value;
    private State(int value){
        this.value = value;
    }
    public int getValue(){
        return this.value;
    }

}

