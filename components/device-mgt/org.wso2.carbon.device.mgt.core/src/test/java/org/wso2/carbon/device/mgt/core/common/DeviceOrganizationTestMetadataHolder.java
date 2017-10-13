package org.wso2.carbon.device.mgt.core.common;

public class DeviceOrganizationTestMetadataHolder {

    private static String deviceId = "d1";
    private static String deviceName = "device1";
    private static String deviceParent = "gateway1";
    private static int minsSinceLastPing = 20;
    private static int state = 0;
    private static int isGateway = 1;


    public static String getDeviceId() {
        return deviceId;
    }

    public static String getDeviceName() {
        return deviceName;
    }

    public static String getDeviceParent() {
        return deviceParent;
    }

    public static int getMinsSinceLastPing() {
        return minsSinceLastPing;
    }

    public static int getState() {
        return state;
    }

    public static void setState(int state) {
        DeviceOrganizationTestMetadataHolder.state = state;
    }

    public static int getIsGateway() {
        return isGateway;
    }
}

