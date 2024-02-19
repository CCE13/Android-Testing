package com.abk.distance;

import com.mygdx.runai.PlayerDataInterface;

public class PlayerDataInterfaceClass implements PlayerDataInterface {
    private float rankIndex = 1.1f;
    private int currentTimeInRun;
    private float playerDistanceToTravel;

    private int currentPaceMin;
    private int currentPaceSecond;
    private double distanceTravelled;
    private String name;


    public PlayerDataInterfaceClass(float rankIndex, int currentTimeInRun,float playerDistanceToTravel,String playerName)
    {
        this.rankIndex = rankIndex;
        this.currentTimeInRun = currentTimeInRun;
        this.playerDistanceToTravel = playerDistanceToTravel;
        this.name = playerName;
    }
    public void SetPlayerData(int currentTimeInRun,int currentPaceMin,int currentPaceSecond,double distanceTravelled)
    {
        this.currentTimeInRun = currentTimeInRun;
        this.currentPaceMin = currentPaceMin;
        this.currentPaceSecond = currentPaceSecond;
        this.distanceTravelled = distanceTravelled;
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
