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
package org.wso2.carbon.device.mgt.jaxrs.service.api;

import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GroupManagementService {

    @GET
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getGroups(@QueryParam("offset") int offset, @QueryParam("limit") int limit);

    @POST
    @Permission(scope = "group-add", permissions = {"/permission/admin/device-mgt/user/groups/add"})
    Response createGroup(DeviceGroup group);

    @Path("/{groupName}")
    @GET
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/view"})
    Response getGroup(@PathParam("groupName") String groupName);

    @Path("/{groupName}")
    @PUT
    @Permission(scope = "group-modify", permissions = {"/permission/admin/device-mgt/user/groups/update"})
    Response updateGroup(@PathParam("groupName") String groupName, DeviceGroup deviceGroup);

    @Path("/{groupName}")
    @DELETE
    @Permission(scope = "group-remove", permissions = {"/permission/admin/device-mgt/user/groups/remove"})
    Response deleteGroup(@PathParam("groupName") String groupName);

    @Path("/share-group-with-user")
    @POST
    @Permission(scope = "group-share", permissions = {"/permission/admin/device-mgt/user/groups/share"})
    Response shareGroupWithUser(String groupName, String targetUser);

    @Path("/share-group-with-role")
    @POST
    @Permission(scope = "group-share", permissions = {"/permission/admin/device-mgt/user/groups/share"})
    Response shareGroupWithRole(String groupName, String targetRole);

    @Path("/remove-share-with-user")
    @POST
    @Permission(scope = "group-share", permissions = {"/permission/admin/device-mgt/user/groups/unshare"})
    Response removeShareWithUser(@PathParam("groupName") String groupName, @QueryParam("username") String targetUser);

    @Path("/remove-share-with-role")
    @POST
    @Permission(scope = "group-share", permissions = {"/permission/admin/device-mgt/user/groups/unshare"})
    Response removeShareWithRole(@PathParam("groupName") String groupName, @QueryParam("roleName") String targetUser);

    @GET
    @Path("/{groupName}/users")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getUsersOfGroup(@PathParam("groupName") String groupName);

    @GET
    @Path("/{groupName}/devices")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/admin/groups/roles"})
    Response getDevicesOfGroup(@PathParam("groupName") String groupName, @QueryParam("offset") int offset,
                        @QueryParam("limit") int limit);

    @POST
    @Path("/{groupName}/devices")
    @Produces("application/json")
    @Permission(scope = "group-add", permissions = {"/permission/admin/device-mgt/user/groups/devices/add"})
    Response addDeviceToGroup(@PathParam("groupName") String groupName, DeviceIdentifier deviceIdentifier);

    @DELETE
    @Path("/{groupName}/devices")
    @Permission(scope = "group-remove", permissions = {"/permission/admin/device-mgt/user/groups/devices/remove"})
    Response removeDeviceFromGroup(@PathParam("groupName") String groupName, @QueryParam("type") String type,
                                   @QueryParam("id") String id);

    @GET
    Response getGroupsByUser(@QueryParam("user") String user);

}
