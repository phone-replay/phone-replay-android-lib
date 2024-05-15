package com.phonereplay.tasklogger.network;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.phonereplay.tasklogger.Binary;
import com.phonereplay.tasklogger.BinaryDataServiceGrpc;
import com.phonereplay.tasklogger.DeviceModel;
import com.phonereplay.tasklogger.LocalSession;
import com.phonereplay.tasklogger.TimeLine;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Client {

    public void sendBinaryDataV3(byte[] file, LocalSession actions, DeviceModel device) {
        try {
            URL url = new URL("http://10.0.0.106:3000/api/v1/sdk/create/55840540423a2256372b3f00304f01f735f695bb");
            String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

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

    public void sendBinaryDataV2(byte[] binaryData, String sessionId, Set<TimeLine> timeLines, DeviceModel deviceModel) {
        try {
            URL url = new URL("http://10.0.0.106:3000/api/v1/sdk/create/55840540423a2256372b3f00304f01f735f695bb");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");

            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.writeBytes("------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"binaryData\"; filename=\"binaryData\"\r\n");
                outputStream.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                outputStream.write(binaryData);
                outputStream.writeBytes("\r\n");

                int i = 0;
                for (TimeLine timeLine : timeLines) {
                    outputStream.writeBytes("------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n");
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"timeLines[" + i + "].coordinates\"\r\n\r\n");
                    outputStream.writeBytes(timeLine.getCoordinates());
                    outputStream.writeBytes("\r\n");

                    outputStream.writeBytes("------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n");
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"timeLines[" + i + "].gestureType\"\r\n\r\n");
                    outputStream.writeBytes(timeLine.getGestureType());
                    outputStream.writeBytes("\r\n");

                    outputStream.writeBytes("------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n");
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"timeLines[" + i + "].targetTime\"\r\n\r\n");
                    outputStream.writeBytes(timeLine.getTargetTime());
                    outputStream.writeBytes("\r\n");

                    i++;
                }

                outputStream.writeBytes("------WebKitFormBoundary7MA4YWxkTrZu0gW--");
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println(response);
                }
            } else {
                System.out.println("Error: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBinaryData(byte[] binaryData, String sessionId, Set<TimeLine> timeLines) {
        if (binaryData == null || sessionId == null) {
            return;
        }
        String serverAddress = "10.0.0.106"; //you ip
        int serverPort = 8000;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .maxInboundMessageSize(50 * 1024 * 1024)
                .build();

        try {
            BinaryDataServiceGrpc.BinaryDataServiceBlockingStub blockingStub = BinaryDataServiceGrpc.newBlockingStub(channel);

            Binary.BinaryDataRequest.Builder requestBuilder = Binary.BinaryDataRequest.newBuilder()
                    .setBinaryData(ByteString.copyFrom(binaryData))
                    .setSessionId(sessionId);

            // Adicionando TimeLines à requisição
            for (TimeLine tl : timeLines) {
                Binary.TimeLine.Builder tlBuilder = Binary.TimeLine.newBuilder()
                        .setCoordinates(tl.getCoordinates())
                        .setGestureType(tl.getGestureType())
                        .setTargetTime(tl.getTargetTime());
                requestBuilder.addTimeLines(tlBuilder);
            }

            Binary.BinaryDataRequest request = requestBuilder.build();
            Binary.BinaryDataResponse response = blockingStub.sendBinaryData(request);

            System.out.println("Response from server: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown();
            }
        }
    }
}
