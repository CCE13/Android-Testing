package com.abk.distance.services;


import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.abk.gps_forground.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.asidik.jfmod.Fmod3DAttributes;
import com.asidik.jfmod.FmodAndroidInit;
import com.asidik.jfmod.FmodEventDescription;
import com.asidik.jfmod.FmodEventInstance;
import com.asidik.jfmod.FmodStudio;
import com.mygdx.runai.Blackboard;
import com.mygdx.runai.LibGDX;
import com.mygdx.runai.SoundInterface;
import com.mygdx.runai.characters.AICharacter;
import com.mygdx.runai.characters.BaseCharacterClass;
import com.mygdx.runai.characters.PacerAICharacter;
import com.mygdx.runai.characters.PlayerCharacter;


public class SoundController implements SoundInterface {


    private FmodStudio fmodStudio;
    private Fmod3DAttributes listenerAttributes;
    private Fmod3DAttributes eventInstanceAttributes = new Fmod3DAttributes();
    //private Fmod3DAttributes eventInstanceAttributes = new Fmod3DAttributes();


    private Map<Integer, Map<String, FmodEventInstance>> aiSoundInstances = new HashMap<>();


    final int[] placementAudioResource = {R.raw.first, R.raw.second, R.raw.third, R.raw.fourth, R.raw.fifth, R.raw.sixth, R.raw.seventh, R.raw.eighth, R.raw.nineth, R.raw.tenth};
    private Map<Integer, Integer> placementSoundIds = new HashMap<>();


    SoundPool soundPool;
    AudioAttributes audioAttributes;

    FmodEventInstance currentInstance;

    Map<Integer, Float> aiDifference = new HashMap<>();
    Map<Float, AICharacter> mapOfAiCharacter = new HashMap<>();

    boolean runOnce;
    FmodEventInstance pacerEventInstance;

    AITextToSpeech aiTextToSpeech;
    public SoundController(Context context, AITextToSpeech aiTextToSpeech){
        this.aiTextToSpeech = aiTextToSpeech;
        FmodAndroidInit.init(context.getApplicationContext());



        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME) //sets the use of the audio if alarm play in alaram
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();


        fmodStudio = new FmodStudio(true);

        fmodStudio.loadBank("Mobile/Master.bank");
        fmodStudio.loadBank("Mobile/Footstep.bank");
        fmodStudio.loadBank("Mobile/Voice.bank");
        fmodStudio.loadBank("Mobile/Master.strings.bank");


        listenerAttributes = new Fmod3DAttributes();
        fmodStudio.setListenerAttributes(0, listenerAttributes);
        aiSoundInstances.clear();


        for(int i = 0; i < placementAudioResource.length; i++ ){
            int soundId = soundPool.load(context, placementAudioResource[i], 1);
            placementSoundIds.put(i + 1 , soundId);
        }


    }




    @Override
    public void playPacerAudio(PlayerCharacter playerCharacter, Blackboard blackboard, PacerAICharacter pacerAICharacter) {
        playAudioPacer(playerCharacter, blackboard);
        //playAudio2(playerCharacter, blackboard);
    }


    @Override
    public void PlayRandomMarathonAudio(PlayerCharacter playerCharacter, Blackboard blackboard) {

        //Find teh nearest AI and plays its footstep audio
        FindNearestPlayer(playerCharacter, blackboard);
    }


    public void FindNearestPlayer(PlayerCharacter playerCharacter, Blackboard blackboard){
        float volume = LibGDX.staticFootstepVolumeValue;

        if(runOnce == false){

            FmodEventDescription wave = fmodStudio.getEvent("event:/PlayerFootsteps");
            currentInstance = wave.createInstance();
            currentInstance.setVolume(volume);
            currentInstance.start();
            runOnce = true;
        }
        AICharacter aiCharacter = blackboard.aiCharacter;

        currentInstance.setVolume(volume);
        if(aiDifference.size() != 9){

            //Gets the difference between the AI and the player
            float difference = Math.abs(playerCharacter.distanceTravelled - aiCharacter.distanceTravelled);
            int index = aiCharacter.index;

            //Put them in a map
            aiDifference.put(index, difference);
            mapOfAiCharacter.put(difference, aiCharacter);


            return;

        }

        //the
        float cloasestAI = Collections.min(aiDifference.values());

        if(aiCharacter == mapOfAiCharacter.get(cloasestAI)){
            eventInstanceAttributes = new Fmod3DAttributes();

            float xValue;

            if(playerCharacter.playerSprite.getPosition().x  - aiCharacter.aiSprite.getPosition().x > 0){
                xValue = 2;


            }else{
                xValue = -2;
            }

            //Update the FMod listener attributes
            listenerAttributes.position.set(0, 0, playerCharacter.distanceTravelled);
            listenerAttributes.velocity.set(0,0,0);
            fmodStudio.setListenerAttributes(0,listenerAttributes);

            eventInstanceAttributes.position.set(xValue ,0,aiCharacter.distanceTravelled);
            eventInstanceAttributes.velocity.set(0,0,0);
            currentInstance.set3DAttributes(eventInstanceAttributes);

            aiDifference.clear();
            mapOfAiCharacter.clear();

        }

        fmodStudio.update();



    }



    private void playAudioPacer(PlayerCharacter playerCharacter, Blackboard blackboard) {

        BaseCharacterClass runner = blackboard.mainCharacter;
        Fmod3DAttributes eventInstanceAttrat = new Fmod3DAttributes();
        float volume = LibGDX.staticFootstepVolumeValue;

        if(runOnce == false){

            FmodEventDescription wave = fmodStudio.getEvent("event:/PlayerFootsteps");
            pacerEventInstance = wave.createInstance();
            pacerEventInstance.start();
            pacerEventInstance.setVolume(volume);

            runOnce = true;
        }

        pacerEventInstance.setVolume(volume);

        pacerEventInstance.setPitch(runner.runnerSpeedMultiplyer);

        //Update the FMod listener attributes
        listenerAttributes.position.set(0,0,  playerCharacter.distanceTravelled);
        listenerAttributes.velocity.set(0,0,0);
        fmodStudio.setListenerAttributes(0,listenerAttributes);

        //Update the FMOD eventInstance attributes to be synced wiht the AI positon
        eventInstanceAttrat.position.set(0 ,0,runner.runnerDistanceTravelled);
        eventInstanceAttrat.velocity.set(0,0,0);

        //eventInstance.set
        pacerEventInstance.set3DAttributes(eventInstanceAttrat);


        //Update FMOD
        fmodStudio.update();
    }
    @Override
    public void PlayPlacementAudio(){

        int placement = LibGDX.playerPlacement;
        float volume = LibGDX.staticVoiceVolumeValue;
        if(placementSoundIds.containsKey(placement)){
            int soundID = placementSoundIds.get(placement);
            soundPool.play(soundID,volume,volume, 1,0,1);
        }




    }

    @Override
    public void playTTSFeedback(String text) {
        aiTextToSpeech.playThisText(text, LibGDX.staticFeedbackVolumeValue);
    }

    @Override
    public void StopMediaPlayer() {
        fmodStudio.mixerSuspend();
    }

    @Override
    public void disposeSoundinterface() {

        aiTextToSpeech.Dispose();
        aiSoundInstances.clear();
        fmodStudio.release();
    }



}
