package com.abk.distance.services;


import com.mygdx.runai.RunAIInterface;
import com.mygdx.runai.SoundInterface;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AIHandler {
    private RunAIInterface game;
    private SoundInterface soundInterface;
    public ArrayList<Float> aiDistanceTravelled;
    public ArrayList<Integer> aiTime;

    public AIHandler(RunAIInterface game, SoundInterface soundInterface) {
        this.game = game;
        this.soundInterface = soundInterface;
        aiDistanceTravelled = new ArrayList<>();
        aiTime = new ArrayList<>();
    }

    public void StartTheAI()
    {
        game.CreateAndRunAI();
        aiDistanceTravelled = game.AIDistances();
    }
    public void RunTheAI(){
            aiDistanceTravelled = game.RunTheAI();

    }
    public void PingTheAI(){
        aiTime = game.GetTheTime();
        aiDistanceTravelled = game.AIDistances();
    }

    public void stopRunning() {
        aiDistanceTravelled.clear();;
        aiTime.clear();
        game = null;
    }
}





