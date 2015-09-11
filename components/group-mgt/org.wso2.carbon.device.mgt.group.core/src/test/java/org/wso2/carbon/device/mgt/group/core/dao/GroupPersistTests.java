/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.group.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.common.GroupManagementException;
import org.wso2.carbon.device.mgt.group.core.common.BaseGroupManagementTest;
import org.wso2.carbon.device.mgt.group.core.common.TestDataHolder;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class GroupPersistTests extends BaseGroupManagementTest {

    private static final Log log = LogFactory.getLog(GroupPersistTests.class);
    GroupDAO groupDAO = GroupManagementDAOFactory.getGroupDAO();

    @BeforeClass @Override public void init() {
        initDataSource();
    }

    @Test public void testAddGroupTest() {
        DeviceGroup deviceGroup = TestDataHolder.generateDummyGroupData();
        try {
            GroupManagementDAOFactory.beginTransaction();
            groupDAO.addGroup(deviceGroup);
            GroupManagementDAOFactory.commitTransaction();
            log.debug("Group added to database");
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device type '" + deviceGroup.getName() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }

        DeviceGroup group = getLastStoredGroup();
            Assert.assertNotNull(group, "Group is null");
            log.debug("Group name: " + group.getName());
    }

    private DeviceGroup getLastStoredGroup() {
        try {
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getLastCreatedGroup(TestDataHolder.OWNER, TestDataHolder.SUPER_TENANT_ID);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving last saved group";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return null;
    }

    public DeviceGroup getGroupById(int groupId) {
        try {
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroup(groupId);
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving group details";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return null;
    }

    @Test(dependsOnMethods = { "testAddGroupTest" }) public void updateGroupTest() {
        long time = new Date().getTime();
        String name = "Test Updated";
        String desc = "Desc updated";
        DeviceGroup group = getLastStoredGroup();
        Assert.assertNotNull(group, "Group is null");
        group.setDateOfLastUpdate(time);
        group.setName(name);
        group.setDescription(desc);
        try {
            GroupManagementDAOFactory.beginTransaction();
            groupDAO.updateGroup(group);
            GroupManagementDAOFactory.commitTransaction();
            log.debug("Group updated");
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating group details";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }

        group = getGroupById(group.getId());
        Assert.assertNotNull(group, "Group is null");
        Assert.assertEquals(group.getName(), name, "Group name is null");
        Assert.assertEquals(group.getDescription(), desc, "Group description is null");
        Assert.assertEquals((long) group.getDateOfLastUpdate(), time, "Update time is not set");
    }

    @Test(dependsOnMethods = { "testAddGroupTest" }) public void findGroupTest() {
        try {
            GroupManagementDAOFactory.openConnection();
            List<DeviceGroup> groups = groupDAO.getGroups("Test", TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotEquals(groups.size(), 0, "No groups found");
            Assert.assertNotNull(groups.get(0), "Group is null");
            log.debug("Group found: " + groups.get(0).getName());
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while find group by name";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = { "testAddGroupTest" }) public void getGroupTest() {
        try {
            GroupManagementDAOFactory.openConnection();
            List<DeviceGroup> groups = groupDAO.getGroups(TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotEquals(groups.size(), 0, "No groups found");
            Assert.assertNotNull(groups.get(0), "Group is null");
            log.debug("No of Groups found: " + groups.size());
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while find group by name";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = { "updateGroupTest" }) public void deleteGroupTest() {
        DeviceGroup group = getLastStoredGroup();
        int groupId = 0;
        try {
            Assert.assertNotNull(group, "Group is null");
            groupId = group.getId();
            GroupManagementDAOFactory.beginTransaction();
            groupDAO.deleteGroup(groupId);
            GroupManagementDAOFactory.commitTransaction();
            log.debug("Group deleted");
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating group details";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        group = getGroupById(groupId);
        Assert.assertNull(group, "Group not deleted");
    }
}
