package com.mygdx.runai.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.runai.DataManager;
import com.mygdx.runai.characters.AICharacter;

import java.util.HashMap;

public class LoadRunNode extends LeafTask<AICharacter> {
    private AICharacter aiCharacter;
    private float rankIndex;
    HashMap<String, Double> speeds = new HashMap<>();

    public LoadRunNode(AICharacter aiCharacter, float rankIndex){
        this.aiCharacter = aiCharacter;
        this.rankIndex = rankIndex;

        speeds.put("1.1", 1.90);
        speeds.put("1.2", 1.96);
        speeds.put("1.3", 2.02);
        speeds.put("1.4", 2.08);
        speeds.put("2.1", 2.15);
        speeds.put("2.2", 2.22);
        speeds.put("2.3", 2.30);
        speeds.put("2.4", 2.38);
        speeds.put("3.1", 2.47);
        speeds.put("3.2", 2.56);
        speeds.put("3.3", 2.67);
        speeds.put("3.4", 2.78);
        speeds.put("4.1", 2.90);
        speeds.put("4.2", 3.03);
        speeds.put("4.3", 3.17);
        speeds.put("4.4", 3.33);
        speeds.put("5.1", 3.51);
        speeds.put("5.2", 3.70);
        speeds.put("5.3", 3.92);
        speeds.put("5.4", 4.17);
        speeds.put("6.1", 4.44);
        speeds.put("6.2", 4.76);
        speeds.put("6.3", 5.13);
        speeds.put("6.4", 5.56);

    }

    @Override
    public Status execute() {
        String rankId = Float.toString(rankIndex);

        float speed = speeds.get(rankId).floatValue();

        aiCharacter.savedSpeed = speed;

        return Status.SUCCEEDED;


    }

    @Override
    protected Task<AICharacter> copyTo(Task<AICharacter> task) {
        return null;
    }
}
