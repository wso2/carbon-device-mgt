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
import org.wso2.carbon.device.mgt.core.search.mgt.Constants;
import org.wso2.carbon.device.mgt.core.search.mgt.ResultSetAggregator;

import java.util.*;

public class ResultSetAggregatorImpl implements ResultSetAggregator {

    @Override
    public List<DeviceWrapper> aggregate(Map<String, List<DeviceWrapper>> deviceWrappers) {

        Map<Integer, DeviceWrapper> generalQueryMap = this.convertToMap(deviceWrappers.get(Constants.GENERAL));
        Map<Integer, DeviceWrapper> andMap = this.convertToMap(deviceWrappers.get(Constants.PROP_AND));
        Map<Integer, DeviceWrapper> orMap = this.convertToMap(deviceWrappers.get(Constants.PROP_OR));
        Map<Integer, DeviceWrapper> locationMap = this.convertToMap(deviceWrappers.get(Constants.LOCATION));
        Map<Integer, DeviceWrapper> finalMap = new HashMap<>();
        List<DeviceWrapper> finalResult = new ArrayList<>();

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

    private Map<Integer, DeviceWrapper> convertToMap(List<DeviceWrapper> deviceWrappers) {

        if (deviceWrappers == null) {
            return null;
        }
        Map<Integer, DeviceWrapper> deviceWrapperMap = new HashMap<>();
        for (DeviceWrapper dw : deviceWrappers) {
            deviceWrapperMap.put(dw.getDevice().getId(), dw);
        }
        return deviceWrapperMap;
    }

    private List<DeviceWrapper> convertDeviceMapToList(Map<Integer, DeviceWrapper> map) {
        List<DeviceWrapper> list = new ArrayList<>();

        for (Integer a : map.keySet()) {
            list.add(map.get(a));
        }
        return list;
    }
}
