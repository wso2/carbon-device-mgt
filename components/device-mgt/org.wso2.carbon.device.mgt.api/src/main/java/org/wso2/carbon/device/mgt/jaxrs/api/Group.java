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

package org.wso2.carbon.device.mgt.jaxrs.api;

import io.swagger.annotations.Api;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;

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
import java.util.List;

@API(name = "Group", version = "1.0.0", context = "/devicemgt_admin/groups", tags = {"devicemgt_admin"})

// Below Api is for swagger annotations
@Path("/groups")
@Api(value = "Group", description = "Group related operations such as get all the available groups, etc.")
@SuppressWarnings("NonJaxWsWebServices")
public interface Group {

    @GET
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getGroups(@QueryParam("start") int startIndex, @QueryParam("length") int length);

    @POST
    @Consumes("application/json")
    @Permission(scope = "group-add", permissions = {"/permission/admin/device-mgt/user/groups/add"})
    Response createGroup(DeviceGroup group);

    @Path("/owner/{owner}/name/{groupName}")
    @GET
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/view"})
    Response getGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner);

    @Path("/owner/{owner}/name/{groupName}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    @Permission(scope = "group-modify", permissions = {"/permission/admin/device-mgt/user/groups/update"})
    Response updateGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                         DeviceGroup deviceGroup);

    @Path("/owner/{owner}/name/{groupName}")
    @DELETE
    @Permission(scope = "group-remove", permissions = {"/permission/admin/device-mgt/user/groups/remove"})
    Response deleteGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner);



    @Path("/all")
    @GET
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getAllGroups();

    @Path("/user/{user}")
    @GET
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getGroups(@PathParam("user") String userName, @QueryParam("start") int startIndex,
                       @QueryParam("length") int length);

    @Path("/user/{user}/search")
    @GET
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response findGroups(@QueryParam("groupName") String groupName, @PathParam("user") String user);

    @Path("/user/{user}/all")
    @GET
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getGroups(@PathParam("user") String userName, @QueryParam("permission") String permission);

    @Path("/count")
    @GET
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getAllGroupCount();

    @Path("/user/{user}/count")
    @GET
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getGroupCount(@PathParam("user") String userName);

    @Path("/owner/{owner}/name/{groupName}/share")
    @PUT
    @Produces("application/json")
    @Permission(scope = "group-share", permissions = {"/permission/admin/device-mgt/user/groups/share"})
    Response shareGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                        @FormParam("shareUser") String shareUser, @FormParam("roleName") String sharingRole);

    @Path("/owner/{owner}/name/{groupName}/unshare")
    @PUT
    @Produces("application/json")
    @Permission(scope = "group-share", permissions = {"/permission/admin/device-mgt/user/groups/unshare"})
    Response unShareGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                          @FormParam("unShareUser") String unShareUser,
                          @FormParam("roleName") String sharingRole);

    @Path("/owner/{owner}/name/{groupName}/share/roles/{roleName}/permissions")
    @PUT
    @Produces("application/json")
    @Permission(scope = "group-add", permissions = {"/permission/admin/device-mgt/admin/groups/roles/permissions/add"})
    Response addSharing(@QueryParam("shareUser") String shareUser, @PathParam("groupName") String groupName,
                        @PathParam("owner") String owner, @PathParam("roleName") String roleName, String[] permissions);

    @DELETE
    @Path("/owner/{owner}/name/{groupName}/share/roles/{roleName}/permissions")
    @Produces("application/json")
    @Permission(scope = "group-remove", permissions = {"/permission/admin/device-mgt/admin/groups/roles/permissions/remove"})
    Response removeSharing(@QueryParam("userName") String userName, @PathParam("groupName") String groupName,
                           @PathParam("owner") String owner, @PathParam("roleName") String roleName);

    @GET
    @Path("/owner/{owner}/name/{groupName}/share/roles")
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/admin/groups/roles"})
    Response getRoles(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                      @QueryParam("userName") String userName);

    @PUT
    @Path("/owner/{owner}/name/{groupName}/user/{userName}/share/roles")
    @Produces("application/json")
    @Permission(scope = "group-modify", permissions = {"/permission/admin/device-mgt/admin/groups/roles"})
    Response setRoles(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                      @PathParam("userName") String userName, List<String> selectedRoles);

    @GET
    @Path("/owner/{owner}/name/{groupName}/users")
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getUsers(@PathParam("groupName") String groupName, @PathParam("owner") String owner);

    @GET
    @Path("/owner/{owner}/name/{groupName}/devices")
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/admin/groups/roles"})
    Response getDevices(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                        @QueryParam("start") int startIdx, @QueryParam("length") int length);

    @GET
    @Path("/owner/{owner}/name/{groupName}/devices/count")
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/devices/count"})
    Response getDeviceCount(@PathParam("groupName") String groupName, @PathParam("owner") String owner);

    @POST
    @Path("/owner/{owner}/name/{groupName}/devices")
    @Produces("application/json")
    @Permission(scope = "group-add", permissions = {"/permission/admin/device-mgt/user/groups/devices/add"})
    Response addDevice(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                       DeviceIdentifier deviceIdentifier);

    @DELETE
    @Path("/owner/{owner}/name/{groupName}/devices/{deviceType}/{deviceId}")
    @Produces("application/json")
    @Permission(scope = "group-remove", permissions = {"/permission/admin/device-mgt/user/groups/devices/remove"})
    Response removeDevice(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                          @PathParam("deviceId") String deviceId, @PathParam("deviceType") String deviceType);

    @GET
    @Path("/owner/{owner}/name/{groupName}/users/{userName}/permissions")
    @Produces("application/json")
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/roles/permissions"})
    Response getPermissions(@PathParam("userName") String userName, @PathParam("groupName") String groupName,
                            @PathParam("owner") String owner);
}
