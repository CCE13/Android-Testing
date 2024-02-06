package com.abk.distance.services

import com.abk.distance.LappingManager
import com.abk.distance.utils.DataPoint
import java.io.Serializable

public class ServiceData : Serializable{
    var distanceTravelled: Double = 0.0
    var paceMin :Int = 0;
    var paceSeconds : Int = 0;
    var stepCounted: ArrayList<StepDataCalculator>? = null
    var totalTimeRan: Int = 0
    var locationDataPoints: ArrayList<DataPoint>? = null
    var aiDistanceTravelled: ArrayList<Float>? = null
    var lappingManager: LappingManager? = null
    var aiTime : ArrayList<Int>? = null
    var locationDistanceData : ArrayList<MapSimulationPointData> ? = null;

    var fastestPaceMin : Int = 0
    var fastestPaceSeconds : Int =0

    var slowestPaceMin : Int = 0
    var slowestPaceSeconds: Int = 0
    var cadence : Int = 0;
    var heartrate :Int = 0;




    constructor(
        distanceTravelled: Double,
        paceMin : Int,
        paceSeconds : Int,
        cadence : Int,
        heartrate : Int,
        stepCounted: ArrayList<StepDataCalculator>?,
        totalTimeRan: Int,
        locationDataPoints: ArrayList<DataPoint>?,
        aiDistanceTravelled:  ArrayList<Float>?,
        aiTime:  ArrayList<Int>?,
        locationDistanceData: ArrayList<MapSimulationPointData>?,
        lappingManager: LappingManager?,
        fastestPaceMin: Int,
        fastestPaceSeconds: Int,
        slowestPaceMin: Int,
        slowestPaceSeconds: Int,) {
        this.distanceTravelled = distanceTravelled
        this.paceMin = paceMin;
        this.paceSeconds = paceSeconds;
        this.stepCounted = stepCounted
        this.totalTimeRan = totalTimeRan
        this.locationDataPoints = locationDataPoints
        this.aiDistanceTravelled = aiDistanceTravelled
        this.lappingManager = lappingManager
        this.aiTime  = aiTime;
        this.locationDistanceData = locationDistanceData;
        this.fastestPaceMin = fastestPaceMin
        this.fastestPaceSeconds = fastestPaceSeconds
        this.slowestPaceMin = slowestPaceMin
        this.slowestPaceSeconds = slowestPaceSeconds
        this.cadence = cadence
        this.heartrate = heartrate
    }
}
