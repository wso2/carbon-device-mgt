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

    //private static ;

    public static List<Device> getDeviceList(DeviceType deviceType) {

        List<Device> deviceList = new ArrayList<Device>();

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

//        Device device1 = new Device();
//        device1.setId(2);
//        device1.setType(deviceType.getName());
//        device1.setName("Nexus 5");
//        device1.setDeviceIdentifier("def456");
//        EnrolmentInfo enrolmentInfo1 = new EnrolmentInfo();
//        enrolmentInfo1.setOwner("Manoj");
//        enrolmentInfo1.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
//        enrolmentInfo1.setStatus(EnrolmentInfo.Status.ACTIVE);
//        device1.setEnrolmentInfo(enrolmentInfo);

        deviceList.add(device);
       // deviceList.add(device1);

        return deviceList;
    }


    public static List<Device> getDeviceList2 (DeviceType deviceType) {
        List<Device> deviceList = new ArrayList<Device>();

        Device device = new Device();
        device.setId(2);
        device.setType(deviceType.getName());
        device.setName("Apple 5S");
        device.setDeviceIdentifier("def123");
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner("Dilshan");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);

        deviceList.add(device);
        return deviceList;
    }

    public static List<Device> getDeviceList3 (DeviceType deviceType) {
        List<Device> deviceList = new ArrayList<Device>();

        Device device = new Device();
        device.setId(3);
        device.setType(deviceType.getName());
        device.setName("Apple 6 Large");
        device.setDeviceIdentifier("xxxx123");
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner("Harshan");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);

        deviceList.add(device);
        return deviceList;
    }

    public static List<Device> getDeviceList4 (DeviceType deviceType) {
        List<Device> deviceList = new ArrayList<Device>();

        Device device = new Device();
        device.setId(4);
        device.setType(deviceType.getName());
        device.setName("HTC M");
        device.setDeviceIdentifier("ppp456");
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner("Dilan");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);

        deviceList.add(device);
        return deviceList;
    }

    public static List<Device> getDeviceList5 (DeviceType deviceType) {
        List<Device> deviceList = new ArrayList<Device>();

        Device device = new Device();
        device.setId(5);
        device.setType(deviceType.getName());
        device.setName("Sony Experia L");
        device.setDeviceIdentifier("ssss123");
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner("Milan");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);

        deviceList.add(device);
        return deviceList;
    }


    public static List<Device> getDeviceList6 (DeviceType deviceType) {
        List<Device> deviceList = new ArrayList<Device>();

        Device device = new Device();
        device.setId(6);
        device.setType(deviceType.getName());
        device.setName("Alcatel RTS");
        device.setDeviceIdentifier("ttt123");
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner("Dileesha");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);

        deviceList.add(device);
        return deviceList;
    }


    public static Device getSingleDevice() {

        Device device = new Device();
        device.setId(1);
        device.setType("android");
        device.setName("Galaxy S6");
        device.setDeviceIdentifier("abc123");
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner("Geeth");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);


        return device;
    }

}
