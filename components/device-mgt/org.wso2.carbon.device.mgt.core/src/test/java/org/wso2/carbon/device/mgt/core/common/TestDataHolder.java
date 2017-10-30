/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.device.mgt.core.common;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.Date;
import java.util.Properties;

public class TestDataHolder {

    public final static String TEST_DEVICE_TYPE = "Test";
    public final static Integer SUPER_TENANT_ID = -1234;
    public final static String SUPER_TENANT_DOMAIN = "carbon.super";
    public final static String initialDeviceIdentifier = "12345";
    public final static String OWNER = "admin";
    public static Device initialTestDevice;
    public static DeviceType initialTestDeviceType;

    public static Device generateDummyDeviceData(String deviceType){

        Device device = new Device();
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(new Date().getTime());
        enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
        enrolmentInfo.setOwner(OWNER);
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.CREATED);
        device.setEnrolmentInfo(enrolmentInfo);
        device.setDescription("Test Description");
        device.setDeviceIdentifier(initialDeviceIdentifier);
        device.setType(deviceType);
        return device;
    }

    public static DeviceType generateDeviceTypeData(String devTypeName){
        DeviceType deviceType = new DeviceType();
        deviceType.setName(devTypeName);
        return deviceType;
    }

    public static Application generateApplicationDummyData(String appIdentifier){

        Application application = new Application();
        Properties properties = new Properties();
        properties.setProperty("test1","testVal");

        application.setName("SimpleCalculator");
        application.setCategory("TestCategory");
        application.setApplicationIdentifier(appIdentifier);
        application.setType("TestType");
        application.setVersion("1.0.0");
        application.setImageUrl("http://test.org/image/");
        application.setLocationUrl("http://test.org/location/");
        application.setAppProperties(properties);

        return application;
    }

    public static DeviceGroup generateDummyGroupData() {
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.setName("Test device group");
        deviceGroup.setDescription("Test description");
        deviceGroup.setOwner(OWNER);
        return deviceGroup;
    }
}
