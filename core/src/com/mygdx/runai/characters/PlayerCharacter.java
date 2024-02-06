package com.mygdx.runai.characters;


import com.badlogic.gdx.graphics.Texture;
import com.mygdx.runai.Sprite;

public class PlayerCharacter extends BaseCharacterClass{

    public Sprite playerSprite;

    public static Integer playerPlacement;



    public PlayerCharacter(Texture texture){
        Name = "Player";
        isPlayer = true;
        playerSprite = new Sprite( 425,500 );

    }


    @Override
    public void update(float playerDistance, float currentTimeInRun) {

        distanceTravelled = playerDistance;


    }
}
