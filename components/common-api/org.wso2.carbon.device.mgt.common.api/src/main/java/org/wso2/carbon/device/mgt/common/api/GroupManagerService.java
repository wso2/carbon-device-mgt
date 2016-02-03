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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.common.GroupManagementException;
import org.wso2.carbon.device.mgt.group.common.GroupUser;
import org.wso2.carbon.device.mgt.group.core.providers.GroupManagementServiceProvider;
import org.wso2.carbon.device.mgt.common.AbstractManagerService;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@WebService
public class GroupManagerService extends AbstractManagerService {

    private static final String DEFAULT_ADMIN_ROLE = "admin";
    private static final String DEFAULT_OPERATOR_ROLE = "invoke-device-operations";
    private static final String DEFAULT_STATS_MONITOR_ROLE = "view-statistics";
    private static final String DEFAULT_VIEW_POLICIES = "view-policies";
    private static final String DEFAULT_MANAGE_POLICIES = "mange-policies";
    private static final String DEFAULT_VIEW_EVENTS = "view-events";
    private static final String[] DEFAULT_ADMIN_PERMISSIONS = { "/permission/device-mgt/admin/groups",
            "/permission/device-mgt/user/groups" };
    private static final String[] DEFAULT_OPERATOR_PERMISSIONS = {
            "/permission/device-mgt/user/groups/device_operation" };
    private static final String[] DEFAULT_STATS_MONITOR_PERMISSIONS = {
            "/permission/device-mgt/user/groups/device_monitor" };
    private static final String[] DEFAULT_MANAGE_POLICIES_PERMISSIONS = {
            "/permission/device-mgt/user/groups/device_policies/add" };
    private static final String[] DEFAULT_VIEW_POLICIES_PERMISSIONS = {
            "/permission/device-mgt/user/groups/device_policies/view" };
    private static final String[] DEFAULT_VIEW_EVENTS_PERMISSIONS = {
            "/permission/device-mgt/user/groups/device_events" };
    private static final Log log = LogFactory.getLog(GroupManagerService.class);

