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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserCredentialWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserWrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserManagementService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a User via the REST API",
            notes = "Adds a new user to WSO2 EMM using this REST API")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Exception in trying to add user by username: 'username'")
    })
    @Permission(scope = "user-modify", permissions = {"/permission/admin/device-mgt/admin/user/add"})
    Response addUser(UserWrapper user);

    @GET
    @Path("/{username}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a User",
            notes = "If you wish to get the details of a specific user that is registered with WSO2 EMM,"
                    + " you can do so using the REST API",
            response = UserWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "User information was retrieved successfully"),
            @ApiResponse(code = 400, message = "User by username: 'username' does not exist"),
            @ApiResponse(code = 500, message = "Exception in trying to retrieve user by username: 'username'")
    })
    @Permission(scope = "user-view", permissions = {"/permission/admin/device-mgt/admin/user/view"})
    Response getUser(@PathParam("username") String username);

    @PUT
    @Path("/{username}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "PUT",
            value = "Updating Details of a User",
            notes = "There will be situations where you will want to update the user details. In such "
                    + "situation you can update the user details using this REST API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User by username: 'username' was successfully updated"),
            @ApiResponse(code = 409, message = "User by username: 'username' doesn't exists. Therefore, "
                    + "request made to update user was refused"),
            @ApiResponse(code = 500, message = "Exception in trying to update user by username: 'username'")
    })
    @Permission(scope = "user-modify", permissions = {"/permission/admin/device-mgt/admin/user/update"})
    Response updateUser(@PathParam("username") String username, UserWrapper user);

    @DELETE
    @Path("/{username}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Deleting a User",
            notes = "In a situation where an employee leaves the organization you will need to remove the"
                    + " user details from WSO2 EMM. In such situations you can use this REST API "
                    + "to remove a user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User by username: 'username' was successfully removed"),
            @ApiResponse(code = 400, message = "User by username: 'username' does not exist for removal"),
            @ApiResponse(code = 500, message = "Exception in trying to remove user by username: 'username'")
    })
    @Permission(scope = "user-modify", permissions = {"/permission/admin/device-mgt/admin/user/remove"})
    Response removeUser(@PathParam("username") String username);

    @POST
    @Path("/{username}/roles")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Role Details of a User",
            notes = "A user can be assigned to one or more role in WSO2 EMM. Using this REST API you are "
                    + "able to get the role/roles a user is assigned to",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User roles obtained for user : 'username'"),
            @ApiResponse(code = 400, message = "User by username: 'username' does not exist for role retrieval"),
            @ApiResponse(code = 500, message = "Exception in trying to retrieve roles for user by username: 'username'")
    })
    @Permission(scope = "user-view", permissions = {"/permission/admin/device-mgt/admin/user/view"})
    Response getRolesOfUser(@PathParam("username") String username);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Users",
            notes = "If you wish to get the details of all the user registered with WSO2 EMM, you can do so "
                    + "using the REST API",
            response = UserWrapper.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All users were successfully retrieved"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of users")
    })
    @Permission(scope = "user-view", permissions = {"/permission/admin/device-mgt/admin/user/list"})
    Response getUsers(@QueryParam("filter") String filter, @QueryParam("offset") int offset,
                      @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Searching for a User Name",
            notes = "You will have 100+ users registered with WSO2 EMM. Therefore if you are unsure of the "
                    + "user name of a user and need to retrieve the details of a specific user, you can "
                    + "search for that user by giving a character or a few characters in the username. "
                    + "You will be given a list of users having the user name with the exact order of the "
                    + "characters you provided",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All users by username were successfully retrieved. Obtained"
                    + " user count: 'count'"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of users")
    })
    @Permission(scope = "user-view", permissions = {"/permission/admin/device-mgt/admin/user/list"})
    Response getUserNames(@QueryParam("filter") String filter, @QueryParam("offset") int offset,
                      @QueryParam("limit") int limit);

    @PUT
    @Path("/{username}/credentials")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Changing the User Password",
            notes = "A user is able to change the password to secure their EMM profile via this REST API")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "UserImpl password by username: 'Username' was "
                    + "successfully changed"),
            @ApiResponse(code = 400, message = "Old password does not match"),
            @ApiResponse(code = 400, message = "Could not change the password of the user: 'Username'. The"
                    + " Character Encoding is not supported"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @Permission(scope = "user-modify", permissions = {"/permission/admin/login"})
    Response resetPassword(@PathParam("username") String username, UserCredentialWrapper credentials);

}
