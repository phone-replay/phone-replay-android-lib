package com.phonereplay.tasklogger;

public class TimeLine {
    private final String coordinates;
    private final String gestureType;
    private final String targetTime;

    public TimeLine(String coordinates, String gestureType, String targetTime) {
        this.coordinates = coordinates;
        this.gestureType = gestureType;
        this.targetTime = targetTime;
    }

    // Getters
    public String getCoordinates() {
        return coordinates;
    }

    public String getGestureType() {
        return gestureType;
    }

    public String getTargetTime() {
        return targetTime;
    }
}
