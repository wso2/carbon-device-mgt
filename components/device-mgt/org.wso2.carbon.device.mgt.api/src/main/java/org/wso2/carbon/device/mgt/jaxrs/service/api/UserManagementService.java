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

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.device.mgt.jaxrs.beans.BasicUserInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.BasicUserInfoList;
import org.wso2.carbon.device.mgt.jaxrs.beans.EnrollmentInvitation;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.OldPasswordResetWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleList;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserInfo;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "UserManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/users"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Adding a User",
                        description = "Adding a User",
                        key = "perm:users:add",
                        permissions = {"/device-mgt/users/manage"}
                ),
                @Scope(
                        name = "Getting Details of a User",
                        description = "Getting Details of a User",
                        key = "perm:users:details",
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Updating Details of a User",
                        description = "Updating Details of a User",
                        key = "perm:users:update",
                        permissions = {"/device-mgt/users/manage"}
                ),
                @Scope(
                        name = "Deleting a User",
                        description = "Deleting a User",
                        key = "perm:users:delete",
                        permissions = {"/device-mgt/users/manage"}
                ),
                @Scope(
                        name = "Getting the Role Details of a User",
                        description = "Getting the Role Details of a User",
                        key = "perm:users:roles",
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Getting Details of Users",
                        description = "Getting Details of Users",
                        key = "perm:users:user-details",
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Getting the User Count",
                        description = "Getting the User Count",
                        key = "perm:users:count",
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Getting the User existence status",
                        description = "Getting the User existence status",
                        key = "perm:users:is-exist",
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Searching for a User Name",
                        description = "Searching for a User Name",
                        key = "perm:users:search",
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Changing the User Password",
                        description = "Adding a User",
                        key = "perm:users:credentials",
                        permissions = {"/login"}
                ),
                @Scope(
                        name = "Sending Enrollment Invitations to Users",
                        description = "Sending Enrollment Invitations to Users",
                        key = "perm:users:send-invitation",
                        permissions = {"/device-mgt/users/manage"}
                )
        }
)
@Path("/users")
@Api(value = "User Management", description = "User management related operations can be found here.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserManagementService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a User",
            notes = "WSO2 IoTS supports user management. Add a new user to the WSO2 IoTS user management system via this REST API",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:add")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Successfully created the user.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the role added."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n User already exists.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not " +
                                    "supported format.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while adding a new user.",
                            response = ErrorResponse.class)
            })
    Response addUser(
            @ApiParam(
                    name = "user",
                    value = "Provide the property details to add a new user.\n" +
                            "Double click the example value and click try out. ",
                    required = true) UserInfo user);

    @GET
    @Path("/{username}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a User",
            notes = "Get the details of a user registered with WSO2 IoTS using the REST API.",
            response = BasicUserInfo.class,
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:details")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the details of the specified user.",
                    response = BasicUserInfo.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n Server error occurred while" +
                            " fetching the ruser details.",
                    response = ErrorResponse.class)
    })
    Response getUser(
            @ApiParam(
                    name = "username",
                    value = "Provide the username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username,
            @ApiParam(
                    name = "domain",
                    value = "The domain name of the user store.",
                    required = false)
            @QueryParam("domain") String domain,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @PUT
    @Path("/{username}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Details of a User",
            notes = "There will be situations where you will want to update the user details. In such "
                    + "situation you can update the user details using this REST API.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:update")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the details of the specified user.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating the user.",
                    response = ErrorResponse.class)
    })
    Response updateUser(
            @ApiParam(
                    name = "username",
                    value = "The username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username,
            @ApiParam(
                    name = "domain",
                    value = "The domain name of the user store.",
                    required = false)
            @QueryParam("domain") String domain,
            @ApiParam(
                    name = "userData",
                    value = "Update the user details.\n" +
                            "NOTE: Do not change the admin username, password and roles when trying out this API.",
                    required = true) UserInfo userData);

    @DELETE
    @Path("/{username}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Deleting a User",
            notes = "When an employee leaves the organization, you can remove the user details from WSO2 IoTS using this REST API.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:delete")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully removed the user from WSO2 IoTS."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while removing the user.",
                    response = ErrorResponse.class
            )
    })
    Response removeUser(
            @ApiParam(
                    name = "username",
                    value = "Username of the user to be deleted.\n" +
                            "INFO: If you want to try out this API, make sure to create a new user and then remove that user. Do not remove the admin user.",
                    required = true,
                    defaultValue = "[Create a new user named Jim, and then try out this API.]")
            @PathParam("username") String username,
            @ApiParam(
                    name = "domain",
                    value = "The domain name of the user store.",
                    required = false)
            @QueryParam("domain") String domain);

    @GET
    @Path("/{username}/roles")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Role Details of a User",
            notes = "A user can be assigned to one or more role in IoTS. Using this REST API you can get the role/roles a user is assigned to.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:roles")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of roles the specified user is assigned to.",
                    response = RoleList.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the list of roles" +
                            " assigned to the specified user.",
                    response = ErrorResponse.class)
    })
    Response getRolesOfUser(
            @ApiParam(
                    name = "username",
                    value = "The username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username,
            @ApiParam(
                    name = "domain",
                    value = "The domain name of the user store.",
                    required = false)
            @QueryParam("domain") String domain);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Users",
            notes = "You are able to manage users in WSO2 IoTS by adding, updating and removing users. If you wish to get the list of users registered with WSO2 IoTS, you can do so "
                    + "using this REST API",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:user-details")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of users registered with WSO2 IoTS.",
                    response = BasicUserInfoList.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the list of WSO2 IoTS users.",
                    response = ErrorResponse.class)
    })
    Response getUsers(
            @ApiParam(
                    name = "filter",
                    value = "The username of the user.",
                    required = false)
            @QueryParam("filter") String filter,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time\n." +
                            "Provide the value in the Java Date Format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String timestamp,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many user details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit);

    @GET
    @Path("/count")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the User Count",
            notes = "Get the number of users in WSO2 IoTS via this REST API.",
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:count")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the user count.",
                    response = BasicUserInfoList.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the total number of users in WSO2 IoTS.",
                    response = ErrorResponse.class)
    })
    Response getUserCount();

    @GET
    @Path("/checkUser")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the User existence status",
            notes = "Check if the user exists in the user store.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:is-exist")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched user exist status.",
                    response = BasicUserInfoList.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the " +
                            "total user exist status.",
                    response = ErrorResponse.class)
    })
    Response isUserExists(@ApiParam(
                    name = "username",
                    value = "The username of the user.",
                    required = true)
            @QueryParam("username") String userName);

    @GET
    @Path("/search/usernames")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Searching for a User Name",
            notes = "If you are unsure of the user name of a user and need to retrieve the details of a specific user, you can "
                    + "search for that user by giving a character or a few characters in the username. "
                    + "You will be given a list of users having the user name in the exact order of the "
                    + "characters you provided.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:search")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of users that matched the given filter.",
                    response = String.class,
                    responseContainer = "List",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the list of users that matched the given filter.",
                    response = ErrorResponse.class)
    })
    Response getUserNames(
            @ApiParam(
                    name = "filter",
                    value = "Provide a character or a few character in the user name",
                    required = true)
            @QueryParam("filter") String filter,
            @ApiParam(
                    name = "domain",
                    value = "The user store domain which the user names should be fetched from",
                    required = false)
            @QueryParam("domain") String domain,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time\n." +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z\n. " +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String timestamp,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many user details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit);

    @PUT
    @Path("/credentials")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Changing the User Password",
            notes = "A user is able to change the password to secure their WSO2 IoTS profile via this REST API.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:credentials")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the user credentials."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating the user credentials.",
                    response = ErrorResponse.class)
    })
    Response resetPassword(
            @ApiParam(
                    name = "credentials",
                    value = "The property to change the password.\n" +
                            "The password should be within 5 to 30 characters",
                    required = true) OldPasswordResetWrapper credentials);

    @POST
    @Path("/send-invitation")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Sending Enrollment Invitations to Users",
            notes = "Send the users a mail inviting them to enroll their devices using the REST API given below.\n" +
                    "Before running the REST API command to send the enrollment invitations to users make sure to configure WSO2 IoTS as explained in step 4, under the WSO2 IoTS general server configurations documentation.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:send-invitation")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully sent the invitation mail."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating the user credentials.",
                    response = ErrorResponse.class)
    })
    Response inviteExistingUsersToEnrollDevice(
            @ApiParam(
                    name = "users",
                    value = "List of users",
                    required = true) List<String> usernames);

    @POST
    @Path("/enrollment-invite")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Sending Enrollment Invitations to email address",
            notes = "Send the a mail inviting recipients to enroll devices.",
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:send-invitation")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully sent the invitation mail."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                              "Server error occurred while updating the user credentials.",
                    response = ErrorResponse.class)
    })
    Response inviteToEnrollDevice(
            @ApiParam(
                    name = "enrollmentInvitation",
                    value = "List of email address of recipients",
                    required = true)
            @Valid EnrollmentInvitation enrollmentInvitation);
}
