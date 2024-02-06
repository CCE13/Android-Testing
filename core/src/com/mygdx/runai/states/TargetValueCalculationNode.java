package com.mygdx.runai.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.runai.characters.AICharacter;

public class TargetValueCalculationNode extends LeafTask<AICharacter> {

    float targetSpeed;
    float distanceTravled;
    float distanceToShow;

    AICharacter aiCharacter;

    public TargetValueCalculationNode(AICharacter aiCharacter){
        this.aiCharacter = aiCharacter;

    }

    @Override
    public Status execute() {
        float playerDistance = aiCharacter.playerDistance;

        //Calculate AI distance traveled
        distanceTravled =  aiCharacter.aiDistance;

        //Calculate distance to show in the screen
        distanceToShow = (distanceTravled - playerDistance) * 20 ;


        aiCharacter.aiDistanceToShow = distanceToShow;


        return Status.SUCCEEDED;

    }

    @Override
    protected Task<AICharacter> copyTo(Task<AICharacter> task) {
        return null;
    }
}
