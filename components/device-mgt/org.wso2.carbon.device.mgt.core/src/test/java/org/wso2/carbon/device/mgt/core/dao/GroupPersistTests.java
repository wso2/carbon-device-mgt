/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class GroupPersistTests extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(GroupPersistTests.class);
    private int groupId = -1;
    private GroupDAO groupDAO;

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource();
        groupDAO = GroupManagementDAOFactory.getGroupDAO();
    }

    @Test
    public void testAddGroupTest() {
        DeviceGroup deviceGroup = TestDataHolder.generateDummyGroupData();
        try {
            GroupManagementDAOFactory.beginTransaction();
            groupId = groupDAO.addGroup(deviceGroup, TestDataHolder.SUPER_TENANT_ID);
            GroupManagementDAOFactory.commitTransaction();
            log.debug("Group added to database. ID: " + groupId);
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device type '" + deviceGroup.getName() + "'.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }

        DeviceGroup group = getGroupById(groupId);
        Assert.assertNotNull(group, "Group is null");
        log.debug("Group name: " + group.getName());
    }

    @Test(dependsOnMethods = {"testAddGroupTest"})
    public void getGroupTest() {
        try {
            GroupManagementDAOFactory.openConnection();
            GroupPaginationRequest request = new GroupPaginationRequest(0, 10);
            request.setGroupName(null);
            request.setOwner(null);
            List<DeviceGroup> groups = groupDAO.getGroups(request, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotEquals(groups.size(), 0, "No groups found");
            Assert.assertNotNull(groups.get(0), "Group is null");
            log.debug("No of Groups found: " + groups.size());
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while find group by name.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = {"testAddGroupTest"})
    public void addDeviceToGroupTest() {
        Device initialTestDevice = TestDataHolder.initialTestDevice;
        DeviceGroup deviceGroup = getGroupById(groupId);
        Assert.assertNotNull(deviceGroup, "Group is null");
        try {
            GroupManagementDAOFactory.beginTransaction();
            groupDAO.addDevice(deviceGroup.getGroupId(), initialTestDevice.getId(), TestDataHolder.SUPER_TENANT_ID);
            GroupManagementDAOFactory.commitTransaction();
            log.debug("Device added to group.");
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device '" + initialTestDevice.getName() + "'.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }

        try {
            GroupManagementDAOFactory.openConnection();
            List<Device> groupedDevices = groupDAO.getDevices(deviceGroup.getGroupId(), 0, 10, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotEquals(groupedDevices.size(), 0, "No device found");
            Assert.assertNotNull(groupedDevices.get(0), "Device is null");
            Assert.assertEquals(groupedDevices.get(0).getId(), initialTestDevice.getId(), "Device ids not matched");
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving group details.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = {"addDeviceToGroupTest"})
    public void removeDeviceFromGroupTest() {
        Device initialTestDevice = TestDataHolder.initialTestDevice;
        DeviceGroup deviceGroup = getGroupById(groupId);
        Assert.assertNotNull(deviceGroup, "Group is null");
        try {
            GroupManagementDAOFactory.beginTransaction();
            groupDAO.removeDevice(deviceGroup.getGroupId(), initialTestDevice.getId(), TestDataHolder.SUPER_TENANT_ID);
            GroupManagementDAOFactory.commitTransaction();
            log.debug("Device added to group.");
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device '" + initialTestDevice.getDeviceIdentifier() + "'.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = {"removeDeviceFromGroupTest"})
    public void updateGroupTest() {
        long time = new Date().getTime();
        String name = "Test Updated";
        String desc = "Desc updated";
        DeviceGroup group = getGroupById(groupId);
        Assert.assertNotNull(group, "Group is null");
        group.setDateOfLastUpdate(time);
        group.setName(name);
        group.setDescription(desc);
        try {
            GroupManagementDAOFactory.beginTransaction();
            groupDAO.updateGroup(group, groupId, TestDataHolder.SUPER_TENANT_ID);
            GroupManagementDAOFactory.commitTransaction();
            log.debug("Group updated");
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating group details.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }

        group = getGroupById(groupId);
        Assert.assertNotNull(group, "Group is null");
        Assert.assertEquals(group.getName(), name, "Group name");
        Assert.assertEquals(group.getDescription(), desc, "Group description");
        Assert.assertEquals((long) group.getDateOfLastUpdate(), time, "Update time");
    }

    @Test(dependsOnMethods = {"updateGroupTest"})
    public void deleteGroupTest() {
        DeviceGroup group = getGroupById(groupId);
        try {
            Assert.assertNotNull(group, "Group is null");
            GroupManagementDAOFactory.beginTransaction();
            groupDAO.deleteGroup(group.getGroupId(), TestDataHolder.SUPER_TENANT_ID);
            GroupManagementDAOFactory.commitTransaction();
            log.debug("Group deleted");
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating group details.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        group = getGroupById(groupId);
        Assert.assertNull(group, "Group is not deleted");
    }

    private DeviceGroup getGroupById(int groupId) {
        try {
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroup(groupId, TestDataHolder.SUPER_TENANT_ID);
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving group details.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return null;
    }
}
