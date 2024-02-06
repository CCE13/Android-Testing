package com.mygdx.runai;

public class AudioVariables {
    public float volume;
    public float pan;
    public boolean playSound;

    //This class is used by audio scripts so that its easier to get the values
    public AudioVariables(float volume,float pan,boolean playSound ){
        this.volume = volume;
        this.pan = pan;
        this.playSound = playSound;
    }
}
