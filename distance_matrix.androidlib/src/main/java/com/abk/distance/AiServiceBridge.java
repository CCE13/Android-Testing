package com.abk.distance;

import android.app.Activity;
import android.content.Intent;

import com.abk.distance.chatgptscript.AiForegroundService;
import com.abk.distance.chatgptscript.MessageEvent;
import com.abk.distance.utils.AiUnityCallbacks;
import com.abk.distance.utils.UnityCallbacks;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AiServiceBridge {

    Activity activity;
    public String data;

    public AiServiceBridge(Activity activity)
    {
        this.activity = activity;
        EventBus.getDefault().register(this);
    }

    public void startForegroundService(){

        Intent serviceIntent = new Intent(activity, AiForegroundService.class);
        serviceIntent.setAction(AiForegroundService.Actions.START.toString());
        activity.startForegroundService(serviceIntent);

    }
    //unregister from the event
    public void stopService(){

        Intent serviceIntent = new Intent(activity, AiForegroundService.class);
        serviceIntent.setAction(AiForegroundService.Actions.STOP.toString());
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event){

        data = event.message;
        AiUnityCallbacks.onUpdateAiAnalyserData(data);
    }






}

