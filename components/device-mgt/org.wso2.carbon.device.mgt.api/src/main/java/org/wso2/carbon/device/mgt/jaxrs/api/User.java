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
import org.wso2.carbon.device.mgt.jaxrs.beans.UserCredentialWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserWrapper;

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
import java.util.List;

/**
 * This represents the JAX-RS services of UserImpl related functionality.
 */
@Api(value = "User")
public interface User {

    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    Response addUser(UserWrapper userWrapper);

    @GET
    @Path("view")
    @Produces({MediaType.APPLICATION_JSON})
    Response getUser(@QueryParam("username") String username);

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    Response updateUser(UserWrapper userWrapper, @QueryParam("username") String username);

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    Response removeUser(@QueryParam("username") String username);

    @GET
    @Path("roles")
    @Produces({MediaType.APPLICATION_JSON})
    Response getRoles(@QueryParam("username") String username);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    Response getAllUsers();

    @GET
    @Path("{filter}")
    @Produces({MediaType.APPLICATION_JSON})
    Response getMatchingUsers(@PathParam("filter") String filter);

    @GET
    @Path("view-users")
    Response getAllUsersByUsername(@QueryParam("username") String userName);

    @GET
    @Path("users-by-username")
    Response getAllUserNamesByUsername(@QueryParam("username") String userName);

    @POST
    @Path("email-invitation")
    @Produces({MediaType.APPLICATION_JSON})
    Response inviteExistingUsersToEnrollDevice(List<String> usernames);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("devices")
    Response getAllDeviceOfUser(@QueryParam("username") String username, @QueryParam("start") int startIdx,
                                @QueryParam("length") int length);

    @GET
                                       @Path("count")
    Response getUserCount();

    @PUT
                                       @Path("{roleName}/users")
                                       @Produces({MediaType.APPLICATION_JSON})
    Response updateRoles(@PathParam("username") String username, List<String> userList);

    @POST
                                       @Path("change-password")
                                       @Consumes({MediaType.APPLICATION_JSON})
                                       @Produces({MediaType.APPLICATION_JSON})
    Response resetPassword(UserCredentialWrapper credentials);

    @POST
                                       @Path("reset-password")
                                       @Consumes({MediaType.APPLICATION_JSON})
                                       @Produces({MediaType.APPLICATION_JSON})
    Response resetPasswordByAdmin(UserCredentialWrapper credentials);
}
