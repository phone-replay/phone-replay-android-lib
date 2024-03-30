package com.phonereplay.tasklogger.network.models.reponses;

import com.google.gson.annotations.SerializedName;
import com.phonereplay.tasklogger.network.models.Project;

public class VerifyProjectAuthResponse {

    @SerializedName("project")
    Project project;

    @SerializedName("auth")
    private boolean auth;


    public Project getProject() {
        return project;
    }

    public boolean isAuth() {
        return auth;
    }
}
