package com.mygdx.runai.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.runai.WeightedProbablity;
import com.mygdx.runai.characters.AICharacter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SpeedIndexCalculator  extends LeafTask<AICharacter> {
    AICharacter aiCharacter;

    boolean runOnce;
    Random rand = new Random();
    float saveIndex;
    float rankId;

    public SpeedIndexCalculator(AICharacter aiCharacter, float rankId){
        this.aiCharacter = aiCharacter;
        this.rankId = rankId;

    }
    @Override
    public Status execute() {

        if (runOnce){
            float mutiplyer = aiCharacter.speedMultiplyer;
            float newRandomValue = randomiseFloat(mutiplyer);
            aiCharacter.speedMultiplyer = newRandomValue;
            return Status.SUCCEEDED;
        }

        aiCharacter.speedMultiplyer = getRandomNumber();
        runOnce = true;
        return Status.SUCCEEDED;

    }

    @Override
    protected Task<AICharacter> copyTo(Task<AICharacter> task) {
        return null;
    }

    private float getRandomNumber(){


        int index;
        Set<Integer> numberList = AICharacter.speedIndexList;

        while (true){
            index = MathUtils.random(1, 10);

            if(!numberList.contains(index)) {

                AICharacter.speedIndexList.add(index);
                aiCharacter.speedIndex = index;
                float speedMultiplyer = 0;

                if (rankId == 0) {
                    speedMultiplyer = indexToCurve(index);
                } else {
                    speedMultiplyer = indexToCurveRank(index);
                }
                return speedMultiplyer;
            }
        }



    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public float indexToCurve(int index){

        float speedMultiplyer = 0;

        switch(index){
            case(1):
                speedMultiplyer =  0.90f;
                break;
            case(2):
                speedMultiplyer = 0.95f;
                break;
            case(3):
                speedMultiplyer =  0.98f;
                break;
            case(4):
                speedMultiplyer = 1.01f;
                break;
            case(5):
                speedMultiplyer = 1.02f;
                break;
            case(6):
                speedMultiplyer = 1.03f;
                break;
            case(7):
                speedMultiplyer = 1.04f;
                break;
            case(8):
                speedMultiplyer = 1.05f;
                break;
            case(9):
                speedMultiplyer = 1.06f;
                break;
            case(10):
                speedMultiplyer = 1.08f;
                break;
        }



        return  speedMultiplyer;




    }

    public float indexToCurveRank(int index){

        float speedMultiplyer = 0;

        switch(index){
            case(1):
                speedMultiplyer =  0.93f;
                break;
            case(2):
                speedMultiplyer = 0.95f;
                break;
            case(3):
                speedMultiplyer =  0.98f;
                break;
            case(4):
                speedMultiplyer = 0.99f;
                break;
            case(5):
                speedMultiplyer = 1.00f;
                break;
            case(6):
                speedMultiplyer = 1.01f;
                break;
            case(7):
                speedMultiplyer = 1.02f;
                break;
            case(8):
                speedMultiplyer = 1.03f;
                break;
            case(9):
                speedMultiplyer = 1.04f;
                break;
            case(10):
                speedMultiplyer = 1.05f;
                break;
        }



        return  speedMultiplyer;




    }
    public float randomiseFloat(float multiplyer){

        //Creates a map of different probabilty values
        WeightedProbablity weightedProbablity = new WeightedProbablity();
        weightedProbablity.addNumber(-4, 1 * multiplyer);
        weightedProbablity.addNumber(-3, 2 * multiplyer);
        weightedProbablity.addNumber(-2, 3 * multiplyer);
        weightedProbablity.addNumber(-1, 4 * multiplyer);
        weightedProbablity.addNumber(0, 5 / multiplyer);
        weightedProbablity.addNumber(1, 4 / multiplyer);
        weightedProbablity.addNumber(2, 3 / multiplyer);
        weightedProbablity.addNumber(3, 2 / multiplyer);
        weightedProbablity.addNumber(4, 1 / multiplyer);

        //Gets a random value based on the current speed multiplyer
        int rand = weightedProbablity.nextNum();

        //Make the number smaller to add to the multiplyer
        float randDec = (float) rand/1000;


        multiplyer += randDec;

        return multiplyer;


    }



}
