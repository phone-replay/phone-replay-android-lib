package com.phonereplay.tasklogger.network;

import com.phonereplay.tasklogger.DeviceModel;
import com.phonereplay.tasklogger.LocalSession;
import com.phonereplay.tasklogger.network.models.reponses.CreateSessionResponse;
import com.phonereplay.tasklogger.network.models.reponses.VerifyProjectAuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PhoneReplayApiInterface {

    @POST("api/session/create/{project_id}")
    Call<CreateSessionResponse> createSession(@Path("project_id") String projectId);

    @GET("api/project/check-project/{project_access_key}")
    Call<VerifyProjectAuthResponse> verifyProjectAuth(@Path("project_access_key") String project_access_key);

    @GET("create-video/{session_id}")
    Call<String> createVideo(@Path("session_id") String sessionId);

    @POST("api/session/create-session-data")
    Call<Void> sendLocalSessionData(@Body LocalSession localSession);

    @POST("api/device/create")
    Call<Void> sendDeviceInfo(@Body DeviceModel deviceModel);
}
