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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAuthorizationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.RoleDoesNotExistException;
import org.wso2.carbon.device.mgt.common.permission.mgt.*;
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
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.JDBCTenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.util.*;


public class DeviceAccessAuthorizationServiceTest extends BaseDeviceManagementTest {
    private static final Log log = LogFactory.getLog(DeviceAccessAuthorizationServiceTest.class);
    private DeviceAccessAuthorizationServiceImpl deviceAccessAuthorizationService;
    private static final String DEVICE_TYPE = "AUTH_SERVICE_TEST_TYPE";
    private static final int NO_OF_DEVICES = 5;
    private static final String ADMIN_USER = "admin";
    private static final String NON_ADMIN_ALLOWED_USER = "nonAdmin";
    private static final String NORMAL_USER = "normal";
    private static final String ADMIN_ROLE = "adminRole";
    private static final String NON_ADMIN_ROLE = "nonAdminRole";
    private static final String DEFAULT_GROUP = "defaultGroup";
    private static final String DEVICE_ID_PREFIX = "AUTH-SERVICE-TEST-DEVICE-ID-";
    public static final String USER_CLAIM_EMAIL_ADDRESS = "http://wso2.org/claims/emailaddress";
    public static final String USER_CLAIM_FIRST_NAME = "http://wso2.org/claims/givenname";
    public static final String USER_CLAIM_LAST_NAME = "http://wso2.org/claims/lastname";
    public static final String ADMIN_PERMISSION = "/permission/admin";
    public static final String NON_ADMIN_PERMISSION = "/permission/admin/manage/device-mgt/devices/owning-device/view";


    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private List<DeviceIdentifier> groupDeviceIds = new ArrayList<>();
    private List<DeviceIdentifier> nonGroupDeviceIds = new ArrayList<>();

    Map<String, String> defaultUserClaims;

    @BeforeClass
    public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing");
        for (int i = 0; i < NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(new GroupManagementProviderServiceImpl());
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
        deviceAccessAuthorizationService = Mockito.mock(DeviceAccessAuthorizationServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        defaultUserClaims = buildDefaultUserClaims("firstname", "lastname", "email");
        initializeTestEnvironment();
    }

    private RegistryService getRegistryService() throws RegistryException, UserStoreException {
        RealmService realmService = new InMemoryRealmService();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ADMIN_USER);
        BasicDataSource dataSource = new BasicDataSource();
        String connectionUrl = "jdbc:h2:./target/databasetest/CARBON_TEST";
        dataSource.setUrl(connectionUrl);
        dataSource.setDriverClassName("org.h2.Driver");
        JDBCTenantManager jdbcTenantManager = new JDBCTenantManager(dataSource, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        realmService.setTenantManager(jdbcTenantManager);
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    @Test
    public void isUserAuthenticated() throws Exception {
        for (DeviceIdentifier deviceId : deviceIds) {
            Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceId, ADMIN_USER));
        }
    }

