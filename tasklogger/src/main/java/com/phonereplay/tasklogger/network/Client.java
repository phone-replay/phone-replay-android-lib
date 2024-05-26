package com.phonereplay.tasklogger.network;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.phonereplay.tasklogger.DeviceModel;
import com.phonereplay.tasklogger.LocalSession;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Client {

    @NonNull
    private static String getString(HttpURLConnection conn) throws IOException {
        InputStream responseStream = new BufferedInputStream(conn.getInputStream());
        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();

        String response = stringBuilder.toString();
        return response;
    }

    public boolean validateAccessKey(String projectKey) {
        try {
            URL url = new URL("http://10.0.0.106:8080/check-recording?key=" + projectKey);
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

    public void sendBinaryData(byte[] file, LocalSession actions, DeviceModel device, String projectKey) {
        try {
            URL url = new URL("http://10.0.0.106:8080/write?key=" + projectKey);
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
            Gson gson = new Gson();
            String deviceJson = gson.toJson(device);
            writer.write(deviceJson);
            writer.write("\r\n");

            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"actions\"\r\n\r\n");
            String actionsJson = gson.toJson(actions);
            writer.write(actionsJson);
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
}
