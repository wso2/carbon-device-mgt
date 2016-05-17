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

@Path("/groups")
@Api(value = "Group", description = "Group related operations such as get all the available groups, etc.")
@SuppressWarnings("NonJaxWsWebServices")
public interface Group {

    @POST
    @Consumes("application/json")
    Response createGroup(DeviceGroup group);

    @Path("/owner/{owner}/name/{groupName}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    Response updateGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                         DeviceGroup deviceGroup);

    @Path("/owner/{owner}/name/{groupName}")
    @DELETE
    Response deleteGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner);

    @GET
    @Produces("application/json")
    Response getGroups(@QueryParam("start") int startIndex, @QueryParam("length") int length);

    @Path("/all")
    @GET
    @Produces("application/json")
    Response getAllGroups();

    @Path("/user/{user}")
    @GET
    @Produces("application/json")
    Response getGroups(@PathParam("user") String userName, @QueryParam("start") int startIndex,
                       @QueryParam("length") int length);

    @Path("/user/{user}/all")
    @GET
    @Produces("application/json")
    Response getGroups(@PathParam("user") String userName);

    @Path("/owner/{owner}/name/{groupName}")
    @GET
    @Produces("application/json")
    Response getGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner);

    @Path("/user/{user}/search")
    @GET
    @Produces("application/json")
    Response findGroups(@QueryParam("groupName") String groupName, @PathParam("user") String user);

    @Path("/user/{user}/all")
    @GET
    @Produces("application/json")
    Response getGroups(@PathParam("user") String userName, @QueryParam("permission") String permission);

    @Path("/count")
    @GET
    @Produces("application/json")
    Response getAllGroupCount();

    @Path("/user/{user}/count")
    @GET
    @Produces("application/json")
    Response getGroupCount(@PathParam("user") String userName);

    @Path("/owner/{owner}/name/{groupName}/share")
    @PUT
    @Produces("application/json")
    Response shareGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                        @FormParam("shareUser") String shareUser, @FormParam("roleName") String sharingRole);

    @Path("/owner/{owner}/name/{groupName}/unshare")
    @PUT
    @Produces("application/json")
    Response unShareGroup(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                          @FormParam("unShareUser") String unShareUser,
                          @FormParam("roleName") String sharingRole);

    @Path("/owner/{owner}/name/{groupName}/share/roles/{roleName}/permissions")
    @PUT
    @Produces("application/json")
    Response addSharing(@QueryParam("shareUser") String shareUser, @PathParam("groupName") String groupName,
                        @PathParam("owner") String owner, @PathParam("roleName") String roleName, String[] permissions);

    @DELETE
    @Path("/owner/{owner}/name/{groupName}/share/roles/{roleName}/permissions")
    @Produces("application/json")
    Response removeSharing(@QueryParam("userName") String userName, @PathParam("groupName") String groupName,
                           @PathParam("owner") String owner, @PathParam("roleName") String roleName);

    @GET
    @Path("/owner/{owner}/name/{groupName}/share/roles")
    @Produces("application/json")
    Response getRoles(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                      @QueryParam("userName") String userName);

    @PUT
    @Path("/owner/{owner}/name/{groupName}/user/{userName}/share/roles")
    @Produces("application/json")
    Response setRoles(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                      @PathParam("userName") String userName, List<String> selectedRoles);

    @GET
    @Path("/owner/{owner}/name/{groupName}/users")
    @Produces("application/json")
    Response getUsers(@PathParam("groupName") String groupName, @PathParam("owner") String owner);

    @GET
    @Path("/owner/{owner}/name/{groupName}/devices")
    @Produces("application/json")
    Response getDevices(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                        @QueryParam("start") int startIdx, @QueryParam("length") int length);

    @GET
    @Path("/owner/{owner}/name/{groupName}/devices/count")
    @Produces("application/json")
    Response getDeviceCount(@PathParam("groupName") String groupName, @PathParam("owner") String owner);

    @POST
    @Path("/owner/{owner}/name/{groupName}/devices")
    @Produces("application/json")
    Response addDevice(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                       DeviceIdentifier deviceIdentifier);

    @DELETE
    @Path("/owner/{owner}/name/{groupName}/devices/{deviceType}/{deviceId}")
    @Produces("application/json")
    Response removeDevice(@PathParam("groupName") String groupName, @PathParam("owner") String owner,
                          @PathParam("deviceId") String deviceId, @PathParam("deviceType") String deviceType);

    @GET
    @Path("/owner/{owner}/name/{groupName}/users/{userName}/permissions")
    @Produces("application/json")
    Response getPermissions(@PathParam("userName") String userName, @PathParam("groupName") String groupName,
                            @PathParam("owner") String owner);
}
