package com.mygdx.runai.states;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.runai.Blackboard;
import com.mygdx.runai.LibGDX;
import com.mygdx.runai.SoundInterface;
import com.mygdx.runai.characters.PacerAICharacter;

public class AudioNode<T> extends LeafTask<T> {

    //Audio variables for the 3D Audio
    Vector2 listner;
    Vector2 source;



    boolean onlyOneAI;

    public boolean playPlacement;
    private Blackboard blackboard;
    private SoundInterface soundInterface;

    private boolean pacer;

    private boolean textToSpeech;

    private int minutes;
    private int seconds;

    float distanceThreshold = 1000;

    int currentPlacement = 0;



    public AudioNode(Blackboard blackboard, Boolean playPlacement, boolean onlyOneAI, SoundInterface soundInterface, boolean pacer, boolean textToSpeach){
        this.playPlacement = playPlacement;
        this.blackboard = blackboard;
        this.onlyOneAI = onlyOneAI;
        this.soundInterface = soundInterface;
        this.pacer = pacer;
        this.textToSpeech = textToSpeach;
    }
    @Override
    public Status execute() {
        //Gets the listener(player) and source(AI) position
        listner = (blackboard.playerPosition != null) ? blackboard.playerPosition : Vector2.Zero;
        source = (blackboard.mainCharacter.runnerSprite != null) ? blackboard.mainCharacter.runnerSprite.getPosition() : Vector2.Zero;


        if(pacer){
            soundInterface.playPacerAudio(blackboard.playerCharacter, blackboard, (PacerAICharacter) blackboard.mainCharacter);
            return Status.SUCCEEDED;
        }


        //Check if only one ai should play it the first ai will play it
        if(onlyOneAI){
            if(blackboard.aiIndex != 1){
                return Status.SUCCEEDED;
            }
        }

        soundInterface.PlayRandomMarathonAudio(blackboard.playerCharacter, blackboard);

        if(playPlacement){

            if(currentPlacement != LibGDX.playerPlacement){

                soundInterface.PlayPlacementAudio();
                currentPlacement = LibGDX.playerPlacement;
            }

        }

        if(textToSpeech){
            float playerDistance = blackboard.aiCharacter.playerDistance;

            if(playerDistance <= distanceThreshold){
                return Status.SUCCEEDED;

            }


            distanceThreshold += 1000;
            speedToPace(blackboard.aiCharacter.aiSpeed);

            float distanceToKilometer  = Math.round((playerDistance/1000) * 10) / 10;
            String text = distanceToKilometer + " Kilometer Completed. " + " Average Pace, " + minutes + " minutes " + seconds + " seconds ";
            soundInterface.playTTSFeedback(text);
        }


        return Status.SUCCEEDED;
    }


    public void speedToPace(float meters){

        float time = blackboard.aiCharacter.TimeTaken;
        float playerDistance = blackboard.playerCharacter.distanceTravelled/1000;

        // Calculate pace in seconds per kilometer
        float paceInSecondsPerKm = time / playerDistance;

        // Convert pace to minutes and seconds
        int paceMinutes = (int) (paceInSecondsPerKm / 60);
        int paceSeconds = (int) (paceInSecondsPerKm % 60);

        //Convert settings to minutes
        minutes = paceMinutes;

        seconds = paceSeconds;

    }



    @Override
    protected Task<T> copyTo(Task<T> task) {
        return null;
    }
}
