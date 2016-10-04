/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupList;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupShare;
import org.wso2.carbon.device.mgt.jaxrs.service.api.GroupManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

public class GroupManagementServiceImpl implements GroupManagementService {

    private static final Log log = LogFactory.getLog(GroupManagementServiceImpl.class);

    private static final String DEFAULT_ADMIN_ROLE = "admin";
    private static final String[] DEFAULT_ADMIN_PERMISSIONS = {"/permission/device-mgt/admin/groups",
                                                               "/permission/device-mgt/user/groups"};

    @Override
    public Response getGroups(int offset, int limit) {
        try {
            GroupManagementProviderService service = DeviceMgtAPIUtils.getGroupManagementProviderService();
            String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            List<DeviceGroup> deviceGroups = service.getGroups(currentUser, offset, limit);
            DeviceGroupList deviceGroupList = new DeviceGroupList();
            deviceGroupList.setList(deviceGroups);
            deviceGroupList.setCount(service.getGroupCount(currentUser));
            return Response.status(Response.Status.OK).entity(deviceGroupList).build();
        } catch (GroupManagementException e) {
            String error = "Error occurred while getting the groups related to users for policy.";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getGroupCount() {
        try {
            String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            int count = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroupCount(currentUser);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while retrieving group count.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response createGroup(DeviceGroup group) {
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        group.setOwner(owner);
        group.setDateOfCreation(new Date().getTime());
        group.setDateOfLastUpdate(new Date().getTime());
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().createGroup(group, DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
            return Response.status(Response.Status.OK).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while adding new group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (GroupAlreadyExistException e) {
            String msg = "Group already exists with name '" + group.getName() + "'.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getGroup(String groupName) {
        return null;
    }

    @Override
    public Response updateGroup(String groupName, DeviceGroup deviceGroup) {
        return null;
    }

    @Override
    public Response deleteGroup(String groupName) {
        return null;
    }

    @Override
    public Response manageGroupSharing(String groupName, DeviceGroupShare deviceGroupShare) {
        return null;
    }

    @Override
    public Response getUsersOfGroup(String groupName) {
        return null;
    }

    @Override
    public Response getDevicesOfGroup(String groupName, int offset, int limit) {
        return null;
    }

    @Override
    public Response getDeviceCountOfGroup(String groupName) {
        return null;
    }

    @Override
    public Response addDevicesToGroup(String groupName, List<DeviceIdentifier> deviceIdentifiers) {
        return null;
    }

    @Override
    public Response removeDevicesFromGroup(String groupName, List<DeviceIdentifier> deviceIdentifiers) {
        return null;
    }

}
