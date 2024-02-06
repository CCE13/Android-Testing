package com.mygdx.runai.characters;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.runai.Sprite;

public abstract class BaseCharacterClass {

    public float distanceTravelled;
    public int position;
    public String Name;

    public boolean isPlayer;


    public float runnerSpeed;
    public float runnerDistanceTravelled;

    public Sprite runnerSprite;
    public float runnerSpeedMultiplyer;

    public int runnnerListIndex;
    public int TimeTaken;
    public Boolean ReachedTarget= false;
    public float targetDistance = 0;




    public void setRunnerValues(float speed, float distanceTravelled, float speedMultiplyer, Sprite sprite, int listIndex){
        runnerSpeed = speed;
        runnerDistanceTravelled = distanceTravelled;
        runnerSpeedMultiplyer = speedMultiplyer;
        runnerSprite = sprite;
        runnnerListIndex = listIndex;
    };


    public void update(float playerDistance, float currentTimeInRun) {

    }
    public void dispose(){

    }
}
