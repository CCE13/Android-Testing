package com.mygdx.runai.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.runai.characters.AICharacter;

public class DebugNode extends LeafTask<AICharacter> {

    String stringToDebug;

    public DebugNode(String stringDesired){
        stringToDebug = stringDesired;
    }

    @Override
    public Status execute(){

        System.out.println("Debug " + stringToDebug);
        return Status.SUCCEEDED;
    }

    @Override
    protected Task<AICharacter> copyTo(Task<AICharacter> task) {
        return null;
    }
}