    @Path("/groups")
    @POST
    @Produces("application/json")
    public Response createGroup(@FormParam("groupName") String groupName,
                                @FormParam("userName") String userName,
                                @FormParam("description") String description) {
        DeviceGroup group = new DeviceGroup();
        group.setName(groupName);
        group.setDescription(description);
        group.setOwner(userName);
        group.setDateOfCreation(new Date().getTime());
        group.setDateOfLastUpdate(new Date().getTime());
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider(
                    GroupManagementServiceProvider.class);
            int groupId = groupManagementService.createGroup(group, DEFAULT_ADMIN_ROLE,
                    DEFAULT_ADMIN_PERMISSIONS);
            boolean isAdded = (groupId > 0) && groupManagementService.addSharing(userName, groupId,
                    DEFAULT_OPERATOR_ROLE, DEFAULT_OPERATOR_PERMISSIONS);
            groupManagementService.addGroupSharingRole(userName, groupId, DEFAULT_STATS_MONITOR_ROLE,
                    DEFAULT_STATS_MONITOR_PERMISSIONS);
            groupManagementService.addGroupSharingRole(userName, groupId, DEFAULT_VIEW_POLICIES,
                    DEFAULT_VIEW_POLICIES_PERMISSIONS);
            groupManagementService.addGroupSharingRole(userName, groupId, DEFAULT_MANAGE_POLICIES,
                    DEFAULT_MANAGE_POLICIES_PERMISSIONS);
            groupManagementService.addGroupSharingRole(userName, groupId, DEFAULT_VIEW_EVENTS,
                    DEFAULT_VIEW_EVENTS_PERMISSIONS);
            if (isAdded) {
                return Response.status(Response.Status.CREATED).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}")
    @PUT
    @Produces("application/json")
    public Response updateGroup(@PathParam("groupId") int groupId, @FormParam("groupName") String groupName,
                                @FormParam("userName") String userName,
                                @FormParam("description") String description) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/modify")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider(
                    GroupManagementServiceProvider.class);
            DeviceGroup group = groupManagementService.getGroup(groupId);
            group.setName(groupName);
            group.setDescription(description);
            group.setOwner(userName);
            group.setDateOfLastUpdate(new Date().getTime());
            groupManagementService.updateGroup(group);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}")
    @DELETE
    @Produces("application/json")
    public Response deleteGroup(@PathParam("groupId") int groupId, @QueryParam("userName") String userName) {

        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/delete")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isDeleted = this.getServiceProvider(GroupManagementServiceProvider.class).deleteGroup(
                    groupId);
            if (isDeleted) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}")
    @GET
    @Produces("application/json")
    public Response getGroup(@PathParam("groupId") int groupId) {
        try {
            DeviceGroup deviceGroup = this.getServiceProvider(GroupManagementServiceProvider.class).getGroup(
                    groupId);
            if (deviceGroup != null) {
                return Response.status(Response.Status.OK).entity(deviceGroup).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/search")
    @GET
    @Produces("application/json")
    public Response findGroups(@QueryParam("groupName") String groupName,
                               @QueryParam("userName") String userName) {
        try {
            List<DeviceGroup> groups = this.getServiceProvider(GroupManagementServiceProvider.class)
                    .findGroups(groupName, userName);
            DeviceGroup[] deviceGroups = new DeviceGroup[groups.size()];
            groups.toArray(deviceGroups);
            return Response.status(Response.Status.OK).entity(deviceGroups).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups")
    @GET
    @Produces("application/json")
    public Response getGroups(@QueryParam("userName") String userName,
                              @QueryParam("permission") String permission) {
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider(
                    GroupManagementServiceProvider.class);
            List<DeviceGroup> groups;
            if (permission != null) {
                groups = groupManagementService.getGroups(userName, permission);
            } else {
                groups = groupManagementService.getGroups(userName);
            }
            DeviceGroup[] deviceGroups = new DeviceGroup[groups.size()];
            groups.toArray(deviceGroups);
            return Response.status(Response.Status.OK).entity(deviceGroups).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/count")
    @GET
    @Produces("application/json")
    public Response getGroupCount(@QueryParam("userName") String userName) {
        try {
            int count = this.getServiceProvider(GroupManagementServiceProvider.class).getGroupCount(userName);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}/share")
    @PUT
    @Produces("application/json")
    public Response shareGroup(@FormParam("userName") String userName,
                               @FormParam("shareUser") String shareUser, @PathParam("groupId") int groupId,
                               @FormParam("roleName") String sharingRole) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isShared = this.getServiceProvider(GroupManagementServiceProvider.class).shareGroup(
                    shareUser, groupId, sharingRole);
            if (isShared) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}/unshare")
    @PUT
    @Produces("application/json")
    public Response unShareGroup(@FormParam("userName") String userName,
                                 @FormParam("unShareUser") String unShareUser,
                                 @PathParam("groupId") int groupId,
                                 @FormParam("roleName") String sharingRole) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isUnShared = this.getServiceProvider(GroupManagementServiceProvider.class).unShareGroup(
                    unShareUser, groupId, sharingRole);
            if (isUnShared) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}/share/roles/{roleName}/permissions")
    @PUT
    @Produces("application/json")
    public Response addSharing(@QueryParam("userName") String userName, @PathParam("groupId") int groupId,
                               @PathParam("roleName") String roleName,
                               @FormParam("permissions") String[] permissions) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isAdded = this.getServiceProvider(GroupManagementServiceProvider.class).addSharing(
                    userName, groupId, roleName, permissions);
            if (isAdded) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @DELETE
    @Path("/groups/{groupId}/share/roles/{roleName}/permissions")
    @Produces("application/json")
    public Response removeSharing(@QueryParam("userName") String userName, @PathParam("groupId") int groupId,
                                  @PathParam("roleName") String roleName) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isRemoved = this.getServiceProvider(GroupManagementServiceProvider.class).removeSharing(
                    groupId, roleName);
            if (isRemoved) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @GET
    @Path("/groups/{groupId}/share/roles")
    @Produces("application/json")
    public Response getRoles(@PathParam("groupId") int groupId, @QueryParam("userName") String userName) {
        try {
            List<String> roles;
            if (userName != null && !userName.isEmpty()) {
                roles = this.getServiceProvider(GroupManagementServiceProvider.class).getRoles(userName,
                        groupId);
            } else {
                roles = this.getServiceProvider(GroupManagementServiceProvider.class).getRoles(groupId);
            }
            String[] rolesArray = new String[roles.size()];
            roles.toArray(rolesArray);
            return Response.status(Response.Status.OK).entity(rolesArray).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @GET
    @Path("/groups/{groupId}/users")
    @Produces("application/json")
    public Response getUsers(@PathParam("groupId") int groupId) {
        try {
            List<GroupUser> users = this.getServiceProvider(GroupManagementServiceProvider.class).getUsers(
                    groupId);
            GroupUser[] usersArray = new GroupUser[users.size()];
            users.toArray(usersArray);
            return Response.status(Response.Status.OK).entity(usersArray).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @GET
    @Path("/groups/{groupId}/devices/all")
    @Produces("application/json")
    public Response getDevices(@PathParam("groupId") int groupId) {
        try {
            List<Device> devices = this.getServiceProvider(GroupManagementServiceProvider.class).getDevices(
                    groupId);
            Device[] deviceArray = new Device[devices.size()];
            devices.toArray(deviceArray);
            return Response.status(Response.Status.OK).entity(deviceArray).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @GET
    @Path("/groups/{groupId}/devices/count")
    @Produces("application/json")
    public Response getDeviceCount(@PathParam("groupId") int groupId) {
        try {
            int count = this.getServiceProvider(GroupManagementServiceProvider.class).getDeviceCount(groupId);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @GET
    @Path("/groups/{groupId}/devices")
    @Produces("application/json")
    public Response getDevices(@PathParam("groupId") int groupId, @QueryParam("index") int index,
                               @QueryParam("limit") int limit) {
        try {
            PaginationRequest request = new PaginationRequest(index, limit);
            PaginationResult paginationResult = this.getServiceProvider(GroupManagementServiceProvider.class).getDevices(groupId, request);
            return Response.status(Response.Status.OK).entity(paginationResult).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @PUT
    @Path("/groups/{groupId}/devices/{deviceType}/{deviceId}")
    @Produces("application/json")
    public Response addDevice(@PathParam("groupId") int groupId, @PathParam("deviceId") String deviceId,
                              @PathParam("deviceType") String deviceType,
                              @FormParam("userName") String userName) {
        if (!checkAuthorize(getCurrentUserName(), groupId,
                "/permission/device-mgt/admin/groups/add_devices")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            boolean isAdded = this.getServiceProvider(GroupManagementServiceProvider.class).addDevice(
                    deviceIdentifier, groupId);
            if (isAdded) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @DELETE
    @Path("/groups/{groupId}/devices/{deviceType}/{deviceId}")
    @Produces("application/json")
    public Response removeDevice(@PathParam("groupId") int groupId, @PathParam("deviceId") String deviceId,
                                 @PathParam("deviceType") String deviceType) {
        if (!checkAuthorize(getCurrentUserName(), groupId,
                "/permission/device-mgt/admin/groups/remove_devices")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            boolean isRemoved = this.getServiceProvider(GroupManagementServiceProvider.class).removeDevice(
                    deviceIdentifier, groupId);
            if (isRemoved) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @GET
    @Path("/groups/{groupId}/users/{userName}/permissions")
    @Produces("application/json")
    public Response getPermissions(@PathParam("userName") String userName,
                                   @PathParam("groupId") int groupId) {
        try {
            String[] permissions = this.getServiceProvider(GroupManagementServiceProvider.class)
                    .getPermissions(userName, groupId);
            return Response.status(Response.Status.OK).entity(permissions).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            this.endTenantFlow();
        }
    }

    @GET
    @Path("/groups/{groupId}/users/{userName}/authorized")
    @Produces("application/json")
    public Response isAuthorized(@PathParam("userName") String userName, @PathParam("groupId") int groupId,
                                 @QueryParam("permission") String permission) {
        boolean isAuthorized = checkAuthorize(userName, groupId, permission);
        if (isAuthorized) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean checkAuthorize(String userName, int groupId, String permission) {
        try {
            return this.getServiceProvider(GroupManagementServiceProvider.class).isAuthorized(userName,
                    groupId, permission);
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

}