package com.mygdx.runai.states;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Task;
import com.mygdx.runai.characters.AICharacter;

public class SucessFailDecorator extends BranchTask<AICharacter> {

    private float rankIndex;

    public SucessFailDecorator(float rankIndex){
        this.rankIndex = rankIndex;
    }

    @Override
    public int getChildCount(){
        return 2;
    }

    @Override
    public void run() {
        // Run the first child if value is 1, run the second child if value is 2
        if (rankIndex <= 0f) {
            Task<AICharacter> child = getChild(0);
            child.setControl(this);
            child.start();


            if (child.checkGuard(this)) {
                child.run();
            } else {
                child.fail();
            }

        } else {
            Task<AICharacter> child = getChild( 1);


            child.setControl(this);
            child.start();


            if (child.checkGuard(this)) {
                child.run();
            } else {
                child.fail();
            }
        }

    }

    @Override
    public void childSuccess(Task<AICharacter> task) {

        control.childSuccess(this);

    }

    @Override
    public void childFail(Task<AICharacter> task) {
        control.childFail(this);

    }

    @Override
    public void childRunning(Task<AICharacter> runningTask, Task<AICharacter> reporter) {
        //if a child fails the branch task also fails
        control.childRunning(runningTask, reporter);

    }
}
