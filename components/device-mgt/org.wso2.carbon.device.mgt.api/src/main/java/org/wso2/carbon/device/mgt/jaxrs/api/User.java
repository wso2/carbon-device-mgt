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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.axis2.databinding.types.soapencoding.Integer;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;
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
 * This represents the JAX-RS services of User related functionality.
 */
@Api(value = "User")
public interface User {

    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
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
    Response addUser(@ApiParam(name = "userWrapper", value = "Includes the required properties to add a user"
                            + " as the <JSON_PAYLOAD> value", required = true) UserWrapper userWrapper);

    @GET
    @Path("view")
    @Produces({MediaType.APPLICATION_JSON})
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
    Response getUser(@ApiParam(name = "username", value = "Provide the name of the user you wish to get the"
                            + " details of as the value", required = true)
                            @QueryParam("username") String username);

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
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
    Response updateUser(@ApiParam(name = "userWrapper", value = "Provide the name of the user you wish to get"
                                + " the details of as the value", required = true) UserWrapper userWrapper,
                        @ApiParam(name = "username", value = "Provide the name of the user you wish to get "
                                + "the details of as the value", required = true)
                                @QueryParam("username") String username);

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Deleting a User",
            notes = "In a situation where an employee leaves the organization you will need to remove the"
                    + " user details from WSO2 EMM. In such situations you can use this REST API "
                    + "to remove a user",
            response = ResponsePayload.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User by username: 'username' was successfully removed"),
            @ApiResponse(code = 400, message = "User by username: 'username' does not exist for removal"),
            @ApiResponse(code = 500, message = "Exception in trying to remove user by username: 'username'")
            })
    Response removeUser(@ApiParam(name = "username", value = "Provide the name of the user you wish to delete"
                                + " as the value for {username}", required = true)
                                @QueryParam("username") String username);

    @GET
    @Path("roles")
    @Produces({MediaType.APPLICATION_JSON})
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
    Response getRoles(@ApiParam(name = "username", value = "Provide the user name of the user you wish to get"
                            + " the role details", required = true) @QueryParam("username") String username);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Users",
            notes = "If you wish to get the details of all the user registered with WSO2 EMM, you can do so "
                    + "using the REST API",
            response = ResponsePayload.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "All users were successfully retrieved"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of users")
            })
    Response getAllUsers();

    @GET
    @Path("{filter}")
    @Produces({MediaType.APPLICATION_JSON})
    Response getMatchingUsers(@PathParam("filter") String filter);

    @GET
    @Path("view-users")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting User Details by Searching via the User Name",
            notes = "You will have 100+ users registered with WSO2 EMM. If you wish to retrieve the user "
                    + "details of a specific user, and you only remember part of the user's username, "
                    + "you are able to retrieve the user details by giving a character or a few characters "
                    + "in the username",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All users by username were successfully retrieved. Obtained"
                    + " user count: 'count'"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of users")
            })
    Response getAllUsersByUsername(@ApiParam(name = "username", value = "Provide any user detail of the user"
                                        + " as the value for {username} to retrieve the user details, such "
                                        + "as email address, first name or last name", required = true)
                                        @QueryParam("username") String userName);

    @GET
    @Path("users-by-username")
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
    Response getAllUserNamesByUsername(@ApiParam(name = "username", value = "Provide a character or a few "
                                            + "character in the user name as the value for {username}",
                                            required = true) @QueryParam("username") String userName);

    @POST
    @Path("email-invitation")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Sending Enrollment Invitations to Users",
            notes = "Send the users a mail inviting them to download the EMM mobile application on their "
                    + "devices using this REST API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Email invitation was successfully sent to user"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of users")
            })
    Response inviteExistingUsersToEnrollDevice(@ApiParam(name = "usernames", value = "List of the users to be"
                                                    + " invited as the <JSON_PAYLOAD>", required = true)
                                                    List<String> usernames);

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Device Details of a User",
            notes = "If you wish to get the details of the devices enrolled by a specific user, you can do "
                    + "so using this REST API",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Device management error")
            })
    Response getAllDeviceOfUser(@ApiParam(name = "username", value = "Provide the name of the user you wish "
                                    + "to get the details", required = true) @QueryParam("username")
                                    String username,
                                @ApiParam(name = "start", value = "Provide the starting pagination index",
                                    required = true) @QueryParam("start") int startIdx,
                                @ApiParam(name = "length", value = "Provide how many device details you "
                                    + "require from the starting pagination index", required = true)
                                    @QueryParam("length") int length);

    @GET
    @Path("count")
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting the User Count",
            notes = "Get the number of users in WSO2 EMM",
            response = Integer.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of users that exist"
                    + " within the current tenant")
            })
    Response getUserCount();

    @PUT
    @Path("{roleName}/users")
    @Produces({MediaType.APPLICATION_JSON})
    Response updateRoles(@PathParam("username") String username, List<String> userList);

    @POST
    @Path("change-password")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Changing the User Password",
            notes = "A user is able to change the password to secure their EMM profile via this REST API",
            response = UserCredentialWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "UserImpl password by username: 'Username' was "
                    + "successfully changed"),
            @ApiResponse(code = 400, message = "Old password does not match"),
            @ApiResponse(code = 400, message = "Could not change the password of the user: 'Username'. The"
                    + " Character Encoding is not supported"),
            @ApiResponse(code = 500, message = "Internal Server Error")
            })
    Response resetPassword(@ApiParam(name = "credentials", value = "Include the required properties to change"
                                + " the user password as <JSON_PAYLOAD> value", required = true)
                                UserCredentialWrapper credentials);

    @POST
    @Path("reset-password")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Resetting the User Password",
            notes = "In a situation where you need to block a user from accessing their EMM profile, "
                    + "the EMM administrator is able to reset the password. This will change the user's "
                    + "password and the user will not be able to able to login to the account as he/she is "
                    + "not aware of the new password.",
            response = UserCredentialWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "UserImpl password by username: 'Username' was "
                    + "successfully changed"),
            @ApiResponse(code = 400, message = "Old password does not match"),
            @ApiResponse(code = 400, message = "Could not change the password of the user: 'Username'. The"
                    + " Character Encoding is not supported"),
            @ApiResponse(code = 500, message = "Internal Server Error")
            })
    Response resetPasswordByAdmin(@ApiParam(name = "credentials", value = "Include the required properties "
                                        + "to change a user password as <JSON_PAYLOAD> value",
                                        required = true) UserCredentialWrapper credentials);
}
