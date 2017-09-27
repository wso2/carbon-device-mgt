/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.device.mgt.core.service;


import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.group.mgt.*;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.cache.DeviceCacheConfiguration;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

public class GroupManagementProviderServiceTest extends BaseDeviceManagementTest {

    GroupManagementProviderService groupManagementProviderService;
    private static final String DEFAULT_ADMIN_ROLE = "admin";
    private static final String[] DEFAULT_ADMIN_PERMISSIONS = {"/permission/device-mgt/admin/groups",
            "/permission/device-mgt/user/groups"};

    @BeforeClass
    @Override
    public void init() throws Exception {
        groupManagementProviderService = new GroupManagementProviderServiceImpl();
        RealmService realmService = new InMemoryRealmService();
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        realmService.getTenantManager().getSuperTenantDomain();
    }

    @Test(expectedExceptions = {GroupManagementException.class, GroupAlreadyExistException.class})
    public void createGroupNull() throws GroupManagementException, GroupAlreadyExistException {
        groupManagementProviderService.createGroup(null, null, null);
    }


    @Test(expectedExceptions = {GroupManagementException.class, GroupAlreadyExistException.class, TransactionManagementException.class})
    public void createGroupError() throws GroupManagementException, GroupAlreadyExistException, TransactionManagementException {
        GroupManagementDAOFactory.beginTransaction();
        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup4(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
    }


    @Test
    public void createGroup() throws GroupManagementException, GroupAlreadyExistException {

        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup1(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);

        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup2(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);

        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup3(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);

        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup4(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void updateGroup() throws GroupManagementException, GroupNotExistException {

        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup1().getName());
        deviceGroup.setName(deviceGroup.getName() + "_UPDATED");
        groupManagementProviderService.updateGroup(deviceGroup, deviceGroup.getGroupId());
    }

    @Test(dependsOnMethods = ("createGroup"), expectedExceptions = {GroupManagementException.class, GroupNotExistException.class})
    public void updateGroupError() throws GroupManagementException, GroupNotExistException {
        groupManagementProviderService.updateGroup(null, 1);
    }

    @Test(dependsOnMethods = ("createGroup"), expectedExceptions = {GroupManagementException.class, GroupNotExistException.class})
    public void updateGroupErrorNotExist() throws GroupManagementException, GroupNotExistException {

        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup2().getName());
        deviceGroup.setName(deviceGroup.getName() + "_UPDATED");
        groupManagementProviderService.updateGroup(deviceGroup, 6);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void deleteGroup() throws GroupManagementException {
        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup4().getName());
        Assert.assertTrue(groupManagementProviderService.deleteGroup(deviceGroup.getGroupId()));
    }


    @Test(dependsOnMethods = ("createGroup"))
    public void deleteGroupNotExists() throws GroupManagementException {
        groupManagementProviderService.deleteGroup(8);
    }


    @Test(dependsOnMethods = ("createGroup"))
    public void getGroup() throws GroupManagementException {

        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup3().getName());
        Assert.assertNotNull(groupManagementProviderService.getGroup(deviceGroup.getGroupId()));
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getGroupByName() throws GroupManagementException {
        Assert.assertNotNull(groupManagementProviderService.getGroup(TestUtils.createDeviceGroup3().getName()));
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getGroups() throws GroupManagementException {
        List<DeviceGroup> deviceGroups = groupManagementProviderService.getGroups();
        Assert.assertNotNull(deviceGroups);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getGroupsByUsername() throws GroupManagementException {
        List<DeviceGroup> deviceGroups = groupManagementProviderService.getGroups("admin");
        Assert.assertNotNull(deviceGroups);
    }

    @Test(dependsOnMethods = ("createGroup"), expectedExceptions = {GroupManagementException.class})
    public void getGroupsByUsernameError() throws GroupManagementException {
        String username = null;
        groupManagementProviderService.getGroups(username);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getGroupsByPagination() throws GroupManagementException {
        PaginationResult result = groupManagementProviderService.getGroups(TestUtils.createPaginationRequest());
        Assert.assertNotNull(result);
    }

    @Test(dependsOnMethods = ("createGroup"), expectedExceptions = {GroupManagementException.class})
    public void getGroupsByPaginationError() throws GroupManagementException {
        GroupPaginationRequest request = null;
        groupManagementProviderService.getGroups(request);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getGroupsByUsernameAndPagination(String username, GroupPaginationRequest paginationRequest)
            throws GroupManagementException {
        PaginationResult result = groupManagementProviderService.getGroups(username, paginationRequest);
        Assert.assertNotNull(result);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getGroupCount() throws GroupManagementException {
        int x = groupManagementProviderService.getGroupCount();
        Assert.assertNotNull(x);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getGroupCountByUsername(String username) throws GroupManagementException {
        int x = groupManagementProviderService.getGroupCount(username);
        Assert.assertNotNull(x);
    }

//    @Test
//    public void manageGroupSharing() throws GroupManagementException, RoleDoesNotExistException, UserStoreException {
//        groupManagementProviderService.manageGroupSharing(0, null);
//        List<String> newRoles = new ArrayList<>();
//        newRoles.add("TEST_ROLE_1");
//        newRoles.add("TEST_ROLE_2");
//        newRoles.add("TEST_ROLE_3");
//
//        UserStoreManager userStoreManager =
//                DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
//                        -1234).getUserStoreManager();
//        Permission[] permissions = new Permission[1];
//        Permission perm = new Permission("/admin/test/perm", "add");
////        perm.setAction("execute.ui");
////        perm.setResourceId("/admin/test/perm");
//        permissions[0] = perm;
//
//        userStoreManager.addRole("TEST_ROLE_1", null, permissions);
//
//        groupManagementProviderService.manageGroupSharing(1, newRoles);
//    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getRoles() throws GroupManagementException {
        List<String> roles = groupManagementProviderService.getRoles(1);
        Assert.assertNotNull(roles);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getDevices() throws GroupManagementException {
        List<Device> devices = groupManagementProviderService.getDevices(1, 1, 50);
        Assert.assertNotNull(devices);
    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getDeviceCount() throws GroupManagementException {
        int x = groupManagementProviderService.getDeviceCount(1);
        Assert.assertEquals(0, x);
    }

//    @Test(dependsOnMethods = ("createGroup"))
//    public void addDevices() throws GroupManagementException, DeviceNotFoundException {
//
//        DeviceCacheConfiguration configuration = new DeviceCacheConfiguration();
//        configuration.setEnabled(false);
//
//        DeviceConfigurationManager.getInstance().getDeviceManagementConfig().setDeviceCacheConfiguration(configuration);
//
//        List<DeviceIdentifier> list = TestUtils.getDeviceIdentifiersList();
//        groupManagementProviderService.addDevices(1, list);
//        groupManagementProviderService.addDevices(2, list);
//        groupManagementProviderService.addDevices(3, list);
//    }
//
//    @Test(dependsOnMethods = ("addDevices"))
//    public void removeDevice() throws GroupManagementException, DeviceNotFoundException {
//        List<DeviceIdentifier> list = TestUtils.getDeviceIdentifiersList();
//        groupManagementProviderService.removeDevice(2, list);
//        groupManagementProviderService.removeDevice(3, list);
//    }

    @Test(dependsOnMethods = ("createGroup"))
    public void getGroupsByUsernameAndPermissions() throws GroupManagementException {
        List<DeviceGroup> groups = groupManagementProviderService.getGroups("admin", "/permission/device-mgt/admin/groups");
        Assert.assertNotNull(groups);
    }

//    @Test(dependsOnMethods = ("addDevices"))
//    public void getGroupsByDeviceIdentifier() throws GroupManagementException {
//        DeviceIdentifier identifier = new DeviceIdentifier();
//        identifier.setId("12345");
//        identifier.setType("Test");
//        List<DeviceGroup> groups = groupManagementProviderService.getGroups(identifier);
//        Assert.assertNull(groups);
//    }

    @Test
    public void createDefaultGroup() throws GroupManagementException {
        groupManagementProviderService.createDefaultGroup("BYOD");
    }
}

