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
import org.wso2.carbon.device.mgt.core.search.mgt.Constants;
import org.wso2.carbon.device.mgt.core.search.mgt.ResultSetAggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetAggregatorImpl implements ResultSetAggregator {

    @Override
    public List<Device> aggregate(Map<String, List<Device>> devices) {

        Map<Integer, Device> generalQueryMap = this.convertToMap(devices.get(Constants.GENERAL));
        Map<Integer, Device> andMap = this.convertToMap(devices.get(Constants.PROP_AND));
        Map<Integer, Device> orMap = this.convertToMap(devices.get(Constants.PROP_OR));
        Map<Integer, Device> locationMap = this.convertToMap(devices.get(Constants.LOCATION));
        Map<Integer, Device> finalMap = new HashMap<>();
        List<Device> finalResult = new ArrayList<>();

        if (andMap.isEmpty()) {
            finalMap = generalQueryMap;
            finalResult = this.convertDeviceMapToList(generalQueryMap);
        } else {
            for (Integer a : andMap.keySet()) {
                if (generalQueryMap.isEmpty()) {
                    finalResult.add(andMap.get(a));
                    finalMap.put(a, andMap.get(a));
                } else if (generalQueryMap.containsKey(a)) {
                    if (!finalMap.containsKey(a)) {
                        finalResult.add(andMap.get(a));
                        finalMap.put(a, andMap.get(a));
                    }
                }
            }
        }
        for (Integer a : orMap.keySet()) {
            if (!finalMap.containsKey(a)) {
                finalResult.add(orMap.get(a));
                finalMap.put(a, orMap.get(a));
            }
        }

        for (Integer a : locationMap.keySet()) {
            if (!finalMap.containsKey(a)) {
                finalResult.add(locationMap.get(a));
                finalMap.put(a, locationMap.get(a));
            }
        }

        return finalResult;
    }

    private Map<Integer, Device> convertToMap(List<Device> devices) {
        if (devices == null) {
            return null;
        }
        Map<Integer, Device> deviceWrapperMap = new HashMap<>();
        for (Device device : devices) {
            deviceWrapperMap.put(device.getId(), device);
        }
        return deviceWrapperMap;
    }

    private List<Device> convertDeviceMapToList(Map<Integer, Device> map) {
        List<Device> list = new ArrayList<>();
        for (Integer a : map.keySet()) {
            list.add(map.get(a));
        }
        return list;
    }

}
