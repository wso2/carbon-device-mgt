/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.util;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Helper class for Device Management API test cases.
 */
public class DeviceMgtAPITestHelper {

    private static final String DEVICE_TYPE_DESCRIPTION = "Dummy Description";
    public static final String DEVICE_TYPE = "TEST_DEVICE_TYPE";
    public static final String DEVICE_NAME = "TEST_DEVICE";
    public final static String OWNER = "admin";

    /**
     * Creates a Device Type with given name and given id.
     * If the name is null, the TEST_DEVICE_TYPE will be used as the name.
     *
     * @param name         : Name of the device type.
     * @param deviceTypeId : The Id of the device type.
     * @return DeviceType
     */
    public static DeviceType getDummyDeviceType(String name, int deviceTypeId) {
        DeviceType deviceType = new DeviceType();
        deviceType.setId(deviceTypeId);
        deviceType.setName(name != null ? name : DEVICE_TYPE);

        DeviceTypeMetaDefinition deviceTypeMetaDefinition = new DeviceTypeMetaDefinition();
        deviceTypeMetaDefinition.setClaimable(true);
        deviceTypeMetaDefinition.setDescription(DEVICE_TYPE_DESCRIPTION);

        PushNotificationConfig pushNotificationConfig =
                new PushNotificationConfig(name, true, null);
        deviceTypeMetaDefinition.setPushNotificationConfig(pushNotificationConfig);

        deviceType.setDeviceTypeMetaDefinition(deviceTypeMetaDefinition);
        return deviceType;
    }

    /**
     * Generates a list of device types.
     *
     * @param count: The number of device types that is needed.
     * @return List<DeviceType> : A list of device types.
     */
    public static List<DeviceType> getDummyDeviceTypeList(int count) {
        List<DeviceType> deviceTypes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            DeviceType deviceType = getDummyDeviceType(DEVICE_TYPE + count, count);
            deviceTypes.add(deviceType);
        }

        return deviceTypes;
    }

    public static Device generateDummyDevice(String deviceType, String identifier) {
        Device device = new Device();
        device.setEnrolmentInfo(generateEnrollmentInfo(new Date().getTime(), new Date().getTime(), OWNER, EnrolmentInfo
                .OwnerShip.BYOD, EnrolmentInfo.Status.ACTIVE));
        device.setDescription("Test Description");
        device.setDeviceIdentifier(identifier);
        device.setType(deviceType);
        device.setDeviceInfo(generateDeviceInfo());
        device.setName(DEVICE_NAME);
        device.setFeatures(new ArrayList<>());
        device.setProperties(new ArrayList<>());
        return device;
    }

    public static EnrolmentInfo generateEnrollmentInfo(long dateOfEnrollment, long dateOfLastUpdate,
                                                       String owner, EnrolmentInfo.OwnerShip ownership,
                                                       EnrolmentInfo.Status status) {
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(dateOfEnrollment);
        enrolmentInfo.setDateOfLastUpdate(dateOfLastUpdate);
        enrolmentInfo.setOwner(owner);
        enrolmentInfo.setOwnership(ownership);
        enrolmentInfo.setStatus(status);
        return enrolmentInfo;
    }

    public static DeviceInfo generateDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceModel("DUMMY_MODEL");
        deviceInfo.setVendor("WSO2");
        deviceInfo.setOsVersion("OREO");
        deviceInfo.setOsBuildDate("24-05-2017");
        deviceInfo.setBatteryLevel(25.0);
        deviceInfo.setInternalTotalMemory(1.5);
        deviceInfo.setInternalAvailableMemory(2.5);
        deviceInfo.setExternalTotalMemory(16.76);
        deviceInfo.setExternalAvailableMemory(4.56);
        deviceInfo.setConnectionType("CON_TYPE");
        deviceInfo.setSsid("SSID");
        deviceInfo.setCpuUsage(23.5);
        deviceInfo.setTotalRAMMemory(1.5);
        deviceInfo.setAvailableRAMMemory(2.33);
        deviceInfo.setPluggedIn(true);
        return deviceInfo;
    }
}
