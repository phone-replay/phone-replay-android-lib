package com.phonereplay.tasklogger.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.phonereplay.tasklogger.DeviceModel;
import com.phonereplay.tasklogger.LocalSession;
import com.phonereplay.tasklogger.network.models.reponses.CreateSessionResponse;
import com.phonereplay.tasklogger.network.models.reponses.VerifyProjectAuthResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient implements PhoneReplayApiInterface {

    public static Retrofit retrofit = null;

    public ApiClient() {
        getClient();
    }

    public void getClient() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.0.100:3000")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public PhoneReplayApiInterface getInstance() {
        return retrofit.create(PhoneReplayApiInterface.class);
    }

    @Override
    public Call<CreateSessionResponse> createSession(String projectId) {
        return getInstance().createSession(projectId);
    }

    @Override
    public Call<VerifyProjectAuthResponse> verifyProjectAuth(String project_access_key) {
        return getInstance().verifyProjectAuth(project_access_key);
    }

    @Override
    public Call<String> createVideo(String sessionId) {
        return getInstance().createVideo(sessionId);
    }

    @Override
    public Call<Void> sendLocalSessionData(LocalSession localSession) {
        return getInstance().sendLocalSessionData(localSession);

    }

    @Override
    public Call<Void> sendDeviceInfo(DeviceModel deviceModel) {
        return getInstance().sendDeviceInfo(deviceModel);
    }
}