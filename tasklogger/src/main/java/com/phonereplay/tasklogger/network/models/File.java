package com.phonereplay.tasklogger.network.models;

import com.google.gson.annotations.SerializedName;

public class File {

    @SerializedName("content")
    String content;

    @SerializedName("session_id")
    String sessionId;

    public File(String content, String sessionId) {
        this.content = content;
        this.sessionId = sessionId;
    }
}
