package com.mygdx.runai.states;

import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.runai.characters.AICharacter;


public class CooldownDecorator<E> extends Decorator<E> {

    private float cooldownTime;
    private float cachedTime = 0;
    private float actualTimer;
    private AICharacter aiCharacter;

    private boolean firstTime;
    int count;



    public CooldownDecorator(Task<E> child, float cooldownTime, AICharacter aiCharacter){
        super(child);
        this.cooldownTime = cooldownTime;
        this.aiCharacter = aiCharacter;

    }

    @Override
    public void start() {
        super.start();

    }



    public void run(){
        child.setControl(this);

        //Make the node run once before starting the cooldown
        if(firstTime == false){

            child.start();
            if (child.checkGuard(this)) {
                child.run();
                count = 0; // Reset the count after running the child task
            } else {
                child.fail();
            }
            firstTime = true;

            return;
        }

        if(count >= cooldownTime){
            child.start();
            if (child.checkGuard(this)) {
                child.run();
                count = 0; // Reset the count after running the child task
            } else {
                child.fail();
            }
        } else {
            this.control.childSuccess(this);
            count++; // Increment the count when the child task is not run
        }

    }

    @Override
    protected Task<E> copyTo(Task<E> task) {
        CooldownDecorator<E> decorator = (CooldownDecorator<E>) task;
        decorator.cooldownTime = cooldownTime;
        return super.copyTo(task);
    }
}
