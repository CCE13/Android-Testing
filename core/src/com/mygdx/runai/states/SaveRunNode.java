package com.mygdx.runai.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.files.FileHandle;
import com.mygdx.runai.characters.AICharacter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveRunNode extends LeafTask<AICharacter> {
    AICharacter aiCharacter;

    public SaveRunNode(AICharacter aiCharacter){

        this.aiCharacter = aiCharacter;

    }
    @Override
    public Status execute() {
        float speed = aiCharacter.aiSpeed;
        int index = aiCharacter.index;

        saveValuesInFile(index, speed);


        return Status.SUCCEEDED;
    }

    @Override
    protected Task<AICharacter> copyTo(Task<AICharacter> task) {
        return null;
    }


    private void saveValuesInFile(int aiIndex, float aiSpeed) {
        try {
            File file = new File("/data/user/0/com.runai.RunAI/files/", aiIndex+".txt");
            FileWriter writer = new FileWriter(file);

            writer.write("RunningSpeed: " + aiSpeed);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred saving the file.");
            e.printStackTrace();
        }
    }

}
