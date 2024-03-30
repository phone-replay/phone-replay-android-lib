package com.phonereplay.tasklogger;

import android.content.Context;
import android.os.Build;


public class DeviceModel {

    private final String manufacturer;
    private final String model;
    private final String device;
    private final String brand;
    private final String osVersion;
    private final int sdkVersion;
    private final String installID;
    private final String totalStorage;
    private final String totalRAM;
    private final String currentNetwork;
    private final String language;
    private final float batteryLevel;
    private final String sessionId;
    private final String screenResolution;
    private final String platform;

    public DeviceModel(Context context, String sessionId) {
        this.sessionId = sessionId;
        this.manufacturer = Build.MANUFACTURER;
        this.model = Build.MODEL;
        this.device = Build.DEVICE;
        this.brand = Build.BRAND;
        this.osVersion = Build.VERSION.RELEASE;
        this.sdkVersion = Build.VERSION.SDK_INT;
        this.installID = Build.ID;
        this.totalStorage = DeviceInfo.getTotalStorage();
        this.totalRAM = DeviceInfo.getTotalRAM(context); // Asumindo que esta lógica é implementada em DeviceInfo
        this.currentNetwork = DeviceInfo.getCurrentNetwork(context); // Asumindo que esta lógica é implementada em DeviceInfo
        this.language = DeviceInfo.getLanguage();
        this.batteryLevel = DeviceInfo.getBatteryLevel(context); // Asumindo que esta lógica é implementada em DeviceInfo
        this.screenResolution = DeviceInfo.getScreenResolution(context);
        this.platform = "Android";
    }
}
