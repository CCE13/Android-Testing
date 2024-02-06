package com.mygdx.runai.states;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.runai.characters.AICharacter;

public class ChangeSpeedNode extends LeafTask<AICharacter> {

    private AICharacter aiCharacter;
    private float rankIndex;

    public ChangeSpeedNode(AICharacter aiCharacter, float rankIndex){
        this.aiCharacter = aiCharacter;
        this.rankIndex = rankIndex;

    }
    @Override
    public Status execute() {
        float playerSpeed = aiCharacter.playerSpeed;
        float speedIndex = aiCharacter.speedMultiplyer;
        float savedSpeed = aiCharacter.savedSpeed;
        if(rankIndex != 0){
            float speed = (savedSpeed * speedIndex);
            aiCharacter.aiSpeed = speed;
            return  Status.SUCCEEDED;

        }

        float speed = (playerSpeed * speedIndex);

        aiCharacter.aiSpeed = speed;



        return Status.SUCCEEDED;

    }

    @Override
    protected Task<AICharacter> copyTo(Task<AICharacter> task) {
        ChangeSpeedNode copy = (ChangeSpeedNode) task;
        copy.aiCharacter = aiCharacter;
        return copy;
    }
}
