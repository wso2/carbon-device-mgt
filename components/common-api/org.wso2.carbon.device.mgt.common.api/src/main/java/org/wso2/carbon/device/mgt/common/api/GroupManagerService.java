/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.common.GroupManagementException;
import org.wso2.carbon.device.mgt.group.common.GroupUser;
import org.wso2.carbon.device.mgt.group.core.providers.GroupManagementServiceProvider;

import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@WebService
public class GroupManagerService {

    private static final String DEFAULT_ADMIN_ROLE = "admin";
    private static final String DEFAULT_OPERATOR_ROLE = "invoke-device-operations";
    private static final String DEFAULT_STATS_MONITOR_ROLE = "view-statistics";
    private static final String DEFAULT_VIEW_POLICIES = "view-policies";
    private static final String DEFAULT_MANAGE_POLICIES = "mange-policies";
    private static final String DEFAULT_VIEW_EVENTS = "view-events";
    private static final String[] DEFAULT_ADMIN_PERMISSIONS = {"/permission/device-mgt/admin/groups",
            "/permission/device-mgt/user/groups"};
    private static final String[] DEFAULT_OPERATOR_PERMISSIONS = {"/permission/device-mgt/user/groups/device_operation"};
    private static final String[] DEFAULT_STATS_MONITOR_PERMISSIONS = {"/permission/device-mgt/user/groups/device_monitor"};
    private static final String[] DEFAULT_MANAGE_POLICIES_PERMISSIONS = {"/permission/device-mgt/user/groups/device_policies/add"};
    private static final String[] DEFAULT_VIEW_POLICIES_PERMISSIONS = {"/permission/device-mgt/user/groups/device_policies/view"};
    private static final String[] DEFAULT_VIEW_EVENTS_PERMISSIONS = {"/permission/device-mgt/user/groups/device_events"};
    private static Log log = LogFactory.getLog(GroupManagerService.class);
    @Context  //injected response proxy supporting multiple threads
    private HttpServletResponse response;
    private PrivilegedCarbonContext ctx;

    private GroupManagementServiceProvider getServiceProvider() {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        PrivilegedCarbonContext.startTenantFlow();
        ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(tenantDomain, true);
        if (log.isDebugEnabled()) {
            log.debug("Getting thread local carbon context for tenant domain: " + tenantDomain);
        }
        return (GroupManagementServiceProvider) ctx.getOSGiService(GroupManagementServiceProvider.class, null);
    }

