/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.authorization;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAuthorizationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.RoleDoesNotExistException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.JDBCTenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;

public class DeviceAccessAuthorizationServiceTest extends BaseDeviceManagementTest {
    private static final Log log = LogFactory.getLog(DeviceAccessAuthorizationServiceTest.class);
    private static final String DEVICE_TYPE = "AUTH_SERVICE_TEST_TYPE";
    private static final int NO_OF_DEVICES = 5;
    private static final String ADMIN_USER = "admin";
    private static final String NON_ADMIN_ALLOWED_USER = "nonAdmin";
    private static final String NORMAL_USER = "normal";
    private static final String ADMIN_ROLE = "adminRole";
    private static final String NON_ADMIN_ROLE = "nonAdminRole";
    private static final String DEFAULT_GROUP = "defaultGroup";
    private static final String DEVICE_ID_PREFIX = "AUTH-SERVICE-TEST-DEVICE-ID-";
    private static final String USER_CLAIM_EMAIL_ADDRESS = "http://wso2.org/claims/emailaddress";
    private static final String USER_CLAIM_FIRST_NAME = "http://wso2.org/claims/givenname";
    private static final String USER_CLAIM_LAST_NAME = "http://wso2.org/claims/lastname";
    private static final String ADMIN_PERMISSION = "/permission/admin";
    private static final String NON_ADMIN_PERMISSION = "/permission/admin/manage/device-mgt/devices/owning-device/view";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private DeviceAccessAuthorizationServiceImpl deviceAccessAuthorizationService;
    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private List<DeviceIdentifier> groupDeviceIds = new ArrayList<>();
    private Map<String, String> defaultUserClaims;

