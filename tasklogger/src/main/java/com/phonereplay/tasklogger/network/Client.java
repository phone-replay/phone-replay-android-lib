package com.phonereplay.tasklogger.network;

import android.util.Log;

import com.phonereplay.tasklogger.ActivityGesture;
import com.phonereplay.tasklogger.DeviceModel;
import com.phonereplay.tasklogger.Gesture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class Client {

    private static final String BASE_URL_K8S = "http://10.0.0.102:8080";
    private static String BASE_URL = null;

    private static String getString(HttpURLConnection conn) throws IOException {
        InputStream responseStream = new BufferedInputStream(conn.getInputStream());
        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();
        return stringBuilder.toString();
    }

    private static JSONObject getJsonObject(ActivityGesture log) throws JSONException {
        JSONObject logJson = new JSONObject();
        logJson.put("activityName", log.getActivityName());
        JSONArray gesturesArray = new JSONArray();
        for (Gesture gesture : log.getGestures()) {
            JSONObject gestureJson = getJsonObject(gesture);
            gesturesArray.put(gestureJson);
        }
        logJson.put("gestures", gesturesArray);
        return logJson;
    }

    private static JSONObject getJsonObject(Gesture gesture) throws JSONException {
        JSONObject gestureJson = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        for (String[] actionData : gesture.getActions()) {
            JSONObject actionJson = new JSONObject();
            actionJson.put("action", actionData[0]);
            actionJson.put("targetTime", actionData[1]);
            actionJson.put("coordinates", actionData[2]);
            actionsArray.put(actionJson);
        }
        gestureJson.put("actions", actionsArray);
        return gestureJson;
    }

    private static JSONObject getDeviceJson(DeviceModel device) throws JSONException {
        JSONObject deviceJson = new JSONObject();
        deviceJson.put("batterylevel", device.getBatteryLevel());
        deviceJson.put("brand", device.getBrand());
        deviceJson.put("currentnetwork", device.getCurrentNetwork());
        deviceJson.put("device", device.getDevice());
        deviceJson.put("installid", device.getInstallID());
        deviceJson.put("language", device.getLanguage());
        deviceJson.put("manufacturer", device.getManufacturer());
        deviceJson.put("model", device.getModel());
        deviceJson.put("osversion", device.getOsVersion());
        deviceJson.put("platform", device.getPlatform());
        deviceJson.put("screenresolution", device.getScreenResolution());
        deviceJson.put("sdkversion", device.getSdkVersion());
        deviceJson.put("totalram", device.getTotalRAM());
        deviceJson.put("totalstorage", device.getTotalStorage());
        return deviceJson;
    }

    public boolean validateAccessKey(String projectKey) {
        try {
            URL url = new URL(BASE_URL + "/check-recording?key=" + projectKey);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] compressString(String data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        gzipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public void sendBinaryData(byte[] file, Map<String, ActivityGesture> activityGesture, DeviceModel device, String projectKey, long duration) {
        try {
            URL url = new URL(BASE_URL_K8S + "/v2/write?key=" + projectKey);
            String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"upload.bin\"\r\n");
            writer.write("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();
            outputStream.write(file);
            outputStream.flush();
            writer.write("\r\n");

            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"device\"\r\n\r\n");
            JSONObject deviceJson = getDeviceJson(device);
            writer.write(deviceJson.toString());
            writer.write("\r\n");

            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"activityGestureLogs\"; filename=\"activityGestureLogs.gz\"\r\n");
            writer.write("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();

            JSONArray activitiesArray = new JSONArray();
            for (ActivityGesture log : activityGesture.values()) {
                JSONObject logJson = getJsonObject(log);
                activitiesArray.put(logJson);
            }
            activityGesture.clear();
            String activitiesArrayString = activitiesArray.toString();
            byte[] compressedActivities = compressString(activitiesArrayString);
            outputStream.write(compressedActivities);
            outputStream.flush();
            writer.write("\r\n");

            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"duration\"\r\n\r\n");
            writer.write(String.valueOf(duration));
            writer.write("\r\n");

            writer.write("--" + boundary + "--\r\n");
            writer.flush();
            writer.close();

            String response = getString(conn);
            System.out.println("Response: " + response);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callEndpoint() {
        try {
            URL url = new URL("https://columba-url-wgvjiuyt2q-uc.a.run.app/url");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                String response = getString(con);
                Log.d("Response from Columba endpoint: ", response);
                BASE_URL = response.trim();
            } else {
                System.out.println("Failed to call Columba endpoint. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
