package com.abk.distance.chatgptscript;

import com.abk.distance.services.MapSimulationPointData;
import com.abk.distance.services.StepDataCalculator;

import java.util.ArrayList;
import java.util.List;

public class RunData {

    public String Name = "";
    public float voiceVolume = 0f;
    public float footstepsVolume = 0f;
    public float feedbackVolume = 0f;

    public List<MapSimulationPointData> mapPointData = new ArrayList<>();
    public List<StepDataCalculator> stepData = new ArrayList<>();

    public int Cadence;
    public int HeartRate;

    public int PaceMins = 0;
    public int PaceSeconds = 0;
    public int CurrentPaceMins = 0;
    public int CurrentPaceSeconds = 0;

    public int FastestPaceMins = 0;
    public int FastestPaceSeconds = 0;

    public int SlowestPaceMins = 0;
    public int SlowestPaceSeconds = 0;

    public int BestLapMins = 0;
    public int BestLapSeconds = 0;

    public float DistanceToTravel = 0f;
    public float DistanceTravelled = 0f;
    public float PreciseDistance = 0f;
    public float DistanceDifferenceFromTheFirstPos = 0f;

    public List<Float> LapDistance = new ArrayList<>();
    public List<Float> LapTimings = new ArrayList<>();

    public float TimeTaken = 0;
    public int Position = 0;
    public int CaloriesBurnt = 0;

    public String DayStarted = "";
    public String DateStarted = "";
    public String TimeStarted = "";
    public String Mode = "";

}
