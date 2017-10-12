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
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
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
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class DeviceManagementProviderServiceTest extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceManagementProviderServiceTest.class);
    public static final String DEVICE_ID = "9999";
    public static final String ALTERNATE_DEVICE_ID = "1128";
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
    public void testGetAvailableDeviceTypes() throws DeviceManagementException {
        List<DeviceType> deviceTypes = deviceMgtService.getDeviceTypes();
        Assert.assertTrue(deviceTypes.size() > 0);
    }

    @Test(expectedExceptions = DeviceManagementException.class)
    public void testNullDeviceEnrollment() throws DeviceManagementException {
        deviceMgtService.enrollDevice(null);
    }

    @Test
    public void testSuccessfulDeviceEnrollment() throws DeviceManagementException {
        Device device = TestDataHolder.generateDummyDeviceData(new DeviceIdentifier(DEVICE_ID, DEVICE_TYPE));
        boolean enrollmentStatus = deviceMgtService.enrollDevice(device);
        Assert.assertTrue(enrollmentStatus);
    }

    @Test(dependsOnMethods = "testSuccessfulDeviceEnrollment")
    public void testIsEnrolled() throws DeviceManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(DEVICE_ID);
        deviceIdentifier.setType(DEVICE_TYPE);
        boolean enrollmentStatus = deviceMgtService.isEnrolled(deviceIdentifier);
        Assert.assertTrue(enrollmentStatus);
    }

    @Test
    public void testIsEnrolledForNonExistingDevice() throws DeviceManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId("34535235235235235");
        deviceIdentifier.setType(DEVICE_TYPE);
        boolean enrollmentStatus = deviceMgtService.isEnrolled(deviceIdentifier);
        Assert.assertFalse(enrollmentStatus);
    }

    @Test(expectedExceptions = DeviceManagementException.class)
    public void testIsEnrolledForNullDevice() throws DeviceManagementException {
        deviceMgtService.isEnrolled(null);
    }

    @Test
    public void testNonExistentDeviceType() throws DeviceManagementException {
        Device device = TestDataHolder.generateDummyDeviceData("abc");
        boolean enrollmentStatus = deviceMgtService.enrollDevice(device);
        Assert.assertFalse(enrollmentStatus);
    }


    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testReEnrollmentofSameDeviceUnderSameUser() throws DeviceManagementException {
        Device device = TestDataHolder.generateDummyDeviceData(new DeviceIdentifier(DEVICE_ID, DEVICE_TYPE));
        boolean enrollment = deviceMgtService.enrollDevice(device);
        Assert.assertTrue(enrollment);
    }

    @Test(dependsOnMethods = {"testReEnrollmentofSameDeviceUnderSameUser"})
    public void testReEnrollmentofSameDeviceWithOtherUser() throws DeviceManagementException {

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(new Date().getTime());
        enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
        enrolmentInfo.setOwner("user1");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.CREATED);

        Device alternateDevice = TestDataHolder.generateDummyDeviceData(DEVICE_ID, DEVICE_TYPE,
                enrolmentInfo);
        Device retrievedDevice1 = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID,
                DEVICE_TYPE));

        deviceMgtService.enrollDevice(alternateDevice);
        Device retrievedDevice2 = deviceMgtService.getDevice(new DeviceIdentifier(alternateDevice
                .getDeviceIdentifier(), alternateDevice.getType()));

        Assert.assertFalse(retrievedDevice1.getEnrolmentInfo().getOwner().equalsIgnoreCase
                (retrievedDevice2.getEnrolmentInfo().getOwner()));
    }


    @Test(dependsOnMethods = {"testReEnrollmentofSameDeviceWithOtherUser"})
    public void testDisenrollment() throws DeviceManagementException {
        Device device = TestDataHolder.generateDummyDeviceData(new DeviceIdentifier(DEVICE_ID, DEVICE_TYPE));
        boolean disenrollmentStatus = deviceMgtService.disenrollDevice(new DeviceIdentifier
                (device
                        .getDeviceIdentifier(),
                        device.getType()));
        log.info(disenrollmentStatus);

        Assert.assertTrue(disenrollmentStatus);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceCount() throws DeviceManagementException {
        int count = deviceMgtService.getDeviceCount();
        Assert.assertTrue(count > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceCountForUser() throws DeviceManagementException {
        int count = deviceMgtService.getDeviceCount(TestDataHolder.OWNER);
        Assert.assertTrue(count > 0);
    }

    @Test
    public void testGetDeviceCountForNonExistingUser() throws DeviceManagementException {
        int count = deviceMgtService.getDeviceCount("ABCD");
        Assert.assertEquals(count, 0);
    }

    @Test(expectedExceptions = DeviceManagementException.class)
    public void testGetDeviceCountForNullUser() throws DeviceManagementException {
        deviceMgtService.getDeviceCount(null);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testIsActive() throws DeviceManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(TestDataHolder.initialDeviceIdentifier);
        deviceIdentifier.setType(DEVICE_TYPE);
        Assert.assertTrue(deviceMgtService.isActive(deviceIdentifier));
    }

    @Test
    public void testIsActiveForNonExistingDevice() throws DeviceManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId("34535235235235235");
        deviceIdentifier.setType("TEST_TYPE");
        Assert.assertFalse(deviceMgtService.isActive(deviceIdentifier));
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testSetActive() throws DeviceManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(TestDataHolder.initialDeviceIdentifier);
        deviceIdentifier.setType(DEVICE_TYPE);
        Assert.assertFalse(deviceMgtService.setActive(deviceIdentifier, true));
    }

    @Test
    public void testSetActiveForNonExistingDevice() throws DeviceManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId("34535235235235235");
        deviceIdentifier.setType("TEST_TYPE");
        Assert.assertFalse(deviceMgtService.setActive(deviceIdentifier, true));
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceEnrolledTenants() throws DeviceManagementException {
        List<Integer> tenants = deviceMgtService.getDeviceEnrolledTenants();
        Assert.assertEquals(tenants.size(), 1);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDevice() throws DeviceManagementException {
        Device device = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID, DEVICE_TYPE));
        Assert.assertTrue(device.getDeviceIdentifier().equalsIgnoreCase(DEVICE_ID));
    }


    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceWithInfo() throws DeviceManagementException {
        Device device = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID, DEVICE_TYPE)
                , true);
        Assert.assertTrue(device.getDeviceInfo() != null);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviceWithOutInfo() throws DeviceManagementException {
        Device device = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID, DEVICE_TYPE)
                , false);
        Assert.assertTrue(device.getDeviceInfo() == null);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevicesOfRole() throws DeviceManagementException {
        List<Device> devices = deviceMgtService.getAllDevicesOfRole("admin");
        Assert.assertTrue(devices.size() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testDeviceByOwner() throws DeviceManagementException {
        Device device = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID,
                DEVICE_TYPE), "admin", true);
        Assert.assertTrue(device != null);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testDeviceByDate() throws DeviceManagementException, TransactionManagementException, DeviceDetailsMgtDAOException {
        Device initialDevice = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID,
                DEVICE_TYPE));

        DeviceManagementDAOFactory.beginTransaction();

        //Device details table will be reffered when looking for last updated time
        //This dao entry is to mimic a device info operation
        deviceDetailsDAO.addDeviceInformation(initialDevice.getId(), TestDataHolder
                .generateDummyDeviceInfo());

        DeviceManagementDAOFactory.closeConnection();

        Device device = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID,
                DEVICE_TYPE), yesterday());
        Assert.assertTrue(device != null);
    }

    @Test(dependsOnMethods = {"testDeviceByDate"})
    public void testDeviceByDateAndOwner() throws DeviceManagementException {
        Device device = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID,
                DEVICE_TYPE), "admin", yesterday(), true);
        Assert.assertTrue(device != null);
    }

    @Test
    public void testGetAvaliableDeviceTypes() throws DeviceManagementException {
        List<String> deviceTypes = deviceMgtService.getAvailableDeviceTypes();
        Assert.assertTrue(!deviceTypes.isEmpty());
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevices() throws DeviceManagementException {
        List<Device> devices = deviceMgtService.getAllDevices();
        Assert.assertTrue(!devices.isEmpty());
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevicesPaginated() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        PaginationResult result = deviceMgtService.getAllDevices(request);
        Assert.assertTrue(result.getRecordsTotal() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevicesByName() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        request.setDeviceName(DEVICE_TYPE + "-" + DEVICE_ID);
        PaginationResult result = deviceMgtService.getDevicesByName(request);
        Assert.assertTrue(result.getRecordsTotal() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevicesByNameAndType() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        request.setDeviceName(DEVICE_TYPE + "-" + DEVICE_ID);
        request.setDeviceType(DEVICE_TYPE);
        List<Device> devices = deviceMgtService.getDevicesByNameAndType(request, true);
        Assert.assertTrue(!devices.isEmpty());
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevicesByStatus() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        request.setStatus(EnrolmentInfo.Status.ACTIVE.toString());
        PaginationResult result = deviceMgtService.getDevicesByStatus(request, true);
        Assert.assertTrue(result.getRecordsTotal() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDevicesOfTypePaginated() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        request.setDeviceType(DEVICE_TYPE);
        PaginationResult result = deviceMgtService.getDevicesByType(request);
        Assert.assertTrue(result.getRecordsTotal() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevicesWithInfo() throws DeviceManagementException {
        List<Device> devices = deviceMgtService.getAllDevices(true);
        Assert.assertTrue(!devices.isEmpty());
        Assert.assertTrue(devices.get(0).getDeviceInfo() != null);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetAllDevicesWithInfoPaginated() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        PaginationResult result = deviceMgtService.getAllDevices(request, true);
        Assert.assertTrue(result.getRecordsTotal() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetTenantedDevice() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        HashMap<Integer, Device> deviceMap = deviceMgtService.getTenantedDevice(new
                DeviceIdentifier
                (DEVICE_ID, DEVICE_TYPE));
        Assert.assertTrue(!deviceMap.isEmpty());
    }

    @Test
    public void testGetLicense() throws DeviceManagementException {
        License license = deviceMgtService.getLicense(DEVICE_TYPE, "ENG");
        Assert.assertTrue(license.getLanguage().equalsIgnoreCase("ENG"));
    }

    @Test(expectedExceptions = DeviceManagementException.class)
    public void testSendRegistrationEmailNoMetaInfo() throws ConfigurationManagementException, DeviceManagementException {
        deviceMgtService.sendRegistrationEmail(null);
        Assert.assertTrue(false);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviesOfUser() throws DeviceManagementException {
        List<Device> devices = deviceMgtService.getDevicesOfUser("admin");
        Assert.assertTrue(!devices.isEmpty());
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDevieByStatus() throws DeviceManagementException {
        Device device = deviceMgtService.getDevice(new DeviceIdentifier(DEVICE_ID,
                DEVICE_TYPE), EnrolmentInfo.Status.ACTIVE);
        Assert.assertTrue(device != null);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDevieByDate() throws DeviceManagementException {
        List<Device> devices = deviceMgtService.getDevices(yesterday());
        Assert.assertTrue(!devices.isEmpty());
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviesOfUserPaginated() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        request.setOwner("admin");
        PaginationResult result = deviceMgtService.getDevicesOfUser(request, true);
        Assert.assertTrue(result.getRecordsTotal() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviesByOwnership() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        request.setOwnership(EnrolmentInfo.OwnerShip.BYOD.toString());
        PaginationResult result = deviceMgtService.getDevicesByOwnership(request);
        Assert.assertTrue(result.getRecordsTotal() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviesByStatus() throws DeviceManagementException {
        PaginationRequest request = new PaginationRequest(0, 100);
        request.setStatus("ACTIVE");
        PaginationResult result = deviceMgtService.getDevicesByStatus(request);
        Assert.assertTrue(result.getRecordsTotal() > 0);
    }

    @Test(dependsOnMethods = {"testSuccessfulDeviceEnrollment"})
    public void testGetDeviesOfUserAndDeviceType() throws DeviceManagementException {
        List<Device> devices = deviceMgtService.getDevicesOfUser("admin", DEVICE_TYPE, true);
        Assert.assertTrue(!devices.isEmpty() && devices.get(0).getType().equalsIgnoreCase
                (DEVICE_TYPE) && devices.get(0).getDeviceInfo() != null);
    }

    @Test
    public void testSendRegistrationEmailSuccessFlow() throws ConfigurationManagementException, DeviceManagementException {
        String recipient = "test-user@wso2.com";
        Properties props = new Properties();
        props.setProperty("first-name", "Test");
        props.setProperty("username", "User");
        props.setProperty("password", "!@#$$$%");

        EmailMetaInfo metaInfo = new EmailMetaInfo(recipient, props);

        deviceMgtService.sendRegistrationEmail(metaInfo);
        Assert.assertTrue(true);
    }

    @Test
    public void testSendEnrollmentInvitation() throws ConfigurationManagementException,
            DeviceManagementException {
        String recipient = "test-user@wso2.com";
        Properties props = new Properties();
        props.setProperty("first-name", "Test");
        props.setProperty("username", "User");
        props.setProperty("password", "!@#$$$%");

        EmailMetaInfo metaInfo = new EmailMetaInfo(recipient, props);

        deviceMgtService.sendEnrolmentInvitation("template-name", metaInfo);
        Assert.assertTrue(true);
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
}