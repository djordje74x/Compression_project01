package org.example.models;

public class FrequencyTable {
    private final long[] symbolCounts = new long[256];

    public long getCountForSymbol(int symbol){
        return symbolCounts[symbol];
    }

    public void increment(int symbol){
        symbolCounts[symbol] += 1;
    }

    public long getTotalCounts(){
        long sum = 0;
        for(long c : symbolCounts){
            sum += c;
        }
        return sum;
    }
}
