package com.mygdx.runai;

import com.mygdx.runai.characters.PacerAICharacter;
import com.mygdx.runai.characters.PlayerCharacter;

public interface SoundInterface {

    public void playPacerAudio(PlayerCharacter playerCharacter, Blackboard blackboard, PacerAICharacter pacerAICharacter);
    public void PlayRandomMarathonAudio(PlayerCharacter playerCharacter, Blackboard blackboard);

    public void PlayPlacementAudio();

    public void playTTSFeedback(String text);

    public void StopMediaPlayer();


    public void disposeSoundinterface();
}
