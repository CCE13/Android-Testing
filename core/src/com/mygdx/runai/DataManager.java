package com.mygdx.runai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class DataManager {


    private static boolean runOnce;
    File file = new File("/data/user/0/com.runai.RunAI/files/","variables.txt");

    public static float LoadPlayerRanDist() {
        return getValues("dist_m");
    }
    public static float LoadPlayerRanTime() {

       return getValues("time_s");
   }

    public static List<AudioVariables> LoadAIAudio(){

        if(!runOnce){
            //clearCurrentDataFiles();
            runOnce = true;
        }

        List<AudioVariables> aiAudioVariables = new ArrayList<>();

        File file = new File("/data/user/0/com.mygdx.runai/files/");

        if(!file.exists()){

            saveValuesInFile("AI" + -1,0,0,false);

        }
        File[] files = file.listFiles();

        System.out.println(files.length);
        if(files != null){
            for (int i = 0; i < files.length; i++){
                if(files[i].getName().contains("AI")){
                    AudioVariables audioVariables = getAudioValues(files[i].getName(), AudioVariables.class);
                    aiAudioVariables.add(audioVariables);

                }
            }
        }

        return aiAudioVariables;

    }


    private static void saveValuesInFile(String name, float volumeValue, float panValue, boolean playSound) {
        try {
            File file = new File("/data/user/0/com.runai.RunAI/files/", name+".txt");
            FileWriter writer = new FileWriter(file);
            writer.write("Volume: " + volumeValue + "\n");
            writer.write("Pan: " + panValue+ "\n");
            writer.write("playSound: " + playSound);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred saving the file.");
            e.printStackTrace();
        }
    }
    public static <T> float getValues(String input){

        try {
            File file = new File("/data/user/0/com.runai.RunAI/files/", input+".txt");
            //FileHandle file = Gdx.files.local(input + ".txt");
            FileReader readers = new FileReader(file);

            if(file == null){
                return 0;
            }
            BufferedReader reader = new BufferedReader(readers);
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(": ");
                String value = parts[1];
                return Float.parseFloat(value);

            }
            reader.close();
        } catch (IOException e) {
            System.out.println("An error occurred unable to load file");
            e.printStackTrace();
        }

        return 0;

    }


    public static <T> T getAudioValues(String input, Class<T> returnValue){

        System.out.println("From datamanager and its up ur ass" + input );
        try {
            File file = new File("/data/user/0/com.mygdx.runai/files/", input);

            FileReader readers = new FileReader(file);
            BufferedReader reader = new BufferedReader(readers);

            if(file == null){

                return returnValue.cast(0);
            }

            float volumeValue = 0;
            float panValue = 0;
            boolean playSound = false;

            String line = reader.readLine();
            while (line != null) {
                //System.out.println("From datamanager and its up ur mothers ass ");
                String[] parts = line.split(": ");

                //FailSafe: If the data randomly does not save properly it would not crash the whole app and skip the reading of the file
                if(parts.length != 2) {
                    System.out.println("Invalid line format: " + line);
                    line = reader.readLine();
                    continue;
                }

                String key = parts[0];
                String value = parts[1];

                // Process each key-value pair here
                if (key.equals("Volume")) {
                    volumeValue = Float.parseFloat(value);
                    // Do something with the volumeValue
                } else if (key.equals("Pan")) {
                    panValue = Float.parseFloat(value);
                    // Do something with the panValue
                } else if (key.equals("playSound")) {
                    playSound = Boolean.parseBoolean(value);
                    // Do something with the playSound
                }

                line = reader.readLine();
            }


            AudioVariables audioVariables = new AudioVariables(volumeValue, panValue, playSound);

            if(returnValue == AudioVariables.class){
                return returnValue.cast(audioVariables);
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("An error occurred unable to load file");
            e.printStackTrace();
        }
        AudioVariables aV = new AudioVariables(0,0,false);

        return returnValue.cast(aV);



    }


    public static float loadAIRunData(int aiIndex){

//        try {
//            //File file = new File("/data/user/0/com.mygdx.runai/files/" + aiIndex +".txt", "variables.txt");
//            FileHandle file = Gdx.files.local( aiIndex+ ".txt");
//
//
//            if(!file.exists()){
//
//                return 0;
//            }
//
//
//            float runningPace = 0;
//
//            BufferedReader reader = new BufferedReader(file.reader());
//            String line = reader.readLine();
//            while (line != null) {
//
//
//                String[] parts = line.split(": ");
//
//
//
//                String key = parts[0];
//                String value = parts[1];
//
//                if(key.equals("RunningSpeed")){
//                    runningPace = Float.parseFloat(value);
//                }
//
//                Gdx.app.log("LoadRunNode", "value " + value + "index " + aiIndex);
//
//
//                return runningPace;
//
//            }
//
//            reader.close();
//
//        } catch (IOException e) {
//            System.out.println("An error occurred unable to load file");
//            e.printStackTrace();
//        }

        return 0;

    }

    public static void clearCurrentDataFiles(){
        File dir = new File("/data/user/0/com.mygdx.runai/files/");

        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    file.delete(); // delete file
                    System.out.println("yo" + file.listFiles());
                }
            }
        } else {
            System.out.println("Directory does not exist");
        }

    }

    //TODO make a universal reader using line.startsWith

}
