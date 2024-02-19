package com.abk.distance.utils

import android.util.Log
import com.unity3d.player.UnityPlayer

class UnityCallbacks {
    private val TAG: String = "UnityCallbacks"

    companion object {
        const val CALLBACK_OBJ: String = "RunningPluginListener"

        const val onUpdatePlayerData: String = "onUpdatePlayerData"
        const val onUpdateFinalPlayerData: String = "onUpdateFinalPlayerData"
        const val onPermissionDenied: String = "onPermissionDenied"
        const val onUpdateAIData : String = "onUpdateAIData"
        const val onUpdateAiAnalyserData : String = "onUpdateAiAnalyserData"

        @JvmStatic
        fun onUpdatePlayerData(data: String) {
            try {
                UnityPlayer.UnitySendMessage(CALLBACK_OBJ, onUpdatePlayerData, data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        @JvmStatic
        fun onFinalUpdatePlayerData(data: String){
            try {
                UnityPlayer.UnitySendMessage(CALLBACK_OBJ, onUpdateFinalPlayerData, data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        @JvmStatic
        fun permissionDenied(permission: String) {
            try {
                UnityPlayer.UnitySendMessage(CALLBACK_OBJ, onPermissionDenied, permission)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        @JvmStatic
        fun onUpdateAIData(data : String){
            try {
                UnityPlayer.UnitySendMessage(CALLBACK_OBJ, onUpdateAIData, data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }
}