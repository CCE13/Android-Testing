package com.abk.distance;

import com.mygdx.runai.PlayerDataInterface;

public class PlayerDataInterfaceClass implements PlayerDataInterface {
    public float rankIndex = 1.1f;
    public float playerDistanceMeters = 0;
    public float currentTimeInRun;
    public float playerDistanceToTravel;

    public PlayerDataInterfaceClass(float rankIndex, float playerDistanceMeters, float currentTimeInRun,float playerDistanceToTravel){
        this.rankIndex = rankIndex;
        this.playerDistanceMeters = playerDistanceMeters;
        this.currentTimeInRun = currentTimeInRun;
        this.playerDistanceToTravel = playerDistanceToTravel;

    }
    @Override
    public float getRankData() {
        return rankIndex;
    }

    @Override
    public float getPlayerDistanceToRun() {
        return playerDistanceToTravel;
    }


}
