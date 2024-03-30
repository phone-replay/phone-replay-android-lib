package com.phonereplay.tasklogger.network.models.reponses;

import com.google.gson.annotations.SerializedName;
import com.phonereplay.tasklogger.network.models.Session;

public class CreateSessionResponse {

    @SerializedName("session")
    Session session;
    @SerializedName("created")
    private boolean created;

    public Session getSession() {
        return session;
    }

    public boolean isCreated() {
        return created;
    }
}
