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
import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.core.common.BaseGroupManagementTest;
import org.wso2.carbon.device.mgt.group.core.common.TestDataHolder;

import java.util.Date;
import java.util.List;

public class GroupPersistTests extends BaseGroupManagementTest {

    GroupDAO groupDAO = GroupManagementDAOFactory.getGroupDAO();

    private static final Log log = LogFactory.getLog(GroupPersistTests.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDatSource();
    }

    @Test
    public void testAddGroupTest() {
        DeviceGroup deviceGroup = TestDataHolder.generateDummyGroupData();
        try {
            groupDAO.addGroup(deviceGroup);
            log.debug("Group added to database");
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while adding device type '" + deviceGroup.getName() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

        DeviceGroup group;
        try {
            group = getLastStoredGroup();
            Assert.assertNotNull(group, "Group is null");
            log.debug("Group name: " + group.getName());
         } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving target device type id";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
    }

    private DeviceGroup getLastStoredGroup() throws GroupManagementDAOException {
        List<DeviceGroup> groups = groupDAO.getAllGroups();
        if (groups.size() > 0) {
            return groups.get(groups.size() - 1);
        } else {
            return null;
        }
    }

    public DeviceGroup getGroupById(int groupId){
        try {
            return groupDAO.getGroupById(groupId);
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving group details";
            log.error(msg, e);
            Assert.fail(msg, e);
            return null;
        }
    }

    @Test(dependsOnMethods = {"testAddGroupTest"})
    public void updateGroupTest() {
        long time = new Date().getTime();
        String name = "Test Updated";
        String desc = "Desc updated";
        try {
            DeviceGroup group = getLastStoredGroup();
            Assert.assertNotNull(group, "Group is null");
            group.setDateOfLastUpdate(time);
            group.setName(name);
            group.setDescription(desc);
            groupDAO.updateGroup(group);
            log.debug("Group updated");
            group = getGroupById(group.getId());
            Assert.assertNotNull(group, "Group is null");
            Assert.assertEquals(group.getName(), name, "Group name is null");
            Assert.assertEquals(group.getDescription(), desc, "Group description is null");
            Assert.assertEquals((long) group.getDateOfLastUpdate(), time, "Update time is not set");
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while updating group details";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"updateGroupTest"})
    public void deleteGroupTest(){
        try {
            DeviceGroup group = getLastStoredGroup();
            Assert.assertNotNull(group, "Group is null");
            int groupId = group.getId();
            groupDAO.deleteGroup(groupId);
            log.debug("Group deleted");
            group = getGroupById(groupId);
            Assert.assertNull(group, "Group not deleted");
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while updating group details";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
    }
}
