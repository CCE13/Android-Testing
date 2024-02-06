package com.mygdx.runai.characters;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.runai.Blackboard;
import com.mygdx.runai.SoundInterface;
import com.mygdx.runai.Sprite;
import com.mygdx.runai.states.AudioNode;
import com.mygdx.runai.states.MovementNode;

public class PacerAICharacter extends BaseCharacterClass {

    public Sprite pacerSprite;
    private BehaviorTree<PacerAICharacter> behaviorTree;

    public Blackboard blackboard = new Blackboard(this);

    public float aiDistance;
    public float aiSpeed;

    public float playerDistance;



    public PacerAICharacter(SoundInterface soundInterface, PlayerCharacter playerCharacter, int pacerMinute, int paceSecond){

        blackboard.playerCharacter = playerCharacter;
        blackboard.aiIndex = 0;
        //Texture texture = RunAI.assetManager.get("bad.png", Texture.class);
        UpdatePacerPace(pacerMinute, paceSecond);
        pacerSprite = new Sprite( 600 , 500);

        setRunnerValues(aiSpeed, aiDistance, 0, pacerSprite, 0);

        behaviorTree = new BehaviorTree<PacerAICharacter>(
            new Sequence<PacerAICharacter>(
                    new MovementNode(pacerSprite, 1000, blackboard),
                    new AudioNode(blackboard,false, false, soundInterface, true,true),
                    new Success<PacerAICharacter>()
            )
        );
    }
    public void UpdatePacerPace(int paceMinutes,int paceSeconds){
        aiSpeed = paceToMps(paceMinutes, paceSeconds);
    }

    @Override
    public void update(float playerDistance, float currentTimeInRun) {
        TimeTaken ++;

        setRunnerValues(aiSpeed,aiDistance,1, pacerSprite, 0);

        blackboard.targetPos = getTargetPos();
        blackboard.timeTaken = TimeTaken;
        aiDistance += aiSpeed;

        distanceTravelled = aiDistance;


        this.playerDistance = playerDistance;

        behaviorTree.step();

        super.update(playerDistance, currentTimeInRun);
    }


    public Vector2 getTargetPos(){
        float difference = aiDistance - playerDistance;
        float distToShow = difference * 20;
        Vector2 targetPos = new Vector2(pacerSprite.getPosition().x, 500 + distToShow);

        return targetPos;
    }

    public float paceToMps(int minutes, int seconds){
        int totalSeconds = minutes * 60 + seconds;
        float paceInMetersPerSecond = 1000f / totalSeconds;
        return paceInMetersPerSecond;
    }
    public void dispose() {
        pacerSprite = null;
        behaviorTree.cancel();
    }


}
