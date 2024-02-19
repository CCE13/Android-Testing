package com.abk.distance.services;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.Locale;
import java.util.Set;

public class AITextToSpeech {
    TextToSpeech tts;

    public AITextToSpeech(Context context){
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.ENGLISH);
                    tts.setPitch(0.95f);
                    tts.setSpeechRate(0.97f);
                }
            }
        });

    }
    public void PauseTTSpeech(){
        tts.stop();
    }
    public void Dispose(){
        tts.stop();
        tts.shutdown();
    }

    public void playThisText(String text, float volume){
        Bundle params = new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,volume);
        tts.speak(text, TextToSpeech.QUEUE_ADD, params, null);
    }
    public void playSilence(long durationInMs){
        // The duration of the pause (in milliseconds)

// The utterance ID
        String silenceUtteranceId = "Silence";

// Request the TTS engine to play a period of silence
        tts.playSilentUtterance(durationInMs, TextToSpeech.QUEUE_ADD, silenceUtteranceId);
    }
}
