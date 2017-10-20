/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupNotExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.RoleDoesNotExistException;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceToGroupsAssignment;
import org.wso2.carbon.device.mgt.jaxrs.service.api.GroupManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a test case for {@link GroupManagementServiceImpl}.
 */
@PowerMockIgnore({"javax.ws.rs.*", "javax.xml.parsers"})
@SuppressStaticInitializationFor({"org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils",
        "org.wso2.carbon.context.PrivilegedCarbonContext"})
@PrepareForTest({DeviceMgtAPIUtils.class, CarbonContext.class})
public class GroupManagementServiceImplTest {
    private GroupManagementService groupManagementService;
    private GroupManagementProviderService groupManagementProviderService;
    private PrivilegedCarbonContext context;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() {
        groupManagementService = new GroupManagementServiceImpl();
        groupManagementProviderService = Mockito.mock(GroupManagementProviderService.class);
        context = Mockito.mock(PrivilegedCarbonContext.class);
        Mockito.doReturn("admin").when(context).getUsername();
    }

    @Test(description = "This method tests the behaviour of getGroups under valid conditions")
    public void testGetGroups() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(context);
        PaginationResult paginationResult = new PaginationResult();
        Mockito.doReturn(paginationResult).when(groupManagementProviderService)
                .getGroups(Mockito.anyString(), Mockito.any(GroupPaginationRequest.class));
        Response response = groupManagementService.getGroups("test", "admin", 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "GetGroups request failed with valid parameters");

