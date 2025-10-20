package org.example.models;

public class Symbol {
    private final int value;
    private final long frequency;

    public Symbol(int value, long frequency){
        this.value = value;
        this.frequency = frequency;
    }

    public int getValue() {
        return value;
    }

    public long getFrequency() {
        return frequency;
    }
}