    @Test
    public void isUserAuthenticatedList() throws Exception {
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds, ADMIN_USER);
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(), 5);
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(), 0);
    }

    @Test
    public void isUserAuthenticatedListOnlyDevId() throws Exception {
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.isUserAuthorized(deviceIds);
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(), 5);
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(), 0);
    }

    @Test
    public void isUserAuthenticatedOnlyDevId() throws Exception {
        for (DeviceIdentifier deviceId : deviceIds) {
            Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceId));
        }
    }

    @Test
    public void isDeviceAdminUser() throws DeviceAccessAuthorizationException, UserStoreException, PermissionManagementException {
        Assert.assertTrue(deviceAccessAuthorizationService.isDeviceAdminUser());

    }

    @Test
    public void isUserAuthorizedAllowedDevice() throws DeviceAccessAuthorizationException, UserStoreException, PermissionManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceIds.get(0), new String[]{NON_ADMIN_PERMISSION}));
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void isUserAuthorizedNotAllowedDevice() throws DeviceAccessAuthorizationException, UserStoreException, PermissionManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        Assert.assertFalse(deviceAccessAuthorizationService.isUserAuthorized(deviceIds.get(3), new String[]{NON_ADMIN_PERMISSION}));
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void nonAdminUserTryIsAdmin() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NORMAL_USER);
        Assert.assertFalse(deviceAccessAuthorizationService.isDeviceAdminUser());
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void isUserAuthorizedAllowedDeviceAllDetails() throws DeviceAccessAuthorizationException, UserStoreException, PermissionManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        Assert.assertTrue(deviceAccessAuthorizationService.isUserAuthorized(deviceIds.get(0),NON_ADMIN_ALLOWED_USER,new String[]{NON_ADMIN_PERMISSION}));
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void isUserAuthorizedAllowedDeviceAllDetailsWrongDevice() throws DeviceAccessAuthorizationException, UserStoreException, PermissionManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        Assert.assertFalse(deviceAccessAuthorizationService.isUserAuthorized(deviceIds.get(3),NON_ADMIN_ALLOWED_USER,new String[]{NON_ADMIN_PERMISSION}));
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void deviceIdAndPermission() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds,new String[]{NON_ADMIN_PERMISSION});
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(),2);
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(),3);
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void deviceIdUsernameAndPermission() throws DeviceAccessAuthorizationException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(NON_ADMIN_ALLOWED_USER);
        DeviceAuthorizationResult deviceAuthorizationResult = deviceAccessAuthorizationService.
                isUserAuthorized(deviceIds,NON_ADMIN_ALLOWED_USER,new String[]{NON_ADMIN_PERMISSION});
        Assert.assertEquals(deviceAuthorizationResult.getAuthorizedDevices().size(),2);
        Assert.assertEquals(deviceAuthorizationResult.getUnauthorizedDevices().size(),3);
        PrivilegedCarbonContext.endTenantFlow();
    }

    public void initializeTestEnvironment() throws UserStoreException, GroupManagementException, RoleDoesNotExistException,
            DeviceNotFoundException {
        //creating UI permission
        Permission adminPermission = new Permission(ADMIN_PERMISSION, CarbonConstants.UI_PERMISSION_ACTION);
        Permission deviceViewPermission = new Permission(NON_ADMIN_PERMISSION, CarbonConstants.UI_PERMISSION_ACTION);
        UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getUserStoreManager();
        //Adding a non Admin User
        userStoreManager.addUser(NON_ADMIN_ALLOWED_USER, "password", null, defaultUserClaims, null);
        //Adding a normal user
        userStoreManager.addUser(NORMAL_USER, "password", null, defaultUserClaims, null);
        //Adding role with permission to Admin user
        userStoreManager.addRole(ADMIN_ROLE, new String[]{ADMIN_USER}, new Permission[]{adminPermission});
        //Adding role with permission to non Admin user
        userStoreManager.addRole(NON_ADMIN_ROLE, new String[]{NON_ADMIN_ALLOWED_USER}, new Permission[]{deviceViewPermission});
        //Creating default group
        GroupManagementProviderService groupManagementProviderService = DeviceManagementDataHolder.getInstance()
                .getGroupManagementProviderService();
        groupManagementProviderService.createDefaultGroup(DEFAULT_GROUP);
        int groupId = groupManagementProviderService.getGroup(DEFAULT_GROUP).getGroupId();
        //Sharing group with admin and non admin roles
        groupManagementProviderService.manageGroupSharing(groupId, new ArrayList<>(Arrays.asList(ADMIN_ROLE, NON_ADMIN_ROLE)));
        //Adding first 2 devices to the group
        groupDeviceIds.add(deviceIds.get(0));
        groupDeviceIds.add(deviceIds.get(1));
        groupManagementProviderService.addDevices(groupId, groupDeviceIds);
        //Rest of the devices
        nonGroupDeviceIds.add(deviceIds.get(2));
        nonGroupDeviceIds.add(deviceIds.get(3));
        nonGroupDeviceIds.add(deviceIds.get(4));
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

}
