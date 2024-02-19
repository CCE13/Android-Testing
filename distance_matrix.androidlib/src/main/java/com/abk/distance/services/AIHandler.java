package com.abk.distance.services;


import com.mygdx.runai.RunAIInterface;
import com.mygdx.runai.SoundInterface;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AIHandler {
    private RunAIInterface game;
    public ArrayList<Float> aiDistanceTravelled;
    public ArrayList<Integer> aiTime;

    public AIHandler(RunAIInterface game) {
        this.game = game;
        aiDistanceTravelled = new ArrayList<>();
        aiTime = new ArrayList<>();
    }

    public void StartTheAI()
    {
        game.CreateAndRunAI();
    }
    public void RunTheAI(){
        aiDistanceTravelled = game.RunTheAI();
        aiTime = game.GetTheTime();
    }

    public void stopRunning() {
        aiDistanceTravelled.clear();;
        aiTime.clear();
        game = null;
    }
}





