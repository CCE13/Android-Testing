package com.abk.distance.utils

import android.util.Log
import com.unity3d.player.UnityPlayer

class AiUnityCallbacks {
    private val TAG: String = "AiUnityCallbacks"

    companion object {


        const val CALLBACK_OBJ: String = "AiPluginListener"
        const val onUpdateAiAnalyserData : String = "onUpdateAiAnalyserData"


        @JvmStatic
        fun onUpdateAiAnalyserData(data : String){
            try {
                UnityPlayer.UnitySendMessage(CALLBACK_OBJ, onUpdateAiAnalyserData, data)
                Log.e("UnityCallBacks","Sending AI analyser data")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}