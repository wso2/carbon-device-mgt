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

package org.wso2.carbon.device.mgt.core.service;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.RoleDoesNotExistException;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * This test class is used for for testing negative scenarios of {@link GroupManagementProviderService}
 */
public class GroupManagementProviderServiceNegativeTest extends BaseDeviceManagementTest {
    private GroupManagementProviderService groupManagementProviderService;

    @BeforeClass
    @Override
    public void init() throws Exception {
        DataSource datasource = this.getDataSource(this.
                readDataSourceConfig("src/test/resources/config/datasource/no-table-data-source-config.xml"));
        GroupManagementDAOFactory.init(datasource);
        groupManagementProviderService = new GroupManagementProviderServiceImpl();
    }

    @Test(description = "This method tests the addDevices method under negative scenarios",
            expectedExceptions = {GroupManagementException.class},
            expectedExceptionsMessageRegExp = "Error occurred while adding device to group.*")
    public void testAddDevicesScenario1() throws GroupManagementException, DeviceNotFoundException {
        List<DeviceIdentifier> list = TestUtils.getDeviceIdentifiersList();
        groupManagementProviderService.addDevices(1, list);
    }

    @Test(description = "This method tests the addDevices method under negative circumstances", expectedExceptions =
            {GroupManagementException.class}, expectedExceptionsMessageRegExp = "Error occurred in addDevices for.*")
    public void testAddDevicesScenario2() throws GroupManagementException, DeviceNotFoundException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier("test", "test");
        List<DeviceIdentifier> list = new ArrayList<>();
        list.add(deviceIdentifier);
        groupManagementProviderService.addDevices(1, list);
    }

    @Test(description = "This method tests the getGroup method of the GroupManagementProviderService under "
            + "negative conditions", expectedExceptions = {GroupManagementException.class},
            expectedExceptionsMessageRegExp = "Error occurred while obtaining group.*")
    public void testGetGroup() throws GroupManagementException {
        groupManagementProviderService.getGroup(1);
    }

    @Test(description = "This method tests the getGroup method of the GroupManagementProviderService under "
            + "negative conditions", expectedExceptions = {GroupManagementException.class},
            expectedExceptionsMessageRegExp = "Error occurred while obtaining group with name.*")
    public void testGetGroupWithName() throws GroupManagementException {
        groupManagementProviderService.getGroup("1");
    }

    @Test(description = "This method tests the getGroups method of the GroupManagementProviderService under negative "
            + "conditions", expectedExceptions = {GroupManagementException.class}, expectedExceptionsMessageRegExp =
            "Error occurred while retrieving all groups in tenant.*")
    public void testGetGroups() throws GroupManagementException {
        groupManagementProviderService.getGroups();
    }

    @Test(description = "This method tests the getGroups method of the GroupManagementProviderService under negative "
            + "conditions", expectedExceptions = {GroupManagementException.class}, expectedExceptionsMessageRegExp =
            "Error occurred while retrieving all groups accessible to user.*")
    public void testGetGroupsWithUserName() throws GroupManagementException {
        groupManagementProviderService.getGroups("test");
    }

    @Test(description = "This method tests the getGroupCount method under negative circumstances", expectedExceptions
            = {GroupManagementException.class}, expectedExceptionsMessageRegExp = "Error occurred while retrieving all "
            + "groups in tenant")
    public void testGetGroupCount() throws GroupManagementException {
        groupManagementProviderService.getGroupCount();
    }

    @Test(description = "This method tests the getGroupCount method with username under negative circumstances",
            expectedExceptions = {GroupManagementException.class}, expectedExceptionsMessageRegExp = "Error occurred "
            + "while retrieving group count of user.*")
    public void testGetGroupCountWithUserName() throws GroupManagementException {
        groupManagementProviderService.getGroupCount("test");

    }

    @Test(description = "This method tests the getGroups method with pagination request under negative "
            + "circumstances", expectedExceptions = {GroupManagementException.class},
            expectedExceptionsMessageRegExp = "Error occurred while retrieving all groups in tenant")
    public void testGetGroupsWithPaginationRequest() throws GroupManagementException {
        groupManagementProviderService.getGroups(TestUtils.createPaginationRequest());
    }

    @Test(description = "This method tests the getGroups method with pagination request and username under negative "
            + "circumstances", expectedExceptions = {GroupManagementException.class},
            expectedExceptionsMessageRegExp = "Error occurred while retrieving all groups accessible to user.")
    public void testGetGroupsWithPaginationRequestAndUserName() throws GroupManagementException {
        groupManagementProviderService.getGroups("test", TestUtils.createPaginationRequest());
    }

    @Test(description = "This method tests the get roles method under negative circumstances",
            expectedExceptions = {GroupManagementException.class}, expectedExceptionsMessageRegExp = "Error occurred "
            + "while retrieving all groups in tenant.*")
    public void testManageGroupSharing() throws GroupManagementException, RoleDoesNotExistException {
        groupManagementProviderService.getRoles(1);
    }

    @Test(description = "This method tests the getDeviceCount under negative circumstances.", expectedExceptions =
            {GroupManagementException.class}, expectedExceptionsMessageRegExp = "Error occurred while retrieving all "
            + "groups in tenant.*")
    public void testGetDeviceCount() throws GroupManagementException {
        groupManagementProviderService.getDeviceCount(1);
    }

    @Test(description = "This method tests the getDevices method under negative circumstances", expectedExceptions =
            {GroupManagementException.class})
    public void testGetDevicesWithPagination() throws GroupManagementException {
        groupManagementProviderService.getDevices(1, 0, 10);
    }

    @Test(description = "This method tests the getGroupCount with username when the user name is given as null",
            expectedExceptions = {GroupManagementException.class}, expectedExceptionsMessageRegExp = "Received empty "
            + "user name for getGroupCount.*")
    public void testGetGroupCountWithUserName2() throws GroupManagementException {
        groupManagementProviderService.getGroupCount(null);
    }

    @Test(description = "This method tests getGroups method under negative circumstances",
            expectedExceptionsMessageRegExp = "Received empty device identifier for getGroups",
            expectedExceptions = {GroupManagementException.class})
    public void testGetGroupsWithDeviceIdentifier() throws GroupManagementException {
        groupManagementProviderService.getGroups((DeviceIdentifier) null);
    }
}