    @BeforeClass
    public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing test environment to test DeviceAccessAuthorization Class");
        for (int i = 0; i < NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(new
                GroupManagementProviderServiceImpl());
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        for (Device device : devices) {
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
        deviceAccessAuthorizationService = Mockito.mock(DeviceAccessAuthorizationServiceImpl.class,
                Mockito.CALLS_REAL_METHODS);
        defaultUserClaims = buildDefaultUserClaims(FIRST_NAME, LAST_NAME, EMAIL);
        initializeTestEnvironment();
        //Starting tenant flow
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
    }

    private RegistryService getRegistryService() throws RegistryException, UserStoreException {
        RealmService realmService = new InMemoryRealmService();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        BasicDataSource dataSource = new BasicDataSource();
        String connectionUrl = "jdbc:h2:./target/databasetest/CARBON_TEST";
        dataSource.setUrl(connectionUrl);
        dataSource.setDriverClassName("org.h2.Driver");
        JDBCTenantManager jdbcTenantManager = new JDBCTenantManager(dataSource,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        realmService.setTenantManager(jdbcTenantManager);
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    private void initializeTestEnvironment() throws UserStoreException, GroupManagementException,
            RoleDoesNotExistException, DeviceNotFoundException {
        //creating UI permission
        Permission adminPermission = new Permission(ADMIN_PERMISSION, CarbonConstants.UI_PERMISSION_ACTION);
        Permission deviceViewPermission = new Permission(NON_ADMIN_PERMISSION, CarbonConstants.UI_PERMISSION_ACTION);
        UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getUserStoreManager();
        //Adding a non Admin User
        userStoreManager.addUser(NON_ADMIN_ALLOWED_USER, PASSWORD, null, defaultUserClaims, null);
        //Adding a normal user
        userStoreManager.addUser(NORMAL_USER, PASSWORD, null, defaultUserClaims, null);
        //Adding role with permission to Admin user
        userStoreManager.addRole(ADMIN_ROLE, new String[]{ADMIN_USER}, new Permission[]{adminPermission});
        //Adding role with permission to non Admin user
        userStoreManager.addRole(NON_ADMIN_ROLE, new String[]{NON_ADMIN_ALLOWED_USER},
                new Permission[]{deviceViewPermission});
        //Creating default group
        GroupManagementProviderService groupManagementProviderService = DeviceManagementDataHolder.getInstance()
                .getGroupManagementProviderService();
        groupManagementProviderService.createDefaultGroup(DEFAULT_GROUP);
        int groupId = groupManagementProviderService.getGroup(DEFAULT_GROUP).getGroupId();
        //Sharing group with admin and non admin roles
        groupManagementProviderService.manageGroupSharing(groupId, new ArrayList<>(Arrays.asList(ADMIN_ROLE,
                NON_ADMIN_ROLE)));
        //Adding first 2 devices to the group
        groupDeviceIds.add(deviceIds.get(0));
        groupDeviceIds.add(deviceIds.get(1));
        groupManagementProviderService.addDevices(groupId, groupDeviceIds);
    }

    private Map<String, String> buildDefaultUserClaims(String firstName, String lastName, String emailAddress) {
        Map<String, String> defaultUserClaims = new HashMap<>();
        defaultUserClaims.put(USER_CLAIM_FIRST_NAME, firstName);
        defaultUserClaims.put(USER_CLAIM_LAST_NAME, lastName);
        defaultUserClaims.put(USER_CLAIM_EMAIL_ADDRESS, emailAddress);
        if (log.isDebugEnabled()) {
            log.debug("Default claim map is created for new user: " + defaultUserClaims.toString());
        }
        return defaultUserClaims;
    }

    //Admin User test cases
    @Test(description = "Check authorization giving a device identifier and username")
    public void userAuthDevIdUserName() throws Exception {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        for (DeviceIdentifier deviceId : deviceIds) {
            Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceId, ADMIN_USER),
                    "Device access authorization for admin user failed");
        }
    }

    @Test(description = "Authorization for multiple device identifiers and username")
    public void userAuthDevIdUserNameResult() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds, ADMIN_USER);
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(), 5,
                "Expected 5 authorized devices for admin user");
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(), 0,
                "Expected 0 un-authorized devices for admin user");
    }

    @Test(description = "Authorization by device identifier")
    public void userAuthDevId() throws Exception {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        for (DeviceIdentifier deviceId : deviceIds) {
            Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceId),
                    "Authorize user from device identifier failed");
        }
    }

    @Test(description = "Authorization by multiple device identifiers")
    public void userAuthDevIdResult() throws Exception {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds);
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(), 5,
                "Expected 5 authorized devices for admin user");
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(), 0,
                "Expected 0 un-authorized devices for admin user");
    }

    @Test(description = "Check current user is a device administrator")
    public void isDevAdminAdminUser() throws DeviceAccessAuthorizationException, UserStoreException,
            PermissionManagementException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        Assert.assertTrue(deviceAccessAuthorizationService.isDeviceAdminUser(),
                "Admin user failed to authorize as admin");
    }

    //Non admin user tests
    @Test(description = "Check authorization by device identifier and permission Allowed test case")
    public void userAuthDevIdPermission() throws DeviceAccessAuthorizationException, UserStoreException,
            PermissionManagementException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceIds.get(0),
                new String[]{NON_ADMIN_PERMISSION}), "Non admin user with permissions attempt to access failed");
    }

    @Test(description = "Check authorization by device identifier and permission Not-allowed test case")
    public void userAuthFalseDevIdPermission() throws DeviceAccessAuthorizationException, UserStoreException,
            PermissionManagementException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        Assert.assertFalse(deviceAccessAuthorizationService.isUserAuthorized(deviceIds.get(3),
                new String[]{NON_ADMIN_PERMISSION}), "Non admin user accessing not allowed device authorized");
    }

    @Test(description = "Authorization by giving a device identifier, username and permission Allowed test case")
    public void userAuthDevIdUserNamePermission() throws DeviceAccessAuthorizationException, UserStoreException,
            PermissionManagementException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceIds.get(0), NON_ADMIN_ALLOWED_USER,
                new String[]{NON_ADMIN_PERMISSION}), "Non admin user with permissions attempt to access failed");
    }

    @Test(description = "Authorization by giving a device identifier, username and permission Not-allowed test case")
    public void userAuthFalseDevIdUserNamePermission() throws DeviceAccessAuthorizationException, UserStoreException,
            PermissionManagementException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        Assert.assertFalse(deviceAccessAuthorizationService.isUserAuthorized(deviceIds.get(3), NON_ADMIN_ALLOWED_USER,
                new String[]{NON_ADMIN_PERMISSION}), "Non admin user accessing not allowed device authorized");
    }

    @Test(description = "Authorization by giving device identifiers and permission")
    public void userAuthDevIdPermissionResult() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds, new String[]{NON_ADMIN_PERMISSION});
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(), 2,
                "Non admin user authentication to 2 devices in a shared group failed");
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(), 3,
                "Non admin user authentication to 3 devices in a non-shared group failed");
    }

    @Test(description = "Authorization by giving device identifiers, username and permission")
    public void userAuthDevIdUserNamePermissionResult() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds, NON_ADMIN_ALLOWED_USER, new String[]{NON_ADMIN_PERMISSION});
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(), 2,
                "Non admin user authentication to 2 devices in a shared group failed");
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(), 3,
                "Non admin user authentication to 3 devices in a non-shared group failed");
    }

    @Test(description = "Authorization for device admin called by normal user")
    public void isDevAdminNormalUser() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NORMAL_USER);
        Assert.assertFalse(deviceAccessAuthorizationService.isDeviceAdminUser(),"Normal user allowed as admin user");
    }

    //Check branches of isUserAuthorized
    @Test(description = "Checking branch - user is device owner")
    public void nonAdminDeviceOwner() throws DeviceAccessAuthorizationException, DeviceManagementException {

        //Creating a temporary device
        Device device = new Device();
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo(NON_ADMIN_ALLOWED_USER, EnrolmentInfo.OwnerShip.BYOD,null);
        device.setEnrolmentInfo(enrolmentInfo);
        device.setName("temp");
        device.setType(DEVICE_TYPE);
        device.setDeviceIdentifier("1234");
        DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().enrollDevice(device);

        //temporary device identifier
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE);
        deviceIdentifier.setId("1234");

        List<DeviceIdentifier> tempList = new ArrayList<>();
        tempList.add(deviceIdentifier);

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(tempList, NON_ADMIN_ALLOWED_USER, new String[]{NON_ADMIN_PERMISSION});
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(), 1,
                "Non admin device owner failed to access device");
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(), 0,
                "Non admin device owner failed to access device");
    }

    @Test(description = "Check authorization without giving permissions")
    public void userAuthWithoutPermissions() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds, NON_ADMIN_ALLOWED_USER, null);
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(), 0,
                "Non admin user try authentication without permission failed");
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(), 5,
                "Non admin user try authentication without permission failed");
    }

    //check Exception cases
    @Test(description = "check a null username in isUserAuthorized method")
    public void callUserAuthWithoutUsername() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds, "", new String[]{NON_ADMIN_PERMISSION});
        Assert.assertEquals(deviceAuthorizationResult,null,
                "Not null result for empty username in isUserAuthorized method");
    }

    @AfterClass
    public void clearAll() {
        PrivilegedCarbonContext.endTenantFlow();
    }

}
