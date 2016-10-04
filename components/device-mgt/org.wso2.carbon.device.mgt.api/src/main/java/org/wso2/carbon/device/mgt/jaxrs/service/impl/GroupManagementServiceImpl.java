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
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupShare;
import org.wso2.carbon.device.mgt.jaxrs.service.api.GroupManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.common.DeviceGroupWrapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupManagementServiceImpl implements GroupManagementService {

    private static final Log log = LogFactory.getLog(GroupManagementServiceImpl.class);

    @Override
    public Response getGroups(int offset, int limit) {
        try {
            List<DeviceGroupWrapper> groupWrappers = new ArrayList<>();
            GroupManagementProviderService service = DeviceMgtAPIUtils.getGroupManagementProviderService();
            String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            List<DeviceGroup> deviceGroups = service.getGroups(currentUser);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            for (DeviceGroup dg : deviceGroups) {
                DeviceGroupWrapper gw = new DeviceGroupWrapper();
                gw.setId(dg.getId());
                gw.setOwner(dg.getOwner());
                gw.setName(dg.getName());
                gw.setTenantId(tenantId);
                groupWrappers.add(gw);
            }
            return Response.status(Response.Status.OK).entity(groupWrappers).build();
        } catch (GroupManagementException e) {
            String error = "ErrorResponse occurred while getting the groups related to users for policy.";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response createGroup(DeviceGroup group) {
        return null;
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
    public Response addDeviceToGroup(String groupName, DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public Response removeDevicesFromGroup(String groupName, String type, String id) {
        return null;
    }
}