package com.phonereplay.tasklogger.network;

import com.google.protobuf.ByteString;
import com.phonereplay.tasklogger.Binary;
import com.phonereplay.tasklogger.BinaryDataServiceGrpc;
import com.phonereplay.tasklogger.TimeLine;

import java.util.Set;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient {

    public void sendBinaryData(byte[] binaryData, String sessionId, Set<TimeLine> timeLines) {
        if (binaryData == null || sessionId == null) {
            return;
        }
        String serverAddress = "10.0.0.100"; //you ip
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
