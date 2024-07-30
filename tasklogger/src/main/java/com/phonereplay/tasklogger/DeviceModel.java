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
    private final String screenResolution;
    private final String platform;

    public DeviceModel(Context context) {
        this.manufacturer = Build.MANUFACTURER;
        this.model = Build.MODEL;
        this.device = Build.DEVICE;
        this.brand = Build.BRAND;
        this.osVersion = Build.VERSION.RELEASE;
        this.sdkVersion = Build.VERSION.SDK_INT;
        this.installID = Build.ID;
        this.totalStorage = DeviceInfo.getTotalStorage();
        this.totalRAM = DeviceInfo.getTotalRAM(context);
        this.currentNetwork = DeviceInfo.getCurrentNetwork(context);
        this.language = DeviceInfo.getLanguage();
        this.batteryLevel = DeviceInfo.getBatteryLevel(context);
        this.screenResolution = DeviceInfo.getScreenResolution(context);
        this.platform = "Android";
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getDevice() {
        return device;
    }

    public String getBrand() {
        return brand;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public int getSdkVersion() {
        return sdkVersion;
    }

    public String getInstallID() {
        return installID;
    }

    public String getTotalStorage() {
        return totalStorage;
    }

    public String getTotalRAM() {
        return totalRAM;
    }

    public String getCurrentNetwork() {
        return currentNetwork;
    }

    public String getLanguage() {
        return language;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public String getPlatform() {
        return platform;
    }
}
