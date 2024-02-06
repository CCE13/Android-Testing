package com.mygdx.runai.states;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.runai.characters.AICharacter;

public class DistanceSpeedBoost extends LeafTask<AICharacter> {

    AICharacter aiCharacter;

    public DistanceSpeedBoost(AICharacter aiCharacter){

        this.aiCharacter = aiCharacter;

    }


    @Override
    public Status execute() {
        //The difference between the AI distance and the player distance
        float distanceDifference = aiCharacter.playerDistance - aiCharacter.aiDistance;
        float aiIndex = aiCharacter.speedMultiplyer;

        if(distanceDifference > 100/ aiIndex){

            //Add the distance it need to cover back
            aiCharacter.aiDistance += distanceDifference;
        }


        return  Status.SUCCEEDED;

    }

    @Override
    protected Task<AICharacter> copyTo(Task<AICharacter> task) {
        return null;
    }
}
