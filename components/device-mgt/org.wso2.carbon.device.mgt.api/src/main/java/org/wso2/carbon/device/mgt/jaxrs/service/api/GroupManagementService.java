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

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "Group Management", version = "1.0.0", context = "/api/device-mgt/v1.0/groups", tags = {"devicemgt_admin"})

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GroupManagementService {

    @GET
    @Permission(name = "View Group", permission = "/permission/admin/device-mgt/user/groups/list")
    Response getGroups(@QueryParam("user") String user, @QueryParam("offset") int offset,
                       @QueryParam("limit") int limit);

    @POST
    @Permission(name = "Add Group", permission = "/permission/admin/device-mgt/user/groups/add")
    Response createGroup(DeviceGroup group);

    @Path("/{groupName}")
    @GET
    @Permission(name = "View Group", permission = "/permission/admin/device-mgt/user/groups/view")
    Response getGroup(@PathParam("groupName") String groupName);

    @Path("/{groupName}")
    @PUT
    @Permission(name = "Update Group", permission = "/permission/admin/device-mgt/user/groups/update")
    Response updateGroup(@PathParam("groupName") String groupName, DeviceGroup deviceGroup);

    @Path("/{groupName}")
    @DELETE
    @Permission(name = "Remove Groups", permission = "/permission/admin/device-mgt/user/groups/remove")
    Response deleteGroup(@PathParam("groupName") String groupName);

    @Path("/{groupName}/share-with-user")
    @POST
    @Permission(name = "Share Group to a User", permission = "/permission/admin/device-mgt/user/groups/share")
    Response shareGroupWithUser(@PathParam("groupName") String groupName, String targetUser);

    @Path("/{groupName}/share-with-role")
    @POST
    @Permission(name = "Share Group to a Role", permission = "/permission/admin/device-mgt/user/groups/share")
    Response shareGroupWithRole(@PathParam("groupName") String groupName, String targetRole);

    @Path("/{groupName}/remove-share-with-user")
    @POST
    @Permission(name = "Unshare a Group", permission = "/permission/admin/device-mgt/user/groups/unshare")
    Response removeShareWithUser(@PathParam("groupName") String groupName, String targetUser);

    @Path("/{groupName}/remove-share-with-role")
    @POST
    @Permission(name = "Unshare a Group", permission = "/permission/admin/device-mgt/user/groups/unshare")
    Response removeShareWithRole(@PathParam("groupName") String groupName, String targetUser);

    @GET
    @Path("/{groupName}/users")
    @Permission(name = "Get Users of Group", permission = "/permission/admin/device-mgt/user/groups/list")
    Response getUsersOfGroup(@PathParam("groupName") String groupName);

    @GET
    @Path("/{groupName}/devices")
    @Permission(name = "Get Devices of Group", permission = "/permission/admin/device-mgt/groups/roles")
    Response getDevicesOfGroup(@PathParam("groupName") String groupName, @QueryParam("offset") int offset,
                               @QueryParam("limit") int limit);

    @POST
    @Path("/{groupName}/devices")
    @Produces("application/json")
    @Permission(name = "Add Device to a Group", permission = "/permission/admin/device-mgt/user/groups/devices/add")
    Response addDeviceToGroup(@PathParam("groupName") String groupName, DeviceIdentifier deviceIdentifier);

    @DELETE
    @Path("/{groupName}/devices")
    @Permission(name = "Remove Devices from Group",
                permission = "/permission/admin/device-mgt/user/groups/devices/remove")
    Response removeDeviceFromGroup(@PathParam("groupName") String groupName, @QueryParam("type") String type,
                                   @QueryParam("id") String id);

}