    private void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
        ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Tenant flow ended");
        }
    }

    @Path("/group")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public boolean createGroup(@FormParam("name") String name, @FormParam("username") String username,
            @FormParam("description") String description) {
        DeviceGroup group = new DeviceGroup();
        group.setName(name);
        group.setDescription(description);
        group.setOwner(username);
        group.setDateOfCreation(new Date().getTime());
        group.setDateOfLastUpdate(new Date().getTime());
        boolean isAdded = false;
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider();
            int groupId = groupManagementService.createGroup(group, DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
            response.setStatus(Response.Status.OK.getStatusCode());
            isAdded = (groupId > 0) && groupManagementService.addSharing(username, groupId, DEFAULT_OPERATOR_ROLE,
                    DEFAULT_OPERATOR_PERMISSIONS);
            groupManagementService.addSharing(username, groupId, DEFAULT_STATS_MONITOR_ROLE,
                                              DEFAULT_STATS_MONITOR_PERMISSIONS);
            groupManagementService.addSharing(username, groupId, DEFAULT_VIEW_POLICIES,
                                              DEFAULT_VIEW_POLICIES_PERMISSIONS);
            groupManagementService.addSharing(username, groupId, DEFAULT_MANAGE_POLICIES,
                                              DEFAULT_MANAGE_POLICIES_PERMISSIONS);
            groupManagementService.addSharing(username, groupId, DEFAULT_VIEW_EVENTS,
                                              DEFAULT_VIEW_EVENTS_PERMISSIONS);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isAdded;
    }

    @Path("/group/id/{groupId}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public boolean updateGroup(@PathParam("groupId") int groupId, @FormParam("name") String name,
            @FormParam("username") String username, @FormParam("description") String description) {
        if (!isAuthorized(username, groupId, "/permission/device-mgt/admin/groups/modify")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider();
            DeviceGroup group = groupManagementService.getGroup(groupId);
            group.setName(name);
            group.setDescription(description);
            group.setOwner(username);
            group.setDateOfLastUpdate(new Date().getTime());
            response.setStatus(Response.Status.OK.getStatusCode());
            groupManagementService.updateGroup(group);
            return true;
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/group/id/{groupId}")
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    public boolean deleteGroup(@PathParam("groupId") int groupId, @QueryParam("username") String username) {
        if (!isAuthorized(username, groupId, "/permission/device-mgt/admin/groups/delete")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isDeleted = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isDeleted = this.getServiceProvider().deleteGroup(groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isDeleted;
    }

    @Path("/group/id/{groupId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public DeviceGroup getGroup(@PathParam("groupId") int groupId, @FormParam("username") String username) {
        DeviceGroup deviceGroup = null;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            deviceGroup = this.getServiceProvider().getGroup(groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return deviceGroup;
    }

    @Path("/group/name/{groupName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public DeviceGroup[] findGroups(@PathParam("groupName") String groupName, @FormParam("username") String username) {
        DeviceGroup[] deviceGroups = null;
        try {
            List<DeviceGroup> groups = this.getServiceProvider().findGroups(groupName, username);
            deviceGroups = new DeviceGroup[groups.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            groups.toArray(deviceGroups);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return deviceGroups;
    }

    @Path("/group/user/{username}/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public DeviceGroup[] getGroups(@PathParam("username") String username,
            @QueryParam("permission") String permission) {
        DeviceGroup[] deviceGroups = null;
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider();
            List<DeviceGroup> groups;
            if(permission != null){
                groups = groupManagementService.getGroups(username, permission);
            }else{
                groups = groupManagementService.getGroups(username);
            }
            deviceGroups = new DeviceGroup[groups.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            groups.toArray(deviceGroups);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return deviceGroups;
    }

    @Path("/group/user/{username}/all/count")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public int getGroupCount(@PathParam("username") String username) {
        int count = -1;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            count = this.getServiceProvider().getGroupCount(username);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return count;
    }

    @Path("/group/id/{groupId}/share")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public boolean shareGroup(@FormParam("username") String username, @FormParam("shareUser") String shareUser,
            @PathParam("groupId") int groupId, @FormParam("role") String sharingRole) {
        if (!isAuthorized(username, groupId, "/permission/device-mgt/admin/groups/share")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isShared = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isShared = this.getServiceProvider().shareGroup(shareUser, groupId, sharingRole);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isShared;
    }

    @Path("/group/id/{groupId}/unshare")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public boolean unShareGroup(@FormParam("username") String username, @FormParam("unShareUser") String unShareUser,
            @PathParam("groupId") int groupId, @FormParam("role") String sharingRole) {
        if (!isAuthorized(username, groupId, "/permission/device-mgt/admin/groups/share")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isUnShared = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isUnShared = this.getServiceProvider().unShareGroup(unShareUser, groupId, sharingRole);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isUnShared;
    }

    @Path("/group/id/{groupId}/role")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public boolean addSharing(@FormParam("username") String username, @PathParam("groupId") int groupId,
            @FormParam("role") String roleName, @FormParam("permissions") String[] permissions) {
        if (!isAuthorized(username, groupId, "/permission/device-mgt/admin/groups/share")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isAdded = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isAdded = this.getServiceProvider().addSharing(username, groupId, roleName, permissions);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isAdded;
    }

    @Path("/group/id/{groupId}/role/{role}")
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    public boolean removeSharing(@QueryParam("username") String username, @PathParam("groupId") int groupId,
            @PathParam("role") String roleName) {
        if (!isAuthorized(username, groupId, "/permission/device-mgt/admin/groups/share")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
        }
        boolean isRemoved = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isRemoved = this.getServiceProvider().removeSharing(groupId, roleName);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isRemoved;
    }

    @Path("/group/id/{groupId}/role/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public String[] getRoles(@PathParam("groupId") int groupId) {
        String[] rolesArray = null;
        try {
            List<String> roles = this.getServiceProvider().getRoles(groupId);
            rolesArray = new String[roles.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            roles.toArray(rolesArray);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return rolesArray;
    }

    @Path("/group/id/{groupId}/{user}/role/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public String[] getRoles(@PathParam("user") String user, @PathParam("groupId") int groupId) {
        String[] rolesArray = null;
        try {
            List<String> roles = this.getServiceProvider().getRoles(user, groupId);
            rolesArray = new String[roles.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            roles.toArray(rolesArray);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return rolesArray;
    }

    @Path("/group/id/{groupId}/user/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public GroupUser[] getUsers(@PathParam("groupId") int groupId) {
        GroupUser[] usersArray = null;
        try {
            List<GroupUser> users = this.getServiceProvider().getUsers(groupId);
            usersArray = new GroupUser[users.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            users.toArray(usersArray);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return usersArray;
    }

    @Path("/group/id/{groupId}/device/all")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getDevices(@PathParam("groupId") int groupId) {
        Device[] deviceArray = null;
        try {
            List<Device> devices = this.getServiceProvider().getDevices(groupId);
            deviceArray = new Device[devices.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            devices.toArray(deviceArray);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return deviceArray;
    }

    @Path("/group/id/{groupId}/device/count")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public int getDeviceCount(@PathParam("groupId") int groupId) {
        try {
            return this.getServiceProvider().getDeviceCount(groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
            return -1;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/group/id/{groupId}/device")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public PaginationResult getDevices(@PathParam("groupId") int groupId,
                                       @QueryParam("index") int index,
                                       @QueryParam("limit") int limit) {
        try {
            return this.getServiceProvider().getDevices(groupId, index, limit);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/group/id/{groupId}/device/assign")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public boolean addDevice(@PathParam("groupId") int groupId, @FormParam("deviceId") String deviceId,
            @FormParam("deviceType") String deviceType, @FormParam("username") String username) {
        if (!isAuthorized(username, groupId, "/permission/device-mgt/admin/groups/add_devices")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isAdded = false;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            response.setStatus(Response.Status.OK.getStatusCode());
            isAdded = this.getServiceProvider().addDevice(deviceIdentifier, groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isAdded;
    }

    @Path("/group/id/{groupId}/device/assign")
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    public boolean removeDevice(@PathParam("groupId") int groupId, @FormParam("deviceId") String deviceId,
            @FormParam("deviceType") String deviceType, @FormParam("username") String username) {
        if (!isAuthorized(username, groupId, "/permission/device-mgt/admin/groups/remove_devices")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isRemoved = false;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            response.setStatus(Response.Status.OK.getStatusCode());
            isRemoved = this.getServiceProvider().removeDevice(deviceIdentifier, groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isRemoved;
    }

    @Path("/group/id/{groupId}/user/{username}/permissions")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public String[] getPermissions(@PathParam("username") String username, @PathParam("groupId") int groupId) {
        String[] permissions = null;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            permissions = this.getServiceProvider().getPermissions(username, groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return permissions;
    }

    @Path("/group/id/{groupId}/user/{username}/authorized")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public boolean isAuthorized(@PathParam("username") String username, @PathParam("groupId") int groupId,
            @QueryParam("permission") String permission){
        boolean isAuthorized = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isAuthorized = this.getServiceProvider().isAuthorized(username, groupId, permission);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isAuthorized;
    }

}
