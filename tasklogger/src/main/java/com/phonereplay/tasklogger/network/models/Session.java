package com.phonereplay.tasklogger.network.models;

import com.google.gson.annotations.SerializedName;

public class Session {

    @SerializedName("id")
    String sessionId;
    @SerializedName("project_id")
    String projectId;

    public String getSessionId() {
        return sessionId;
    }

}
