package com.mygdx.runai.states;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.runai.characters.AICharacter;

public class AISpeedBoost extends LeafTask<AICharacter> {
    public boolean playOnce;
    AICharacter aiCharacter;

    public AISpeedBoost(AICharacter aiCharacter){
        this.aiCharacter = aiCharacter;
    }

    @Override
    public Status execute() {

        if(playOnce){return Status.SUCCEEDED;}


        if(!playOnce){

            aiCharacter.aiDistance += 20 / aiCharacter.index;
            playOnce = true;

        }

        return Status.SUCCEEDED;

    }

    @Override
    protected Task<AICharacter> copyTo(Task<AICharacter> task) {
        return null;
    }
}
