package com.phonereplay.tasklogger.network.models;

import com.google.gson.annotations.SerializedName;

public class Project {

    @SerializedName("id")
    String projectId;
    @SerializedName("name")
    String name;
    @SerializedName("expiration")
    String expiration;
    @SerializedName("user_id")
    String userId;
    @SerializedName("project_access_key")
    String projectAccessId;

    public String getProjectId() {
        return projectId;
    }


}
