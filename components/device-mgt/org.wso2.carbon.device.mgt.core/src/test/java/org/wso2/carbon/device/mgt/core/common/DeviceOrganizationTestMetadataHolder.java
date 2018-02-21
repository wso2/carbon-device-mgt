/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
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

