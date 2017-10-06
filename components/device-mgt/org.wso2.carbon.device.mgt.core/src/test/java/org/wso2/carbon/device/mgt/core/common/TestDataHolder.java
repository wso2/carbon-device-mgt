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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TestDataHolder {

    public final static String TEST_DEVICE_TYPE = "TEST-DEVICE-TYPE";
    public final static Integer SUPER_TENANT_ID = -1234;
    public final static String SUPER_TENANT_DOMAIN = "carbon.super";
    public final static String initialDeviceIdentifier = "12345";
    public final static String OWNER = "admin";
    public static final String OPERATION_CONFIG = "TEST-OPERATION-";
    public static Device initialTestDevice;
    public static DeviceType initialTestDeviceType;

    public static Device generateDummyDeviceData(String deviceType) {
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

    public static DeviceInfo generateDummyDeviceInfo() {
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

    public static Notification getNotification(int notificationId, String status, String deviceId,
                                               String description, String deviceName, int operationId,
                                               String deviceType) {
        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setStatus(status);
        notification.setDeviceIdentifier(deviceId);
        notification.setDescription(description);
        notification.setDeviceName(deviceName);
        notification.setOperationId(operationId);
        notification.setDeviceType(deviceType);
        return notification;
    }

    public static Device generateDummyDeviceData(String deviceIdentifier, String deviceType,
                                                 EnrolmentInfo enrolmentInfo) {
        Device device = new Device();
        device.setEnrolmentInfo(enrolmentInfo);
        device.setDescription("Test Description");
        device.setDeviceIdentifier(deviceIdentifier);
        device.setType(deviceType);
        return device;
    }

    public static List<Device> generateDummyDeviceData(List<DeviceIdentifier> deviceIds) {
        List<Device> devices = new ArrayList<>();
        for (DeviceIdentifier deviceId : deviceIds) {
            Device device = new Device();
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setOwner(OWNER);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            enrolmentInfo.setStatus(EnrolmentInfo.Status.CREATED);
            device.setEnrolmentInfo(enrolmentInfo);
            device.setDescription("Test Description");
            device.setDeviceIdentifier(deviceId.getId());
            device.setType(deviceId.getType());
            devices.add(device);
        }
        return devices;
    }

    public static Device generateDummyDeviceData(DeviceIdentifier deviceIdentifier) {
            Device device = new Device();
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setOwner(OWNER);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            enrolmentInfo.setStatus(EnrolmentInfo.Status.CREATED);
            device.setEnrolmentInfo(enrolmentInfo);
            device.setDescription("Test Description");
            device.setDeviceIdentifier(deviceIdentifier.getId());
            device.setType(deviceIdentifier.getType());
        return device;
    }

    public static DeviceType generateDeviceTypeData(String devTypeName) {
        DeviceType deviceType = new DeviceType();
        deviceType.setName(devTypeName);
        return deviceType;
    }

    public static Application generateApplicationDummyData(String appIdentifier) {
        Application application = new Application();
        Properties properties = new Properties();
        properties.setProperty("test1", "testVal");
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

    public static OperationMonitoringTaskConfig generateMonitoringTaskConfig(boolean enabled, int frequency,
            int numberOfOperations) {
        OperationMonitoringTaskConfig taskConfig = new OperationMonitoringTaskConfig();
        List<MonitoringOperation> operationList = new ArrayList<>();

        while (--numberOfOperations >= 0) {
            operationList.add(generateMonitoringOperation(OPERATION_CONFIG + String.valueOf(numberOfOperations),
                    1 + (int) (Math.random() * 4)));
        }

        taskConfig.setEnabled(enabled);
        taskConfig.setFrequency(frequency);
        taskConfig.setMonitoringOperation(operationList);

        return taskConfig;
    }

    private static MonitoringOperation generateMonitoringOperation(String name, int recurrentTimes) {
        MonitoringOperation operation = new MonitoringOperation();
        operation.setTaskName(name);
        operation.setRecurrentTimes(recurrentTimes);

        return operation;
    }
}
