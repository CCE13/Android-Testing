package com.abk.distance.services;

import java.io.Serializable;
import java.util.ArrayList;

public class MapSimulationPointData implements Serializable {
    public int PlayerTime;
    public int PaceMin;
    public int PaceSecond;
    public double PlayerDistance;
    public ArrayList<Float> AIDistance;

    public MapSimulationPointData(int playerTime,double playerDistance,ArrayList<Float> AIDistance,int paceMin,int paceSecond){
        this.PlayerTime = playerTime;
        this.PlayerDistance = playerDistance;
        this.AIDistance = AIDistance;
        this.PaceMin = paceMin;
        this.PaceSecond = paceSecond;

    }
}
