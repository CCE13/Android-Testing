package com.mygdx.runai.gamemodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.mygdx.runai.SoundInterface;
import com.mygdx.runai.Sprite;
import com.mygdx.runai.characters.BaseCharacterClass;
import com.mygdx.runai.characters.PacerAICharacter;
import com.mygdx.runai.characters.PlayerCharacter;

public class PacerMode extends Mode{

    private SoundInterface soundInterface;


    Sprite playerSprite;
    Vector2 playerPosition;

    int paceMinute;
    int paceSecond;


    public PacerMode(SoundInterface soundInterface, Texture playerTexture, int paceMinute, int paceSecond){
        this.soundInterface = soundInterface;
        this.paceMinute = paceMinute;
        this.paceSecond = paceSecond;

        create(playerTexture);
    }


    private void create(Texture playerTexture){

        //Creates the AI and the player character
        runners = new PacerAICharacter[1];

        System.out.println( "pace" + paceMinute + "pace sec" + paceSecond);
        playerCharacter = new PlayerCharacter(playerTexture);

        //Cache the playersprite value to render later
        playerSprite = playerCharacter.playerSprite;
        playerPosition = playerCharacter.playerSprite.getPosition();

        playerCharacter = new PlayerCharacter(playerTexture);
        //Sets all the contructor values of the AI character
        for (int i = 0; i < runners.length; i++){
            runners[i] = new PacerAICharacter(soundInterface, playerCharacter, paceMinute, paceSecond);
        }
    }

    public void UpdatePacerPace(int paceMinute,int paceSeconds){
       PacerAICharacter pacer =  (PacerAICharacter)runners[0];
       pacer.UpdatePacerPace(paceMinute,paceSeconds);
    }

    public void update(float currentTimeInRun, float playerDistanceRan) {
        for(BaseCharacterClass aiCharacter: runners){
            aiCharacter.update(playerDistanceRan, currentTimeInRun);
        }

        playerCharacter.update(playerDistanceRan,currentTimeInRun);
        super.update(currentTimeInRun, playerDistanceRan);
    }




}

