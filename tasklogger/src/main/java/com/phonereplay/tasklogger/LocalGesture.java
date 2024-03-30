package com.phonereplay.tasklogger;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalGesture {
    @SerializedName("activityId")
    public String activityId;

    @SerializedName("gestureType")
    public String gestureType;

    @SerializedName("targetTime")
    public String targetTime;
    @SerializedName("createdAt")
    public String createdAt; // Horário em que o gesto foi registrado

    @SerializedName("coordinates")
    public String coordinates;

    // Construtor atualizado para aceitar targetTime diretamente
    @SuppressLint("SimpleDateFormat")
    public LocalGesture(String activityId, String gestureType, String targetTime, String coordinates) {
        this.activityId = activityId;
        this.gestureType = gestureType;
        this.targetTime = targetTime;
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.coordinates = coordinates;
    }
}

