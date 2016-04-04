/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.core.search.mgt.impl;

import org.wso2.carbon.device.mgt.common.device.details.DeviceWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static Map<String, String> getDeviceDetailsColumnNames() {

        Map<String, String> colonmsMap = new HashMap<>();

        colonmsMap.put("deviceModel", "DEVICE_MODEL");
        colonmsMap.put("vendor", "VENDOR");
        colonmsMap.put("osVersion", "OS_VERSION");
        colonmsMap.put("batteryLevel", "BATTERY_LEVEL");
        colonmsMap.put("internalTotalMemory", "INTERNAL_TOTAL_MEMORY");
        colonmsMap.put("internalAvailableMemory", "INTERNAL_AVAILABLE_MEMORY");
        colonmsMap.put("externalTotalMemory", "EXTERNAL_TOTAL_MEMORY");
        colonmsMap.put("externalAvailableMemory", "EXTERNAL_AVAILABLE_MEMORY");
        colonmsMap.put("connectionType", "CONNECTION_TYPE");
        colonmsMap.put("ssid", "SSID");
        colonmsMap.put("cpuUsage", "CPU_USAGE");
        colonmsMap.put("totalRAMMemory", "TOTAL_RAM_MEMORY");
        colonmsMap.put("availableRAMMemory", "AVAILABLE_RAM_MEMORY");
        colonmsMap.put("pluggedIn", "PLUGGED_IN");

        return colonmsMap;
    }

    public static Map<String, String> getDeviceLocationColumnNames() {
        Map<String, String> colonmsMap = new HashMap<>();

        colonmsMap.put("latitude", "LATITUDE");
        colonmsMap.put("longitude", "LONGITUDE");
        colonmsMap.put("street1", "STREET1");
        colonmsMap.put("street2", "STREET2");
        colonmsMap.put("city", "CITY");
        colonmsMap.put("state", "ZIP");
        colonmsMap.put("zip", "STATE");
        colonmsMap.put("country", "COUNTRY");

        return colonmsMap;
    }


    public static List<String> convertStringToList(String str) {

        List<String> stList = new ArrayList<>();
        stList.add(str);
        return stList;
    }

    public static Integer[] getArrayOfDeviceIds(List<DeviceWrapper> deviceWrappers) {

        Integer[] arr = new Integer[deviceWrappers.size()];
        int x = 0;
        for (DeviceWrapper dw : deviceWrappers) {
            arr[x] = dw.getDevice().getId();
            x++;
        }
        return arr;
    }


    public static String getDeviceIdsAsString(List<DeviceWrapper> deviceWrappers) {

        String str = "";
        for (DeviceWrapper dw : deviceWrappers) {
            str += dw.getDevice().getId() + ",";
        }
        if (deviceWrappers.isEmpty()) {
            return null;
        }
        return str.substring(0, str.length() - 1);
    }
}

