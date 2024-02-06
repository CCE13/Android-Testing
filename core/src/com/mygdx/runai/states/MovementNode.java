package com.mygdx.runai.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.runai.Blackboard;
import com.mygdx.runai.Sprite;


public class MovementNode<T> extends LeafTask<T> {
    Sprite sprite;
    float velocityX;
    float velocityY;

    private Vector2 startPos;
    private float speed;
    public Blackboard blackboard;


    public MovementNode(Sprite sprite, float speed, Blackboard blackboard){
        this.sprite = sprite;
        this.speed = speed;
        this.blackboard = blackboard;

    }





    @Override
    public Status execute() {

        Vector2 targetPos = blackboard.targetPos;
        float currentTimeInRun = blackboard.timeTaken;
        this.startPos = new Vector2(sprite.getPosition());

        // Calculate the direction to the target
        Vector2 direction = targetPos.cpy().sub(startPos).nor();

        // Calculate the distance to the target
        float distance = startPos.dst(targetPos);

        // Calculate the distance to be traveled in this frame
        float distanceToTravel = speed * currentTimeInRun;


        // Check if the remaining distance is less than the distance to be traveled in this frame
        if (distance < distanceToTravel) {
            sprite.setPosition(sprite.getPosition().x, targetPos.y);
            sprite.setVelocity(0, 0);

            return Status.SUCCEEDED;
        }

        // Calculate the movement along the Y-axis
        float yMovement = direction.y * distanceToTravel;

        // Update the position only on the Y-axis
        sprite.setPosition(sprite.getPosition().x, sprite.getPosition().y + yMovement);
        sprite.update(currentTimeInRun);

        return Status.SUCCEEDED;

    }



    @Override
    protected Task<T> copyTo(Task<T> task) {
        return null;
    }
}
