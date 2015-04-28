/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.policy.mgt.core.util;

import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.dto.Status;

import java.util.ArrayList;
import java.util.List;

public class DeviceCreator {

    private static List<Device> deviceList = new ArrayList<Device>();

    public static List<Device> getDeviceList(DeviceType deviceType) {


        Device device = new Device();
        device.setId(1);
        device.setDeviceTypeId(1);
        device.setName("Galaxy S6");
        device.setOwnerId("geeth");
        device.setOwnerShip("BYOD");
        device.setTenantId(-1234);
        device.setDeviceType(deviceType);
        device.setStatus(Status.ACTIVE);
        device.setDeviceIdentificationId("aaaaaaaaaaaaaaaaaaaaaaaaaa");


        Device device2 = new Device();
        device2.setId(2);
        device2.setDeviceTypeId(1);
        device2.setName("Nexus 5");
        device2.setOwnerId("manoj");
        device2.setOwnerShip("BYOD");
        device2.setTenantId(-1234);
        device2.setDeviceType(deviceType);
        device.setStatus(Status.ACTIVE);
        device2.setDeviceIdentificationId("bbbbbbbbbbbbbbbbbbbbbbbb");


        deviceList.add(device);
        // deviceList.add(device2);

        return deviceList;

    }

    public static Device getSingleDevice() {
        return deviceList.get(0);
    }


}
