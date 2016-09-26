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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.service.api.GroupManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.common.DeviceGroupWrapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    public Response getGroups(@QueryParam("user") String user, @QueryParam("offset") int offset,
                              @QueryParam("limit") int limit) {
        try {
            List<DeviceGroupWrapper> groupWrappers = new ArrayList<>();
            GroupManagementProviderService service = DeviceMgtAPIUtils.getGroupManagementProviderService();
            List<DeviceGroup> deviceGroups = service.getGroups(user);
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
    public Response getGroup(@PathParam("groupName") String groupName) {
        return null;
    }

    @Override
    public Response updateGroup(@PathParam("groupName") String groupName, DeviceGroup deviceGroup) {
        return null;
    }

    @Override
    public Response deleteGroup(@PathParam("groupName") String groupName) {
        return null;
    }

    @Override
    public Response shareGroupWithUser(String groupName, String targetUser) {
        return null;
    }

    @Override
    public Response shareGroupWithRole(String groupName, String targetRole) {
        return null;
    }

    @Override
    public Response removeShareWithUser(@PathParam("groupName") String groupName,
                                        @QueryParam("username") String targetUser) {
        return null;
    }

    @Override
    public Response removeShareWithRole(@PathParam("groupName") String groupName,
                                        @QueryParam("roleName") String targetUser) {
        return null;
    }

    @Override
    public Response getUsersOfGroup(@PathParam("groupName") String groupName) {
        return null;
    }

    @Override
    public Response getDevicesOfGroup(@PathParam("groupName") String groupName, @QueryParam("offset") int offset,
                                      @QueryParam("limit") int limit) {
        return null;
    }

    @Override
    public Response addDeviceToGroup(@PathParam("groupName") String groupName, DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public Response removeDeviceFromGroup(@PathParam("groupName") String groupName, @QueryParam("type") String type,
                                          @QueryParam("id") String id) {
        return null;
    }
}