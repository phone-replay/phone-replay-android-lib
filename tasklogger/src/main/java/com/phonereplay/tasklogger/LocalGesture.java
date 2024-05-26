package com.phonereplay.tasklogger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalGesture {
    public String activityId;

    public String gestureType;

    public String targetTime;
    public String createdAt;

    public String coordinates;

    public LocalGesture(String activityId, String gestureType, String targetTime, String coordinates) {
        this.activityId = activityId;
        this.gestureType = gestureType;
        this.targetTime = targetTime;
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.coordinates = coordinates;
    }
}

