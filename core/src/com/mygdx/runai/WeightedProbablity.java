package com.mygdx.runai;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedProbablity {
    private NavigableMap<Double, Integer> map = new TreeMap<>();
    private Random random;

    //the total percentage amount
    private double total = 0;

    public WeightedProbablity(){
        this.random = new Random();

    }

    public void addNumber(int number, double probability){
        if(probability <= 0) return;
        total += probability;
        map.put(total, number);

    }

    public int nextNum(){
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
