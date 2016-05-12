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

package org.wso2.carbon.device.mgt.jaxrs.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyEixistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupUser;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.api.Group;
import org.wso2.carbon.device.mgt.jaxrs.api.util.DeviceMgtAPIUtils;
import org.wso2.carbon.user.core.multiplecredentials.UserDoesNotExistException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@SuppressWarnings("NonJaxWsWebServices")
public class GroupImpl implements Group {

    private static Log log = LogFactory.getLog(GroupImpl.class);

    @Override
    @POST
    @Consumes("application/json")
    public Response createGroup(DeviceGroup group) {
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        group.setOwner(owner);
        group.setDateOfCreation(new Date().getTime());
        group.setDateOfLastUpdate(new Date().getTime());
        try {
            GroupManagementProviderService groupManagementService = DeviceMgtAPIUtils.getGroupManagementProviderService();
            groupManagementService.createGroup(group, DeviceGroupConstants.Roles.DEFAULT_ADMIN_ROLE, DeviceGroupConstants.Permissions.DEFAULT_ADMIN_PERMISSIONS);
            groupManagementService.addGroupSharingRole(owner, group.getName(), owner,
                                                       DeviceGroupConstants.Roles.DEFAULT_OPERATOR_ROLE,
                                                       DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS);
            groupManagementService.addGroupSharingRole(owner, group.getName(), owner, DeviceGroupConstants.Roles.DEFAULT_STATS_MONITOR_ROLE,
                                                       DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS);
            groupManagementService.addGroupSharingRole(owner, group.getName(), owner, DeviceGroupConstants.Roles.DEFAULT_VIEW_POLICIES,
                                                       DeviceGroupConstants.Permissions.DEFAULT_VIEW_POLICIES_PERMISSIONS);
            groupManagementService.addGroupSharingRole(owner, group.getName(), owner, DeviceGroupConstants.Roles.DEFAULT_MANAGE_POLICIES,
                                                       DeviceGroupConstants.Permissions.DEFAULT_MANAGE_POLICIES_PERMISSIONS);
            groupManagementService.addGroupSharingRole(owner, group.getName(), owner, DeviceGroupConstants.Roles.DEFAULT_VIEW_EVENTS,
                                                       DeviceGroupConstants.Permissions.DEFAULT_VIEW_EVENTS_PERMISSIONS);
            return Response.status(Response.Status.CREATED).build();
        } catch (GroupAlreadyEixistException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/owner/{owner}/name/{groupName}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                                DeviceGroup deviceGroup) {
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().updateGroup(deviceGroup, groupName, owner);
            return Response.status(Response.Status.OK).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/owner/{owner}/name/{groupName}")
    @DELETE
    public Response deleteGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner) {
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().deleteGroup(groupName, owner);
            return Response.status(Response.Status.OK).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @GET
    @Produces("application/json")
    public Response getGroups(@QueryParam("start") int startIndex, @PathParam("length") int length) {
        try {
            PaginationResult paginationResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getGroups(startIndex, length);
            if (paginationResult.getRecordsTotal() > 0) {
                return Response.status(Response.Status.OK).entity(paginationResult).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/all")
    @GET
    @Produces("application/json")
    public Response getAllGroups() {
        try {
            GroupManagementProviderService groupManagementProviderService = DeviceMgtAPIUtils
                    .getGroupManagementProviderService();
            PaginationResult paginationResult = groupManagementProviderService
                    .getGroups(0, groupManagementProviderService.getGroupCount());
            if (paginationResult.getRecordsTotal() > 0) {
                return Response.status(Response.Status.OK).entity(paginationResult.getData()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/user/{user}")
    @GET
    @Produces("application/json")
    public Response getGroups(@PathParam("user") String userName, @QueryParam("start") int startIndex,
                              @QueryParam("length") int length) {
        try {
            PaginationResult paginationResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getGroups(userName, startIndex, length);
            if (paginationResult.getRecordsTotal() > 0) {
                return Response.status(Response.Status.OK).entity(paginationResult).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/user/{user}/all")
    @GET
    @Produces("application/json")
    public Response getGroups(@PathParam("user") String userName) {
        try {
            List<DeviceGroup> deviceGroups = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getGroups(userName);
            if (deviceGroups.size() > 0) {
                return Response.status(Response.Status.OK).entity(deviceGroups).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/owner/{owner}/name/{groupName}")
    @GET
    @Produces("application/json")
    public Response getGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner) {
        try {
            DeviceGroup deviceGroup = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroup(groupName, owner);
            if (deviceGroup != null) {
                return Response.status(Response.Status.OK).entity(deviceGroup).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/user/{user}/search")
    @GET
    @Produces("application/json")
    public Response findGroups(@QueryParam("groupName") String groupName, @PathParam("user") String user) {
        try {
            List<DeviceGroup> groups = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .findInGroups(groupName, user);
            DeviceGroup[] deviceGroups = new DeviceGroup[groups.size()];
            groups.toArray(deviceGroups);
            return Response.status(Response.Status.OK).entity(deviceGroups).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/user/{user}/all")
    @GET
    @Produces("application/json")
    public Response getGroups(@PathParam("user") String userName, @QueryParam("permission") String permission) {
        try {
            GroupManagementProviderService groupManagementService = DeviceMgtAPIUtils.getGroupManagementProviderService();
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
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/count")
    @GET
    @Produces("application/json")
    public Response getAllGroupCount() {
        try {
            int count = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroupCount();
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/user/{user}/count")
    @GET
    @Produces("application/json")
    public Response getGroupCount(@PathParam("user") String userName) {
        try {
            int count = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroupCount(userName);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/owner/{owner}/name/{groupName}/share")
    @PUT
    @Produces("application/json")
    public Response shareGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                               @FormParam("shareUser") String shareUser,
                               @FormParam("roleName") String sharingRole) {

        try {
            boolean isShared = DeviceMgtAPIUtils.getGroupManagementProviderService().shareGroup(
                    shareUser, groupName, owner, sharingRole);
            if (isShared) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("GroupImpl not found").build();
            }
        } catch (UserDoesNotExistException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/owner/{owner}/name/{groupName}/unshare")
    @PUT
    @Produces("application/json")
    public Response unShareGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                                 @FormParam("unShareUser") String unShareUser,
                                 @FormParam("roleName") String sharingRole) {
        try {
            boolean isUnShared = DeviceMgtAPIUtils.getGroupManagementProviderService().unshareGroup(
                    unShareUser, groupName, owner, sharingRole);
            if (isUnShared) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("GroupImpl not found").build();
            }
        } catch (UserDoesNotExistException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @Path("/owner/{owner}/name/{groupName}/share/roles/{roleName}/permissions")
    @PUT
    @Produces("application/json")
    public Response addSharing(@QueryParam("shareUser") String shareUser,
                               @PathParam("groupName") String groupName, @PathParam("owner") String owner,
                               @PathParam("roleName") String roleName,
                               @FormParam("permissions") String[] permissions) {

        try {
            boolean isAdded = DeviceMgtAPIUtils.getGroupManagementProviderService().addGroupSharingRole(
                    shareUser, groupName, owner, roleName, permissions);
            if (isAdded) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @DELETE
    @Path("/owner/{owner}/name/{groupName}/share/roles/{roleName}/permissions")
    @Produces("application/json")
    public Response removeSharing(@QueryParam("userName") String userName,
                                  @PathParam("groupName") String groupName, @PathParam("owner") String owner,
                                  @PathParam("roleName") String roleName) {
        try {
            boolean isRemoved = DeviceMgtAPIUtils.getGroupManagementProviderService().removeGroupSharingRole(
                    groupName, owner, roleName);
            if (isRemoved) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @GET
    @Path("/owner/{owner}/name/{groupName}/share/roles")
    @Produces("application/json")
    public Response getRoles(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                             @QueryParam("userName") String userName) {
        try {
            List<String> roles;
            if (userName != null && !userName.isEmpty()) {
                roles = DeviceMgtAPIUtils.getGroupManagementProviderService().getRoles(userName, groupName, owner);
            } else {
                roles = DeviceMgtAPIUtils.getGroupManagementProviderService().getRoles(groupName, owner);
            }
            String[] rolesArray = new String[roles.size()];
            roles.toArray(rolesArray);
            return Response.status(Response.Status.OK).entity(rolesArray).build();
        } catch (UserDoesNotExistException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @PUT
    @Path("/owner/{owner}/name/{groupName}/user/{userName}/share/roles")
    @Produces("application/json")
    public Response setRoles(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                             @PathParam("userName") String userName, List<String> selectedRoles) {
        try {
            List<String> allRoles = DeviceMgtAPIUtils.getGroupManagementProviderService().getRoles(groupName, owner);
            for (String role : allRoles) {
                if (selectedRoles.contains(role)) {
                    DeviceMgtAPIUtils.getGroupManagementProviderService()
                            .shareGroup(userName, groupName, owner, role);
                } else {
                    DeviceMgtAPIUtils.getGroupManagementProviderService()
                            .unshareGroup(userName, groupName, owner, role);
                }
            }
            return Response.status(Response.Status.OK).build();
        } catch (UserDoesNotExistException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @GET
    @Path("/owner/{owner}/name/{groupName}/users")
    @Produces("application/json")
    public Response getUsers(@PathParam("groupName") String groupName, @PathParam("owner") String owner) {
        try {
            List<GroupUser> users = DeviceMgtAPIUtils.getGroupManagementProviderService().getUsers(
                    groupName, owner);
            GroupUser[] usersArray = new GroupUser[users.size()];
            users.toArray(usersArray);
            return Response.status(Response.Status.OK).entity(usersArray).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @GET
    @Path("/owner/{owner}/name/{groupName}/devices")
    @Produces("application/json")
    public Response getDevices(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                               @QueryParam("start") int startIdx, @QueryParam("length") int length) {
        try {
            PaginationResult paginationResult = DeviceMgtAPIUtils
                    .getGroupManagementProviderService().getDevices(groupName, owner, startIdx, length);
            if (paginationResult.getRecordsTotal() > 0) {
                return Response.status(Response.Status.OK).entity(paginationResult).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @GET
    @Path("/owner/{owner}/name/{groupName}/devices/count")
    @Produces("application/json")
    public Response getDeviceCount(@PathParam("groupName") String groupName, @PathParam("owner") String owner) {
        try {
            int count = DeviceMgtAPIUtils.getGroupManagementProviderService().getDeviceCount(groupName, owner);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @POST
    @Path("/owner/{owner}/name/{groupName}/devices")
    @Produces("application/json")
    public Response addDevice(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                              DeviceIdentifier deviceIdentifier) {
        try {
            boolean isAdded = DeviceMgtAPIUtils.getGroupManagementProviderService().addDevice(
                    deviceIdentifier, groupName, owner);
            if (isAdded) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @DELETE
    @Path("/owner/{owner}/name/{groupName}/devices/{deviceType}/{deviceId}")
    @Produces("application/json")
    public Response removeDevice(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                                 @PathParam("deviceId") String deviceId,
                                 @PathParam("deviceType") String deviceType) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            boolean isRemoved = DeviceMgtAPIUtils.getGroupManagementProviderService().removeDevice(
                    deviceIdentifier, groupName, owner);
            if (isRemoved) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Override
    @GET
    @Path("/owner/{owner}/name/{groupName}/users/{userName}/permissions")
    @Produces("application/json")
    public Response getPermissions(@PathParam("userName") String userName,
                                   @PathParam("groupName") String groupName, @PathParam("owner") String owner) {
        try {
            String[] permissions = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getPermissions(userName, groupName, owner);
            return Response.status(Response.Status.OK).entity(permissions).build();
        } catch (UserDoesNotExistException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

}