        Mockito.reset(groupManagementProviderService);
        List<DeviceGroup> deviceGroupList = new ArrayList<>();
        deviceGroupList.add(new DeviceGroup("test"));
        paginationResult.setData(deviceGroupList);
        paginationResult.setRecordsTotal(1);
        Mockito.doReturn(paginationResult).when(groupManagementProviderService)
                .getGroups(Mockito.anyString(), Mockito.any(GroupPaginationRequest.class));
        response = groupManagementService.getGroups("test", "admin", 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "GetGroups request failed with valid parameters");
    }

    @Test(description = "This method tests the behaviour of getGroups method under negative circumstances",
            dependsOnMethods = {"testGetGroups"})
    public void testGetGroupUnderNegativeConditions() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(context);
        Mockito.reset(groupManagementProviderService);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService)
                .getGroups(Mockito.anyString(), Mockito.any(GroupPaginationRequest.class));
        Response response = groupManagementService.getGroups("test", "admin", 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "GetGroups request succeeded with in-valid parameters");
    }

    @Test(description = "This method tests the behaviour of getGroupCount method under valid conditions and invalid "
            + "conditions")
    public void testGetGroupCount() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(context);
        Mockito.doReturn(2).when(groupManagementProviderService).getGroupCount(Mockito.anyString());
        Response response = groupManagementService.getGroupCount();
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "GetGroupCount request failed with valid parameters");
        Mockito.reset(groupManagementProviderService);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService)
                .getGroupCount(Mockito.anyString());
        response = groupManagementService.getGroupCount();
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "GetGroupCount request succeeded with in-valid parameters");
    }

    @Test(description = "This method tests the behaviour of createGroup method under valid and invalid scenarios")
    public void testCreateGroup() throws GroupManagementException, GroupAlreadyExistException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
                .toReturn(context);
        Response response = groupManagementService.createGroup(null);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "createGroup request succeeded with the group equals to null");
        Mockito.doNothing().when(groupManagementProviderService)
                .createGroup(Mockito.any(), Mockito.any(), Mockito.any());
        response = groupManagementService.createGroup(new DeviceGroup());
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode(),
                "createGroup request failed for a request with valid parameters");
        Mockito.reset(groupManagementProviderService);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService)
                .createGroup(Mockito.any(), Mockito.any(), Mockito.any());
        response = groupManagementService.createGroup(new DeviceGroup());
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "createGroup request succeeded for a request with in-valid parameters");
        Mockito.reset(groupManagementProviderService);
        Mockito.doThrow(new GroupAlreadyExistException()).when(groupManagementProviderService)
                .createGroup(Mockito.any(), Mockito.any(), Mockito.any());
        response = groupManagementService.createGroup(new DeviceGroup());
        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "createGroup request succeeded for a request with in-valid parameters");
    }

    @Test(description = "This method tests the functionality of getGroup method under various conditions")
    public void testGetGroup() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        Mockito.doReturn(new DeviceGroup()).when(groupManagementProviderService).getGroup(1);
        Mockito.doReturn(null).when(groupManagementProviderService).getGroup(2);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService).getGroup(3);
        Response response = groupManagementService.getGroup(1);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getGroup request failed for a request with valid parameters");
        response = groupManagementService.getGroup(2);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
                "getGroup request returned a group for a non-existing group");
        response = groupManagementService.getGroup(3);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "getGroup request returned a group for a in-valid request");
    }

    @Test(description = "This method tests the functionality of updateGroup method under various conditions")
    public void testUpdateGroup() throws GroupManagementException, GroupNotExistException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.setGroupId(1);
        Mockito.doNothing().when(groupManagementProviderService).updateGroup(deviceGroup, 1);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService)
                .updateGroup(deviceGroup, 2);
        Mockito.doThrow(new GroupNotExistException()).when(groupManagementProviderService).updateGroup(deviceGroup, 3);
        Response response = groupManagementService.updateGroup(1, deviceGroup);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "update request failed for a request with valid parameters");
        response = groupManagementService.updateGroup(2, deviceGroup);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "update request succeeded for a in-valid request");
        response = groupManagementService.updateGroup(3, deviceGroup);
        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "update request succeeded for a in-valid request");
        response = groupManagementService.updateGroup(4, null);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "update request succeeded for a in-valid request");
    }

    @Test(description = "This method tests the functionality of deleteGroup method under various scenarios")
    public void testDeleteGroup() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        Mockito.doReturn(true).when(groupManagementProviderService).deleteGroup(1);
        Mockito.doReturn(false).when(groupManagementProviderService).deleteGroup(2);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService).deleteGroup(3);
        Response response = groupManagementService.deleteGroup(1);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "delete group request failed for a request with valid parameters");
        response = groupManagementService.deleteGroup(2);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
                "Non-existing group was successfully deleted");
        response = groupManagementService.deleteGroup(3);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Deletion succeeded with an erroneous condition.");
    }

    @Test(description = "This method tests the functionality of manageGroupSharing under various conditions")
    public void testManageGroupSharing() throws GroupManagementException, RoleDoesNotExistException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        Mockito.doNothing().when(groupManagementProviderService).manageGroupSharing(1, null);
        Mockito.doThrow(new GroupManagementException("test")).when(groupManagementProviderService)
                .manageGroupSharing(2, null);
        Mockito.doThrow(new RoleDoesNotExistException()).when(groupManagementProviderService)
                .manageGroupSharing(3, null);
        Response response = groupManagementService.manageGroupSharing(1, null);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "managegroupSharing request failed for a request with valid parameters");
        response = groupManagementService.manageGroupSharing(2, null);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "managegroupSharing request succeeded for a request with in-valid parameters");
        response = groupManagementService.manageGroupSharing(3, null);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "managegroupSharing request succeeded for a request with in-valid parameters");
    }

    @Test(description = "This method tests the functionality of getGroupRoles under various conditions")
    public void testGetGroupRoles() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        Mockito.doReturn(new ArrayList<String>()).when(groupManagementProviderService).getRoles(1);
        Mockito.doReturn(null).when(groupManagementProviderService).getRoles(2);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService).getRoles(3);
        Response response = groupManagementService.getRolesOfGroup(1);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getRolesOfGroup request failed for a request with valid parameters");
        response = groupManagementService.getRolesOfGroup(2);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getRolesOfGroup request failed for a request with valid parameters");
        response = groupManagementService.getRolesOfGroup(3);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "getRolesOfGroup request failed for a request with in-valid parameters");
    }

    @Test(description = "This method tests the getDevicesOfGroup under various conditions")
    public void testGetDevicesOfGroup() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        Mockito.doReturn(1).when(groupManagementProviderService).getDeviceCount(Mockito.anyInt());
        Mockito.doReturn(new ArrayList<Device>()).when(groupManagementProviderService).getDevices(1, 0, 10);
        Mockito.doReturn(null).when(groupManagementProviderService).getDevices(2, 0, 10);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService).getDevices(3, 0, 10);
        Response response = groupManagementService.getDevicesOfGroup(1, 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getDevicesOfGroup request failed for a request with valid parameters");
        response = groupManagementService.getDevicesOfGroup(2, 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getDevicesOfGroup request failed for a request with valid parameters");
        response = groupManagementService.getDevicesOfGroup(3, 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "getDevicesOfGroup request succeded for a request with in-valid parameters");
    }

    @Test(description = "This method tests the getDeviceCountOfGroup function under various different conditions.")
    public void testGetDeviceCountOfGroup() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        Mockito.doReturn(1).when(groupManagementProviderService).getDeviceCount(1);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService).getDeviceCount(2);
        Response response = groupManagementService.getDeviceCountOfGroup(1);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getDeviceCountOfGroup request failed for a request with valid parameters");
        response = groupManagementService.getDeviceCountOfGroup(2);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "getDeviceCountOfGroup request succeded for a request with in-valid parameters");
    }

    @Test(description = "This method tests the addDevicesToGroup method under various conditions.")
    public void testAddDevicesToGroup() throws GroupManagementException, DeviceNotFoundException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        Mockito.doNothing().when(groupManagementProviderService).addDevices(1, deviceIdentifiers);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService).addDevices(2,
                deviceIdentifiers);
        Mockito.doThrow(new DeviceNotFoundException()).when(groupManagementProviderService).addDevices(3,
                deviceIdentifiers);
        Response response = groupManagementService.addDevicesToGroup(1, deviceIdentifiers);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "addDevicesToGroup request failed for a request with valid parameters");
        response = groupManagementService.addDevicesToGroup(2, deviceIdentifiers);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "addDevicesToGroup request succeded for a request with in-valid parameters");
        response = groupManagementService.addDevicesToGroup(3, deviceIdentifiers);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "addDevicesToGroup request succeded for a request with in-valid parameters");
    }

    @Test(description = "This method tests the removeDevicesFromGroup method under various conditions.")
    public void testRemoveDevicesFromGroup() throws GroupManagementException, DeviceNotFoundException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        Mockito.doNothing().when(groupManagementProviderService).removeDevice(1, deviceIdentifiers);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService).removeDevice(2,
                deviceIdentifiers);
        Mockito.doThrow(new DeviceNotFoundException()).when(groupManagementProviderService).removeDevice(3,
                deviceIdentifiers);
        Response response = groupManagementService.removeDevicesFromGroup(1, deviceIdentifiers);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "removeDevicesFromGroup request failed for a request with valid parameters");
        response = groupManagementService.removeDevicesFromGroup(2, deviceIdentifiers);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "removeDevicesFromGroup request succeeded for a request with in-valid parameters");
        response = groupManagementService.removeDevicesFromGroup(3, deviceIdentifiers);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "removeDevicesFromGroup request succeeded for a request with in-valid parameters");
    }

    @Test(description = "This method tests the getGroups with device id and device type under different conditions")
    public void testGetGroupsWithDeviceId() throws GroupManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier("test", "android");
        Mockito.doReturn(new ArrayList<DeviceGroup>()).when(groupManagementProviderService).getGroups(deviceIdentifier);
        Response response = groupManagementService.getGroups("test", "android");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "getGroups request failed with valid parameters");
        Mockito.reset(groupManagementProviderService);
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService)
                .getGroups(Mockito.any(DeviceIdentifier.class));
        response = groupManagementService.getGroups("test", "android2");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "getGroups request succeeded with in-valid parameters");
    }

    @Test(description = "This method tests updateDeviceAssigningToGroups under different conditions.")
    public void testUpdateDeviceAssigningToGroups() throws GroupManagementException, DeviceNotFoundException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getGroupManagementProviderService"))
                .toReturn(groupManagementProviderService);
        Mockito.reset(groupManagementProviderService);
        DeviceToGroupsAssignment deviceToGroupsAssignment = new DeviceToGroupsAssignment();
        List<Integer> groupIds = new ArrayList<>();
        groupIds.add(1);
        groupIds.add(2);
        deviceToGroupsAssignment.setDeviceGroupIds(groupIds);
        deviceToGroupsAssignment.setDeviceIdentifier(new DeviceIdentifier("test", "android"));
        List<DeviceGroup> deviceGroups = new ArrayList<>();
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.setGroupId(1);
        deviceGroups.add(deviceGroup);
        deviceGroup = new DeviceGroup();
        deviceGroup.setGroupId(3);
        deviceGroup.setOwner(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        deviceGroups.add(deviceGroup);
        deviceGroup = new DeviceGroup();
        deviceGroup.setGroupId(4);
        deviceGroup.setOwner("test");
        deviceGroups.add(deviceGroup);
        Mockito.doReturn(deviceGroups).when(groupManagementProviderService)
                .getGroups(Mockito.any(DeviceIdentifier.class));
        Mockito.doNothing().when(groupManagementProviderService).addDevices(Mockito.anyInt(), Mockito.any());
        Mockito.doNothing().when(groupManagementProviderService).removeDevice(Mockito.anyInt(), Mockito.any());
        Response response = groupManagementService.updateDeviceAssigningToGroups(deviceToGroupsAssignment);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "updateDeviceAssigningToGroups request failed with valid parameters");
        Mockito.doThrow(new DeviceNotFoundException()).when(groupManagementProviderService)
                .removeDevice(Mockito.anyInt(), Mockito.any());
        response = groupManagementService.updateDeviceAssigningToGroups(deviceToGroupsAssignment);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "updateDeviceAssigningToGroups request succeeded with in-valid parameters");
        Mockito.doThrow(new GroupManagementException()).when(groupManagementProviderService)
                .getGroups(Mockito.any(DeviceIdentifier.class));
        response = groupManagementService.updateDeviceAssigningToGroups(deviceToGroupsAssignment);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "updateDeviceAssigningToGroups request succeeded with in-valid parameters");
    }
}
