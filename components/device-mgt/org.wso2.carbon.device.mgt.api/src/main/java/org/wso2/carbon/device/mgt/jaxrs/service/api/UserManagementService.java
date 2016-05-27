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

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserCredentialWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserWrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@API(name = "UserManagement", version = "1.0.0", context = "/devicemgt_admin/users", tags = {"devicemgt_admin"})
@Path("/users")
@Api(value = "UserManagement", description = "User management related operations can be found here.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserManagementService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a user.",
            notes = "Adds a new user to EMM using this REST API.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Added the user successfully."),
            @ApiResponse(code = 500, message = "Exception in trying to add the user.")
    })
    @Permission(scope = "user-modify", permissions = {"/permission/admin/device-mgt/admin/user/add"})
    Response addUser(
            @ApiParam(name = "user", value = "User related details.",required = true) UserWrapper user);

    @GET
    @Path("/{username}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting details of a user.",
            notes = "If you wish to get the details of a specific user that is registered with EMM,"
                    + " you can do so using the REST API.",
            response = UserWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User information was retrieved successfully.",
                    response = UserWrapper.class),
            @ApiResponse(code = 404, message = "User by the provided username does not exist."),
            @ApiResponse(code = 500, message = "Exception in trying to retrieve user by username.")
    })
    @Permission(scope = "user-view", permissions = {"/permission/admin/device-mgt/admin/user/view"})
    Response getUser(
            @ApiParam(name = "username", value = "Username of the user to be fetched.",required = true)
            @PathParam("username") String username);

    @PUT
    @Path("/{username}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating details of a user",
            notes = "There will be situations where you will want to update the user details. In such "
                    + "situation you can update the user details using this REST API.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User was successfully updated"),
            @ApiResponse(code = 409, message = "User by the provided username doesn't exists. Therefore, "
                    + "request made to update user was refused."),
            @ApiResponse(code = 500, message = "Exception in trying to update user by username: 'username'")
    })
    @Permission(scope = "user-modify", permissions = {"/permission/admin/device-mgt/admin/user/update"})
    Response updateUser(
            @ApiParam(name = "username", value = "Username of the user to be updated.",required = true)
            @PathParam("username") String username,
            @ApiParam(name = "user", value = "User related details.",required = true)
                    UserWrapper user);

    @DELETE
    @Path("/{username}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Deleting a user.",
            notes = "In a situation where an employee leaves the organization you will need to remove the"
                    + " user details from EMM. In such situations you can use this REST API to remove a user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User was successfully removed."),
            @ApiResponse(code = 404, message = "User does not exist for removal."),
            @ApiResponse(code = 500, message = "Exception in trying to remove user.")
    })
    @Permission(scope = "user-modify", permissions = {"/permission/admin/device-mgt/admin/user/remove"})
    Response removeUser(
            @ApiParam(name = "username", value = "Username of the user to be deleted.",required = true)
            @PathParam("username") String username);

    @POST
    @Path("/{username}/roles")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the role details of a user.",
            notes = "A user can be assigned to one or more role in EMM. Using this REST API you are "
                    + "able to get the role/roles a user is assigned to.",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User roles obtained for the provided user.", response = String.class,
                    responseContainer = "List"),
            @ApiResponse(code = 404, message = "User does not exist for role retrieval."),
            @ApiResponse(code = 500, message = "Exception in trying to retrieve roles for the provided user.")
    })
    @Permission(scope = "user-view", permissions = {"/permission/admin/device-mgt/admin/user/view"})
    Response getRolesOfUser(
            @ApiParam(name = "username", value = "Username of the user.",required = true)
            @PathParam("username") String username);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting details of users",
            notes = "If you wish to get the details of all the user registered with EMM, you can do so "
                    + "using the REST API",
            response = UserWrapper.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All users were successfully retrieved.", response = UserWrapper.class,
                    responseContainer = "List"),
            @ApiResponse(code = 404, message = "No users found."),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of users.")
    })
    @Permission(scope = "user-view", permissions = {"/permission/admin/device-mgt/admin/user/list"})
    Response getUsers(
            @ApiParam(name = "filter", value = "Username of the user details to be fetched.",required = true)
            @QueryParam("filter") String filter,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many user details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Searching for a username.",
            notes = "If you are unsure of the "
                    + "user name of a user and need to retrieve the details of a specific user, you can "
                    + "search for that user by giving a character or a few characters in the username. "
                    + "You will be given a list of users having the user name with the exact order of the "
                    + "characters you provided.",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All users by username were successfully retrieved.",
                    response = String.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No users found."),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of users.")
    })
    @Permission(scope = "user-view", permissions = {"/permission/admin/device-mgt/admin/user/list"})
    Response getUserNames(
            @ApiParam(name = "filter", value = "Username/part of the user name to search.",required = true)
            @QueryParam("filter") String filter,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many user details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @PUT
    @Path("/{username}/credentials")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Changing the user password.",
            notes = "A user is able to change the password to secure their EMM profile via this REST API.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User password was successfully changed."),
            @ApiResponse(code = 400, message = "Old password does not match."),
            @ApiResponse(code = 500, message = "Could not change the password of the user. The Character encoding is" +
                    " not supported.")
    })
    @Permission(scope = "user-modify", permissions = {"/permission/admin/login"})
    Response resetPassword(
            @ApiParam(name = "username", value = "Username of the user.",required = true)
            @PathParam("username") String username,
            @ApiParam(name = "credentials", value = "Credential.",required = true)
            UserCredentialWrapper credentials);

}
