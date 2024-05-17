package com.phonereplay.tasklogger.service;

import android.content.Context;
import android.graphics.Bitmap;

import com.phonereplay.tasklogger.DeviceModel;
import com.phonereplay.tasklogger.LocalSession;
import com.phonereplay.tasklogger.network.Client;
import com.phonereplay.tasklogger.utils.NetworkUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;

public class PhoneReplayService {
    private static final int COMPRESSION_QUALITY = 10;
    private final Client client;
    private final Context context;
    private byte[] fullBytesVideo;
    private byte[] previousImageCompressed;
    private String accessKey;

    public PhoneReplayService(Context context) {
        this.client = new Client();
        this.context = context;
    }

    private static byte[] joinByteArrays(byte[] array1, byte[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;
        byte[] result = new byte[length1 + length2];
        System.arraycopy(array1, 0, result, 0, length1);
        System.arraycopy(array2, 0, result, length1, length2);
        return result;
    }

    public static byte[] compress(byte[] data) throws IOException {
        if (data == null) {
            return null;
        }
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        try {
            deflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            deflater.finish();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            return outputStream.toByteArray();
        } finally {
            deflater.end();
        }
    }

    /*
    private static String encodeToBase64(byte[] binaryData) {
        byte[] base64Encoded = android.util.Base64.encode(binaryData, Base64.DEFAULT);
        return new String(base64Encoded);
    }
     */

    private static byte[] writeImageCompressedFromBitmap(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
        return compress(byteArrayOutputStream.toByteArray());
    }

    private static byte[] writeImageFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static boolean compareByteArrays(byte[] array1, byte[] array2) {
        return Arrays.equals(array1, array2);
    }

    private static byte[] combineIdentifierAndData(byte[] data) {
        byte[] identifierBytes = "------".getBytes();
        byte[] combinedData = new byte[identifierBytes.length + data.length];

        System.arraycopy(identifierBytes, 0, combinedData, 0, identifierBytes.length);
        System.arraycopy(data, 0, combinedData, identifierBytes.length, data.length);

        return combinedData;
    }

    public void queueBytesBitmap(Bitmap bitmap) throws IOException {
        byte[] imageCompressed = writeImageCompressedFromBitmap(bitmap);
        byte[] combineIdentifierAndData;

        if (fullBytesVideo != null) {
            if (compareByteArrays(previousImageCompressed, imageCompressed)) {
                combineIdentifierAndData = combineIdentifierAndData("D".getBytes());
            } else {
                combineIdentifierAndData = combineIdentifierAndData(imageCompressed);
                previousImageCompressed = imageCompressed;
            }
            byte[] joinByteArrays;
            joinByteArrays = joinByteArrays(fullBytesVideo, combineIdentifierAndData);
            fullBytesVideo = joinByteArrays;
        } else {
            previousImageCompressed = imageCompressed;
            combineIdentifierAndData = imageCompressed;
            fullBytesVideo = combineIdentifierAndData;
        }
        bitmap.recycle();
    }

    public void queueBytesBitmapV2(Bitmap bitmap) {
        byte[] image = writeImageFromBitmap(bitmap);
        byte[] combineIdentifierAndData;

        if (fullBytesVideo != null) {
            if (compareByteArrays(previousImageCompressed, image)) {
                combineIdentifierAndData = combineIdentifierAndData("D".getBytes());
            } else {
                combineIdentifierAndData = combineIdentifierAndData(image);
                previousImageCompressed = image;
            }
            byte[] joinByteArrays;
            joinByteArrays = joinByteArrays(fullBytesVideo, combineIdentifierAndData);
            fullBytesVideo = joinByteArrays;
        } else {
            previousImageCompressed = image;
            combineIdentifierAndData = image;
            fullBytesVideo = combineIdentifierAndData;
        }
        bitmap.recycle();
    }

    public void createVideo(LocalSession timeLines, DeviceModel deviceModel, String projectKey) throws IOException {
        client.sendBinaryDataV3(compress(fullBytesVideo), timeLines, deviceModel, projectKey);
        //fullBytesVideo = null;
        if (NetworkUtil.isWiFiConnected(context)) {
        } else {
        }
    }
}
