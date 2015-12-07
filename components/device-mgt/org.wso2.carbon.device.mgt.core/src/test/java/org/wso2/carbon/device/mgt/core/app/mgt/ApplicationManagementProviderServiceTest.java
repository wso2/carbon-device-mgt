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
package org.wso2.carbon.device.mgt.core.app.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.core.DeviceManagementPluginRepository;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;

import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementProviderServiceTest {

    private ApplicationManagementProviderService appMgtProvider;
    private static final Log log = LogFactory.getLog(ApplicationManagementProviderServiceTest.class);
    private DeviceManagementPluginRepository deviceManagementPluginRepository = null;

    @BeforeClass
    public void init() {
        deviceManagementPluginRepository = new DeviceManagementPluginRepository();
        TestDeviceManagementService testDeviceManagementService = new TestDeviceManagementService(
                TestDataHolder.TEST_DEVICE_TYPE, TestDataHolder.SUPER_TENANT_DOMAIN);
        try {
            deviceManagementPluginRepository.addDeviceManagementProvider(testDeviceManagementService);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while initiate plugins '" + TestDataHolder.TEST_DEVICE_TYPE + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
    }

    @Test
    public void updateApplicationTest() {

        List<Application> applications = new ArrayList<>();

        Application application1 = TestDataHolder.generateApplicationDummyData("org.wso2.app1");
        Application application2 = TestDataHolder.generateApplicationDummyData("org.wso2.app2");
        Application application3 = TestDataHolder.generateApplicationDummyData("org.wso2.app3");
        Application application4 = TestDataHolder.generateApplicationDummyData("org.wso2.app4");

        applications.add(application1);
        applications.add(application2);
        applications.add(application3);
        applications.add(application4);

        Device device = TestDataHolder.initialTestDevice;

        if (device == null) {
            throw new IllegalStateException("Device information is not available");
        }
        DeviceIdentifier deviceId = new DeviceIdentifier();

        String deviceIdentifier = TestDataHolder.initialDeviceIdentifier;
        if (deviceIdentifier == null) {
            throw new IllegalStateException("Device identifier is not available");
        }
        deviceId.setId(deviceIdentifier);
        deviceId.setType(device.getType());

        AppManagementConfig appManagementConfig = new AppManagementConfig();
        appMgtProvider = new ApplicationManagerProviderServiceImpl();

        try {
            appMgtProvider.updateApplicationListInstalledInDevice(deviceId, applications);
        } catch (ApplicationManagementException appMgtEx) {
            String msg = "Error occurred while updating app list '" + TestDataHolder.TEST_DEVICE_TYPE + "'";
            log.error(msg, appMgtEx);
            Assert.fail(msg, appMgtEx);
        }

        Application application5 = TestDataHolder.generateApplicationDummyData("org.wso2.app5");
        applications = new ArrayList<>();
        applications.add(application4);
        applications.add(application3);
        applications.add(application5);

        try {
            appMgtProvider.updateApplicationListInstalledInDevice(deviceId, applications);
            List<Application> installedApps = appMgtProvider.getApplicationListForDevice(deviceId);
            log.info("Number of installed applications:" + installedApps.size());
            Assert.assertEquals(installedApps.size(), 3, "Num of installed applications should be two");
        } catch (ApplicationManagementException appMgtEx) {
            String msg = "Error occurred while updating app list '" + TestDataHolder.TEST_DEVICE_TYPE + "'";
            log.error(msg, appMgtEx);
            Assert.fail(msg, appMgtEx);
        }

    }

}
