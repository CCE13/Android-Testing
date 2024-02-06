package com.mygdx.runai.gamemodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.runai.Leaderboard;
import com.mygdx.runai.PlayerDataInterface;
import com.mygdx.runai.SoundInterface;
import com.mygdx.runai.Sprite;
import com.mygdx.runai.characters.AICharacter;
import com.mygdx.runai.characters.BaseCharacterClass;
import com.mygdx.runai.characters.PlayerCharacter;

import java.util.ArrayList;
import java.util.List;

public class RankMode extends Mode {



    Sprite playerSprite;
    Vector2 playerPosition;

    private List<Sprite> enemySprites = new ArrayList<>();

    public RankMode(SoundInterface soundInterface, Leaderboard leaderboard, Texture playerTexture, PlayerDataInterface playerDataInterface){
       create(soundInterface, leaderboard, playerTexture, playerDataInterface);
    }

    public void create(SoundInterface soundInterface, Leaderboard leaderboard, Texture playerTexture, PlayerDataInterface playerDataInterface){

        //Creates the AI and the player character
        runners = new AICharacter[9];
        playerCharacter = new PlayerCharacter(playerTexture);

        //Cache the playersprite value to render later
        playerSprite = playerCharacter.playerSprite;

        playerPosition = playerCharacter.playerSprite.getPosition();


        //Sets all the contructor values of the AI character
        for (int i = 0; i < runners.length; i++){
            runners[i] = new AICharacter(i + 1, i * 100, 0, soundInterface, playerCharacter, playerDataInterface);
            //aiCharacters[i] = new AICharacter(i, enemyTexture,  500, 500);
            enemySprites.add( runners[i].runnerSprite);
            leaderboard.addRunner(runners[i]);
            runners[i].runnerSprite.setPosition( randomizeXValue(i), 400);
        }

        leaderboard.addRunner(playerCharacter);
    }

    public void update(float currentTimeInRun, float playerDistanceRan) {

        for(BaseCharacterClass aiCharacter: runners){
            aiCharacter.update(playerDistanceRan, currentTimeInRun);
        }

        playerCharacter.update(playerDistanceRan, currentTimeInRun);
        super.update(currentTimeInRun, playerDistanceRan);
    }



    public float randomizeXValue(int index){
        //Sets the position based on the index of the list
        float minusValue = 50 * index;
        //Makes sure that the x value is not to far from the player
        float xValue = 700 - minusValue;
        return xValue;
    }
}
