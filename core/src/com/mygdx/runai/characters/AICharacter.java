package com.mygdx.runai.characters;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.runai.Blackboard;
import com.mygdx.runai.PlayerDataInterface;
import com.mygdx.runai.SoundInterface;
import com.mygdx.runai.Sprite;
import com.mygdx.runai.states.AISpeedBoost;
import com.mygdx.runai.states.AudioNode;
import com.mygdx.runai.states.ChangeSpeedNode;
import com.mygdx.runai.states.CooldownDecorator;
import com.mygdx.runai.states.DistanceSpeedBoost;
import com.mygdx.runai.states.LoadRunNode;
import com.mygdx.runai.states.MovementNode;
import com.mygdx.runai.states.SpeedIndexCalculator;
import com.mygdx.runai.states.SucessFailDecorator;
import com.mygdx.runai.states.TargetValueCalculationNode;

import java.util.HashSet;
import java.util.Set;

public class AICharacter extends BaseCharacterClass {

    private  PlayerDataInterface playerDataInterface;
    private BehaviorTree<AICharacter> behaviorTree;
    public Sprite aiSprite;

    public float playerSpeed = 1;
    public float playerDistance = 1;

    public float speedMultiplyer;
    public float aiDistanceToShow;


    public float aiDistance = 1;
    public float aiSpeed;


    public static float playerTestSpeed = 10;

    public static Set<Integer> speedIndexList = new HashSet<>();

    public int index;
    public int speedIndex = 1;

    public float halfAttentuation;

    public SoundInterface soundInterface;
    public float savedSpeed;

    public float volumeValue = 0.5f;

    public static float S_PlayerDistance;

    public Blackboard blackboard = new Blackboard(this);
    public float distanceToTravel;




    public AICharacter(int index,  float x, float y, SoundInterface soundInterface, PlayerCharacter playerCharacter, PlayerDataInterface playerDataInterface){
        aiSprite = new Sprite( x,y);

        runnerSprite = aiSprite;
        //this.playerCharacter = playerCharacter;
        this.playerDataInterface = playerDataInterface;
        blackboard.mainCharacter = this;
        blackboard.playerCharacter = playerCharacter;
        blackboard.aiCharacter = this;
        blackboard.aiIndex = index;

        blackboard.targetPos = GetTargetPos();


        Name = "AI";
        isPlayer = false;
        this.index = index;
        this.soundInterface = soundInterface;



        Sequence<AICharacter> successSequence = new Sequence<AICharacter>(

                new CooldownDecorator<AICharacter>(new SpeedIndexCalculator(this, 0), 7,this),
                new AISpeedBoost(this),
                new ChangeSpeedNode(this, 0),
                new MovementNode(aiSprite,10, blackboard),
                new TargetValueCalculationNode(this),
                new AudioNode<AICharacter>(blackboard, false, false, soundInterface, false, false),
                new DistanceSpeedBoost(this),
                new CooldownDecorator<AICharacter>(new AudioNode(blackboard, false, true, soundInterface, false, true), 1, this),
                new CooldownDecorator<AICharacter>(new AudioNode(blackboard, true, true, soundInterface, false, false), 3, this)
        );


        Sequence<AICharacter> failureSequence = new Sequence<AICharacter>(

                new CooldownDecorator<AICharacter>(new SpeedIndexCalculator(this, playerDataInterface.getRankData()), 7,this),
                new LoadRunNode(this, playerDataInterface.getRankData()),
                new AISpeedBoost(this),
                new ChangeSpeedNode(this, playerDataInterface.getRankData()),
                new TargetValueCalculationNode(this),
                new MovementNode<AICharacter>(aiSprite, 1000, blackboard ),
                new AudioNode<AICharacter>(blackboard, false,false,soundInterface,false, false),
                new CooldownDecorator<AICharacter>(new AudioNode(blackboard, false, true, soundInterface, false, true), 1, this),
                new CooldownDecorator<AICharacter>(new AudioNode(blackboard, true, true, soundInterface, false, false), 3, this)
        );

        SucessFailDecorator branchNode = new SucessFailDecorator(playerDataInterface.getRankData());
        branchNode.addChild(successSequence);
        branchNode.addChild(failureSequence);


        //Main behvaiour tree branch
        behaviorTree = new BehaviorTree<AICharacter>(
                new Sequence<AICharacter>(
                        new Invert<AICharacter>(
                                branchNode
                        ),

                        new Success<AICharacter>()
                )

        );
    }


    @Override
    public void update(float playerDistance, float currentTimeInRun) {
        //check if the ai has travelled
        blackboard.targetPos = GetTargetPos();


        TimeTaken ++;

        blackboard.timeTaken = TimeTaken;

        behaviorTree.step();

        //Sets the speed of the AI based on the players speed
        //TODO Change this when ur done testing
        this.playerDistance = Float.isNaN(playerDistance) || playerDistance == 0 ? (float) 1.0 : playerDistance;

        S_PlayerDistance = playerDistance;

        playerSpeed = playerDistance / TimeTaken;



        if(!Float.isNaN(aiSpeed)){
            aiDistance += aiSpeed;
        }
        distanceTravelled = aiDistance;
        super.update(playerDistance, currentTimeInRun);
    }


    public Vector2 GetTargetPos(){
        //Vector2 targetPos = new Vector2(aiSprite.getPosition().x, 500 + speed);
        Vector2 targetPos = new Vector2(aiSprite.getPosition().x, 500 + aiDistanceToShow);

        return targetPos;
    }
    public void dispose() {
        S_PlayerDistance = 0;
        aiSprite = null;
        behaviorTree.cancel();
        speedIndexList.clear();
    }
}
