package com.mygdx.runai;


import com.badlogic.gdx.math.Vector2;
import com.mygdx.runai.characters.AICharacter;
import com.mygdx.runai.characters.BaseCharacterClass;
import com.mygdx.runai.characters.PlayerCharacter;

public class Blackboard {

    public Vector2 targetPos;

    public Vector2 playerPosition;
    //public Sprite aiSprite;

    public int aiIndex;

    public PlayerCharacter playerCharacter;

    public BaseCharacterClass mainCharacter;

    public AICharacter aiCharacter;

    public float timeTaken = 0;

    public Blackboard(BaseCharacterClass mainCharacter){
        this.mainCharacter = mainCharacter;

    }



}
