package com.abk.distance.chatgptscript;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import scala.util.control.TailCalls;

public class JsonStreamReader {
    public float value;
    public static RunData runData;
    public Context context;

    public JsonStreamReader(Context context){
        this.context = context;

    }


    public void convertToRunData(String fileName, Context context){
        Gson gson = new Gson();
        AssetManager assetManager = context.getAssets();
        try (InputStreamReader reader = new InputStreamReader(assetManager.open(fileName))) {
            runData = gson.fromJson(reader, RunData.class);
        } catch (IOException e) {
            Log.e("RunDataConverter", "Error reading JSON file from assets: " + e.getMessage(), e);
        }
    }

}
