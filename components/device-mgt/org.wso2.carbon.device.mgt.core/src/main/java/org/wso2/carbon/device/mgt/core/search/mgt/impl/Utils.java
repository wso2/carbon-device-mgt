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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.search.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private static Map<String, String> genericColumnsMap = new HashMap<>();
    private static Map<String, String> locationColumnsMap = new HashMap<>();

    private static Map<String, String> operators = new HashMap<>();

    static {
        genericColumnsMap.put("deviceModel", "DEVICE_MODEL");
        genericColumnsMap.put("vendor", "VENDOR");
        genericColumnsMap.put("osVersion", "OS_VERSION");
        genericColumnsMap.put("osBuildDate", "OS_BUILD_DATE");
        genericColumnsMap.put("batteryLevel", "BATTERY_LEVEL");
        genericColumnsMap.put("internalTotalMemory", "INTERNAL_TOTAL_MEMORY");
        genericColumnsMap.put("internalAvailableMemory", "INTERNAL_AVAILABLE_MEMORY");
        genericColumnsMap.put("externalTotalMemory", "EXTERNAL_TOTAL_MEMORY");
        genericColumnsMap.put("externalAvailableMemory", "EXTERNAL_AVAILABLE_MEMORY");
        genericColumnsMap.put("connectionType", "CONNECTION_TYPE");
        genericColumnsMap.put("ssid", "SSID");
        genericColumnsMap.put("cpuUsage", "CPU_USAGE");
        genericColumnsMap.put("totalRAMMemory", "TOTAL_RAM_MEMORY");
        genericColumnsMap.put("availableRAMMemory", "AVAILABLE_RAM_MEMORY");
        genericColumnsMap.put("pluggedIn", "PLUGGED_IN");


        locationColumnsMap.put("latitude", "LATITUDE");
        locationColumnsMap.put("longitude", "LONGITUDE");
        locationColumnsMap.put("street1", "STREET1");
        locationColumnsMap.put("street2", "STREET2");
        locationColumnsMap.put("city", "CITY");
        locationColumnsMap.put("state", "ZIP");
        locationColumnsMap.put("zip", "STATE");
        locationColumnsMap.put("country", "COUNTRY");

        //=, >, <, >=, <=, <>, !=, !>, !<
        operators.put("=", "=");
        operators.put(">", ">");
        operators.put("<", "<");
        operators.put(">=", ">=");
        operators.put("<=", "<=");
        operators.put("<>", "<>");
        operators.put("!=", "!=");
        operators.put("!>", "!>");
        operators.put("!<", "!<");
        operators.put("%", "%");

    }

    public static boolean checkColumnType(String column) {

        boolean bool = false;

        switch (column) {
            case "deviceModel":
                bool = true;
                break;
            case "vendor":
                bool = true;
                break;
            case "osVersion":
                bool = true;
                break;
            case "connectionType":
                bool = true;
                break;
            case "ssid":
                bool = true;
                break;
            default:
                bool = false;
                break;
        }

        return bool;
    }

    public static String getConvertedValue(String column, String value) {

        if (checkColumnType(column)) {
            return "\'" + value + "\'";
        } else return value;

    }

    public static Map<String, String> getDeviceDetailsColumnNames() {
        return genericColumnsMap;
    }

    public static Map<String, String> getDeviceLocationColumnNames() {
        return locationColumnsMap;
    }

    public static boolean checkDeviceDetailsColumns(String str) {
        return genericColumnsMap.containsKey(str) || genericColumnsMap.containsValue(str);
    }

    public static boolean checkDeviceLocationColumns(String str) {
        return locationColumnsMap.containsKey(str) || locationColumnsMap.containsValue(str);
    }

    public static List<String> convertStringToList(String str) {

        List<String> stList = new ArrayList<>();
        stList.add(str);
        return stList;
    }

    public static Integer[] getArrayOfDeviceIds(List<Device> devices) {
        Integer[] arr = new Integer[devices.size()];
        int x = 0;
        for (Device device : devices) {
            arr[x] = device.getId();
            x++;
        }
        return arr;
    }


    public static String getDeviceIdsAsString(List<Device> devices) {

        String str = "";
        for (Device device : devices) {
            str += device.getId() + ",";
        }
        if (devices.isEmpty()) {
            return null;
        }
        return str.substring(0, str.length() - 1);
    }


    public static boolean validateOperators(List<Condition> conditions) {
        for (Condition con : conditions) {
            if (!operators.containsKey(con.getOperator())) {
                return false;
            }
        }
        return true;
    }

}

