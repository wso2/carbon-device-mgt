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
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.operation.TestNotificationStrategy;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DeviceManagementProviderServiceTest extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceManagementProviderServiceTest.class);
    private DeviceManagementProviderService providerService;
    private static final String DEVICE_TYPE = "RANDOM_DEVICE_TYPE";
    private DeviceDetailsDAO deviceDetailsDAO = DeviceManagementDAOFactory.getDeviceDetailsDAO();

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
    public void testGetAvailableDeviceTypes() {
        try {
            List<DeviceType> deviceTypes = deviceMgtService.getDeviceTypes();
            Assert.assertTrue(deviceTypes.size() > 0);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting the device types";
            Assert.fail(msg, e);
        }
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
    public void testSuccessfulDeviceEnrollment() {
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);
        try {
            boolean enrollmentStatus = deviceMgtService.enrollDevice(device);
            Assert.assertTrue(enrollmentStatus);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while enrolling device";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = "testSuccessfulDeviceEnrollment")
    public void testIsEnrolled() {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(TestDataHolder.initialDeviceIdentifier);
            deviceIdentifier.setType(DEVICE_TYPE);
            boolean enrollmentStatus = deviceMgtService.isEnrolled(deviceIdentifier);
            Assert.assertTrue(enrollmentStatus);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while checking enrollment status.";
            Assert.fail(msg, e);
        }
    }

    @Test
    public void testIsEnrolledForNonExistingDevice() {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId("34535235235235235");
            deviceIdentifier.setType(DEVICE_TYPE);
            boolean enrollmentStatus = deviceMgtService.isEnrolled(deviceIdentifier);
            Assert.assertFalse(enrollmentStatus);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while checking enrollment status.";
            Assert.fail(msg, e);
        }
    }

    @Test(expectedExceptions = DeviceManagementException.class)
    public void testIsEnrolledForNullDevice() throws DeviceManagementException {
        deviceMgtService.isEnrolled(null);
    }

    @Test
    public void testNonExistentDeviceType() {
        Device device = TestDataHolder.generateDummyDeviceData("abc");
        try {
            boolean enrollmentStatus = deviceMgtService.enrollDevice(device);
            Assert.assertFalse(enrollmentStatus);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while enrolling device";
            Assert.fail(msg, e);
        }
    }


    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testReEnrollmentofSameDeviceUnderSameUser() {
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);

        try {
            boolean enrollment = deviceMgtService.enrollDevice(device);

            Assert.assertTrue(enrollment);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while enrolling device";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testReEnrollmentofSameDeviceUnderSameUser"})
    public void testReEnrollmentofSameDeviceWithOtherUser() {

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(new Date().getTime());
        enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
        enrolmentInfo.setOwner("user1");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.CREATED);

        Device alternateDevice = TestDataHolder.generateDummyDeviceData("12345", DEVICE_TYPE,
                enrolmentInfo);

        try {
            Device retrievedDevice1 = deviceMgtService.getDevice(new DeviceIdentifier("12345", DEVICE_TYPE));

            deviceMgtService.enrollDevice(alternateDevice);
            Device retrievedDevice2 = deviceMgtService.getDevice(new DeviceIdentifier(alternateDevice
                    .getDeviceIdentifier(), alternateDevice.getType()));

            log.info(retrievedDevice1.getEnrolmentInfo().getOwner());
            log.info(retrievedDevice2.getEnrolmentInfo().getOwner());

            Assert.assertFalse(retrievedDevice1.getEnrolmentInfo().getOwner().equalsIgnoreCase
                    (retrievedDevice2.getEnrolmentInfo().getOwner()));
        } catch (DeviceManagementException e) {
            String msg = "Error Occured while enrolling device";
            Assert.fail(msg, e);
        }
    }


    @Test(dependsOnMethods = {"testReEnrollmentofSameDeviceWithOtherUser"})
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
            String msg = "Error occurred while enrolling device";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceCount() {
        try {
            int count = deviceMgtService.getDeviceCount();
            Assert.assertTrue(count > 0);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting the device count";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceCountForUser() {
        try {
            int count = deviceMgtService.getDeviceCount(TestDataHolder.OWNER);
            Assert.assertTrue(count > 0);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting the device count";
            Assert.fail(msg, e);
        }
    }

    @Test
    public void testGetDeviceCountForNonExistingUser() {
        try {
            int count = deviceMgtService.getDeviceCount("ABCD");
            Assert.assertEquals(count, 0);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting the device count";
            Assert.fail(msg, e);
        }
    }

    @Test(expectedExceptions = DeviceManagementException.class)
    public void testGetDeviceCountForNullUser() throws DeviceManagementException {
        deviceMgtService.getDeviceCount(null);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testIsActive() {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(TestDataHolder.initialDeviceIdentifier);
            deviceIdentifier.setType(DEVICE_TYPE);
            Assert.assertTrue(deviceMgtService.isActive(deviceIdentifier));
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while checking the device status";
            Assert.fail(msg, e);
        }
    }

    @Test
    public void testIsActiveForNonExistingDevice() {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId("34535235235235235");
            deviceIdentifier.setType("TEST_TYPE");
            Assert.assertFalse(deviceMgtService.isActive(deviceIdentifier));
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while checking the device status";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testSetActive() {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(TestDataHolder.initialDeviceIdentifier);
            deviceIdentifier.setType(DEVICE_TYPE);
            Assert.assertFalse(deviceMgtService.setActive(deviceIdentifier, true));
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }

    @Test
    public void testSetActiveForNonExistingDevice() {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId("34535235235235235");
            deviceIdentifier.setType("TEST_TYPE");
            Assert.assertFalse(deviceMgtService.setActive(deviceIdentifier, true));
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status for non-existing device";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceEnrolledTenants() {
        try {
            List<Integer> tenants = deviceMgtService.getDeviceEnrolledTenants();
            Assert.assertEquals(tenants.size(), 1);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDevice() {
        try {
            Device device = deviceMgtService.getDevice(new DeviceIdentifier("12345",DEVICE_TYPE));
            Assert.assertTrue(device.getDeviceIdentifier().equalsIgnoreCase("12345"));
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }


    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceWithInfo() {
        try {
            Device device = deviceMgtService.getDevice(new DeviceIdentifier("12345", DEVICE_TYPE)
                    , true);
            Assert.assertTrue(device.getDeviceInfo() != null);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceWithOutInfo() {
        try {
            Device device = deviceMgtService.getDevice(new DeviceIdentifier("12345", DEVICE_TYPE)
                    , false);
            Assert.assertTrue(device.getDeviceInfo() == null);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevicesOfRole() {
        try {
            List<Device> devices = deviceMgtService.getAllDevicesOfRole("admin");
            Assert.assertTrue(devices.size() > 0);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testDeviceByOwner() {
        try {
            Device device = deviceMgtService.getDevice(new DeviceIdentifier("12345",
                    DEVICE_TYPE), "admin", true);
            Assert.assertTrue(device != null);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testDeviceByDate() {
        try {
            Device initialDevice = deviceMgtService.getDevice(new DeviceIdentifier("12345",
                    DEVICE_TYPE));

            DeviceManagementDAOFactory.beginTransaction();

            //Device details table will be reffered when looking for last updated time
            //This dao entry is to mimic a device info operation
            deviceDetailsDAO.addDeviceInformation(initialDevice.getId(), TestDataHolder
                    .generateDummyDeviceInfo());
        } catch (DeviceManagementException e) {
            e.printStackTrace();
        } catch (TransactionManagementException e) {
            e.printStackTrace();
        } catch (DeviceDetailsMgtDAOException e) {
            e.printStackTrace();
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        try {
            Device device = deviceMgtService.getDevice(new DeviceIdentifier("12345",
                    DEVICE_TYPE), yesterday());
            Assert.assertTrue(device != null);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"testDeviceByDate"})
    public void testDeviceByDateAndOwner() {
        try {
            Device device = deviceMgtService.getDevice(new DeviceIdentifier("12345",
                    DEVICE_TYPE), "admin", yesterday(), true);
            Assert.assertTrue(device != null);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device status";
            Assert.fail(msg, e);
        }
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }


}