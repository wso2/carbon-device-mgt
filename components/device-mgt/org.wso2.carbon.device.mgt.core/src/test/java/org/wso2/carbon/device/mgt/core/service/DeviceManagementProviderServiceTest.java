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
package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;

public class DeviceManagementProviderServiceTest extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceManagementProviderServiceTest.class);
    private DeviceManagementProviderService providerService;
    private static final String DEVICE_TYPE = "RANDOM_DEVICE_TYPE";

    DeviceManagementProviderService deviceMgtService;

    @BeforeClass
    public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing");

        deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(new GroupManagementProviderServiceImpl());
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));

    }

    private RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    @Test
    public void testNullDeviceEnrollment() {
        try {
            boolean enrollmentStatus = deviceMgtService.enrollDevice(null);
        } catch (DeviceManagementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testSuccessfullDeviceEnrollment() {
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);
        try {
            boolean enrollmentStatus = deviceMgtService.enrollDevice(device);
            Assert.assertTrue(enrollmentStatus);
        } catch (DeviceManagementException e) {
            String msg = "Error Occured while enrolling device";
            Assert.fail(msg, e);
        }
    }

    @Test
    public void testNonExistentDeviceType() {
        Device device = TestDataHolder.generateDummyDeviceData("abc");
        try {
            boolean enrollmentStatus = deviceMgtService.enrollDevice(device);
            Assert.assertFalse(enrollmentStatus);
        } catch (DeviceManagementException e) {
            String msg = "Error Occured while enrolling device";
            Assert.fail(msg, e);
        }
    }


    @Test(dependsOnMethods = {"testSuccessfullDeviceEnrollment"})
    public void testReEnrollmentofSameDeviceUnderSameUser() {
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);

        try {
            boolean enrollment = deviceMgtService.enrollDevice(device);

            Assert.assertTrue(enrollment);
        } catch (DeviceManagementException e) {
            String msg = "Error Occured while enrolling device";
            Assert.fail(msg, e);
        }
    }

//    @Test(dependsOnMethods = {"testReEnrollmentofSameDeviceUnderSameUser"})
//    public void testReEnrollmentofSameDeviceWithOtherUser() {
//
//        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
//        enrolmentInfo.setDateOfEnrolment(new Date().getTime());
//        enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
//        enrolmentInfo.setOwner("user1");
//        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
//        enrolmentInfo.setStatus(EnrolmentInfo.Status.CREATED);
//
//        Device alternateDevice = TestDataHolder.generateDummyDeviceData("12345", DEVICE_TYPE,
//                enrolmentInfo);
//
//        try {
//            Device retrievedDevice1 = deviceMgtService.getDevice(new DeviceIdentifier("12345", DEVICE_TYPE));
//
//            deviceMgtService.enrollDevice(alternateDevice);
//            Device retrievedDevice2 = deviceMgtService.getDevice(new DeviceIdentifier(alternateDevice
//                    .getDeviceIdentifier(), alternateDevice.getType()));
//
//            log.info(retrievedDevice1.getEnrolmentInfo().getOwner());
//            log.info(retrievedDevice2.getEnrolmentInfo().getOwner());
//
//            Assert.assertFalse(retrievedDevice1.getEnrolmentInfo().getOwner().equalsIgnoreCase
//                    (retrievedDevice2.getEnrolmentInfo().getOwner()));
//        } catch (DeviceManagementException e) {
//            String msg = "Error Occured while enrolling device";
//            Assert.fail(msg, e);
//        }
//    }


    @Test(dependsOnMethods = {"testReEnrollmentofSameDeviceUnderSameUser"})
    public void testDisenrollment() {
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);

        try {
            boolean disenrollmentStatus = deviceMgtService.disenrollDevice(new DeviceIdentifier
                    (device
                            .getDeviceIdentifier(),
                            device.getType()));
            log.info(disenrollmentStatus);

            Assert.assertTrue(disenrollmentStatus);
        } catch (DeviceManagementException e) {
            String msg = "Error Occured while enrolling device";
            Assert.fail(msg, e);
        }
    }

}
