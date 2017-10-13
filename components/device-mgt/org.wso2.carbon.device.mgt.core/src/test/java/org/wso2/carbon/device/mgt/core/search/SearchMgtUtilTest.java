/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.search;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.search.mgt.impl.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds the Unit test cases to test org.wso2.carbon.device.mgt.core.search.mgt.impl.Util
 * */
public class SearchMgtUtilTest {

    private static List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
    private static final String DEVICE_ID_PREFIX = "SEARCH-DEVICE-ID-";
    private static final String DEVICE_TYPE = "SEARCH_TYPE";
    private static final String DEVICE_IDS = "0,0,0,0,0";
    private static final Integer[] DEVICE_IDS_INT = {0,0,0,0,0};
    private List<Device> devices;

    @BeforeClass
    public void init() throws Exception {
        for (int i = 0; i < 5; i++) {
            deviceIdentifiers.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        devices = TestDataHolder.generateDummyDeviceData(deviceIdentifiers);
    }

    @Test(description = "Test for converting given devices list to string")
    public void testConvertDeviceListToString() {
        String ids = Utils.getDeviceIdsAsString(devices);
        Assert.assertEquals(ids, DEVICE_IDS);
    }

    @Test(description = "Test for get all the device ids as an array")
    public void testGetArrayOfDeviceIds() {
        Integer[] deviceIds = Utils.getArrayOfDeviceIds(devices);
        Assert.assertArrayEquals(deviceIds, DEVICE_IDS_INT);
    }

    @Test(description = "Test to convert given String to a List")
    public void testConvertStringToList() {
        List<String> stringList = Utils.convertStringToList("some string");
        List<String> expected = this.getStringList();

        Assert.assertEquals(stringList, expected);
    }

    @Test(description = "Test to check what type of data the specified column can hold")
    public void testColumnTypes() {
        Map<String, String> colTypes = this.buildColumnMap();

        for (String key : colTypes.keySet()) {
            String result = Utils.checkColumnType(key);
            Assert.assertEquals(result, colTypes.get(key));
        }
    }

    /**
     * Generates a map of columns and particular data type.
     * @return HashMap of column name and data type.
     * */
    private Map<String, String> buildColumnMap() {
        Map<String, String> columnTypes = new HashMap<>();

        columnTypes.put("deviceModel", "String");
        columnTypes.put("vendor", "String");
        columnTypes.put("osVersion", "String");
        columnTypes.put("connectionType", "String");
        columnTypes.put("ssid", "String");
        columnTypes.put("imei", "String");
        columnTypes.put("imsi", "String");
        columnTypes.put("batteryLevel", "Double");
        columnTypes.put("externalAvailableMemory", "Double");
        columnTypes.put("externalTotalMemory", "Double");
        columnTypes.put("internalAvailableMemory", "Double");
        columnTypes.put("cpuUsage", "Double");
        columnTypes.put("someProperty", "String");
        return columnTypes;
    }

    /**
     * Generates a list of Strings.
     * @return List<String>
     * */
    private List<String> getStringList() {
        List<String> strings = new ArrayList<>();
        strings.add("some string");
        return strings;
    }
}
