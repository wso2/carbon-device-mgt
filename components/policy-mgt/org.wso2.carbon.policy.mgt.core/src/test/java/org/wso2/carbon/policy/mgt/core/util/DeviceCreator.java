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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.ArrayList;
import java.util.List;

public class DeviceCreator {

    private static List<Device> deviceList = new ArrayList<Device>();

    public static List<Device> getDeviceList(DeviceType deviceType) {

        Device device = new Device();
        device.setId(1);
        device.setType(deviceType.getName());
        device.setName("Galaxy S6");
        device.setDeviceIdentifier("abc123");
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner("Geeth");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);

        Device device1 = new Device();
        device1.setId(1);
        device1.setType(deviceType.getName());
        device1.setName("Nexus 5");
        device1.setDeviceIdentifier("def456");
        EnrolmentInfo enrolmentInfo1 = new EnrolmentInfo();
        enrolmentInfo1.setOwner("Manoj");
        enrolmentInfo1.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo1.setStatus(EnrolmentInfo.Status.ACTIVE);
        device1.setEnrolmentInfo(enrolmentInfo);

        deviceList.add(device);
        // deviceList.add(device2);

        return deviceList;
    }

    public static Device getSingleDevice() {
        return deviceList.get(0);
    }

}
