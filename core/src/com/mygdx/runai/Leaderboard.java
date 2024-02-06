package com.mygdx.runai;

import com.mygdx.runai.characters.BaseCharacterClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {
    private List<BaseCharacterClass> runnerList;

    public Leaderboard(){
        runnerList = new ArrayList<>();
    }


    public void addRunner(BaseCharacterClass runner){
        runnerList.add(runner);

    }

    public void sortAIByYAxis(){
        Collections.sort(runnerList, new Comparator<BaseCharacterClass>() {
            @Override
            public int compare(BaseCharacterClass ai1, BaseCharacterClass ai2) {
                return Float.compare(ai2.distanceTravelled, ai1.distanceTravelled);
            }
        });


    }

    public int getPlayerPlacement(){
        sortAIByYAxis(); // make sure the list is sorted
        for(int i = 0; i < runnerList.size(); i++){
            if(runnerList.get(i).isPlayer){

                return i + 1; // we add one because list indices start from 0
            }

        }
        return -1; // return -1 if the player is not found in the list
    }
    public List<BaseCharacterClass> getAICharacters(){
        return runnerList;
    }

}
