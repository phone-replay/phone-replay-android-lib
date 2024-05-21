package com.phonereplay.tasklogger.network;

import com.google.gson.Gson;
import com.phonereplay.tasklogger.DeviceModel;
import com.phonereplay.tasklogger.LocalSession;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Client {

    public void sendBinaryDataV3(byte[] file, LocalSession actions, DeviceModel device, String projectKey) {
        try {
            URL url = new URL("http://10.0.0.106:3000/api/v1/sdk/create/" + projectKey);
            String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            // Anexar arquivo
            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"upload.bin\"\r\n");
            writer.write("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();
            outputStream.write(file);
            outputStream.flush();
            writer.write("\r\n");

            // Anexar dados do dispositivo como JSON
            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"device\"\r\n\r\n");
            Gson gson = new Gson();
            String deviceJson = gson.toJson(device);
            writer.write(deviceJson);
            writer.write("\r\n");

            // Anexar ações como JSON
            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"actions\"\r\n\r\n");
            String actionsJson = gson.toJson(actions);
            writer.write(actionsJson);
            writer.write("\r\n");

            // Finalizando o corpo da requisição
            writer.write("--" + boundary + "--\r\n");
            writer.flush();
            writer.close();

            // Obter resposta do servidor
            InputStream responseStream = new BufferedInputStream(conn.getInputStream());
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();

            String response = stringBuilder.toString();
            System.out.println("Response: " + response);

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
