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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleList;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "RoleManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/roles"),
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
                        name = "Getting the List of Roles",
                        description = "Getting the List of Roles",
                        key = "perm:roles:view",
                        permissions = {"/device-mgt/roles/view"}
                ),
                @Scope(
                        name = "Getting Permission Details of a Role",
                        description = "Getting Permission Details of a Role",
                        key = "perm:roles:permissions",
                        permissions = {"/device-mgt/roles/view"}
                ),
                @Scope(
                        name = "Getting the List of Roles",
                        description = "Getting the List of Roles",
                        key = "perm:roles:details",
                        permissions = {"/device-mgt/roles/view"}
                ),
                @Scope(
                        name = "Adding a Role",
                        description = "Adding a Role",
                        key = "perm:roles:add",
                        permissions = {"/device-mgt/roles/manage"}
                ),
                @Scope(
                        name = "Adding a combined Role",
                        description = "Adding a combined Role",
                        key = "perm:roles:create-combined-role",
                        permissions = {"/device-mgt/roles/manage"}
                ),
                @Scope(
                        name = "Updating Role Details",
                        description = "Updating Role Details",
                        key = "perm:roles:update",
                        permissions = {"/device-mgt/roles/manage"}
                ),
                @Scope(
                        name = "Deleting a Role",
                        description = "Deleting a Role",
                        key = "perm:roles:delete",
                        permissions = {"/device-mgt/roles/manage"}
                ),
                @Scope(
                        name = "Adding Users to a Role",
                        description = "Adding Users to a Role",
                        key = "perm:roles:add-users",
                        permissions = {"/device-mgt/roles/manage"}
                )
        }
)
@Path("/roles")
@Api(value = "Role Management", description = "Role management related operations can be found here.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RoleManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the List of Roles",
            notes = "WSO2 EMM supports role-based access control (RBAC) and role management. Using this API you can the list of roles that are in WSO2 EMM.\n" +
                    "Note: Internal roles, roles created for service-providers, and application related roles will not be given in the output.",
            tags = "Role Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:roles:view")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of roles in WSO2 EMM.",
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching list of roles.",
                            response = ErrorResponse.class)
            })
    Response getRoles(
            @ApiParam(
                    name = "filter",
                    value = "Provide a character or a few characters in the role name.",
                    required = false)
            @QueryParam("filter") String filter,
            @ApiParam(
                    name = "user-store",
                    value = "The name of the UserStore you wish to get the list of roles.",
                    required = false)
            @QueryParam("user-store") String userStoreName,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time." +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many role details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit);

    @GET
    @Path("/{roleName}/permissions")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Permission Details of a Role",
            notes = "An individual is associated a with set of responsibilities based on their " +
                    "role. In  WSO2 EMM you are able to configure permissions based on the responsibilities carried " +
                    "out by various roles. Therefore, if you wish to retrieve the permission details of a role, you can do " +
                    "so using this REST API.",
            response = UIPermissionNode.class,
            responseContainer = "List",
            tags = "Role Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:roles:permissions")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the permissions details for the specified role.",
                            response = UIPermissionNode.class,
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource.\n"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified role does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n Server error occurred while fetching the permission list for the requested role.",
                            response = ErrorResponse.class)
            })
    Response getPermissionsOfRole(
            @ApiParam(
                    name = "roleName",
                    value = "The name of the role.",
                    required = true,
                    defaultValue = "Engineer")
            @PathParam("roleName") String roleName,
            @ApiParam(
                    name = "user-store",
                    value = "The name of the user store from which you wish to get the permission of role.",
                    required = false)
            @QueryParam("user-store") String userStoreName,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time." +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @GET
    @Path("/{roleName}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Role",
            notes = "Get the permissions associated with a role and role specific details using this REST API.",
            response = RoleInfo.class,
            tags = "Role Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:roles:details")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the details of the role.",
                            response = RoleInfo.class,
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified role does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the details of" +
                                    "requested role.",
                            response = ErrorResponse.class)
    })
    Response getRole(
            @ApiParam(
                    name = "roleName",
                    value = "The name of the role.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("roleName") String roleName,
            @ApiParam(
                    name = "user-store",
                    value = "The name of the user store which the particular of role resides in",
                    required = false)
            @QueryParam("user-store") String userStoreName,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time." +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Role",
            notes = "WSO2 EMM supports role-based access control (RBAC) and role management. Add a new role to WSO2 EMM using this REST API.",
            tags = "Role Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:roles:add")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully created the role.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The URL to the newly added role."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding a new role.",
                    response = ErrorResponse.class)
    })
    Response addRole(
            @ApiParam(
                    name = "role",
                    value = "The properties required to add a new role.",
                    required = true) RoleInfo role);

    @POST
    @Path("/create-combined-role/{roleName}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a combined Role",
            notes = "WSO2 EMM supports role-based access control (RBAC) and role management. Add a new combined role to WSO2 EMM using this REST API.",
            tags = "Role Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:roles:create-combined-role")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully created the role.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The URL to the newly added role."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding a new role.",
                    response = ErrorResponse.class)
    })
    Response addCombinedRole(
            @ApiParam(
                    name = "roles",
                    value = "List of roles names required to add a new combined role.",
                    required = true) List<String> roles,
            @PathParam("roleName") String roleName,
            @QueryParam("user-store") String userStoreName);

    @PUT
    @Path("/{roleName}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Role Details",
            notes = "There will be situations where you need to update the role details, such as the permissions" +
                    " or the role name. Update the role details using this REST API.",
            tags = "Role Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:roles:update")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the specified role.",
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
                    message = "Not Found. \n The specified role does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while updating the role.",
                    response = ErrorResponse.class)
    })
    Response updateRole(
            @ApiParam(
                    name = "roleName",
                    value = "The name of the role.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("roleName") String roleName,
            @ApiParam(
                    name = "role",
                    value = "The properties required to update a role.\n" +
                            "NOTE: Don't change the role and the permissions of the admin user. " +
                            "If you want to try out this API by updating all the properties, create a new role and update the properties accordingly.",
                    required = true) RoleInfo role,
            @ApiParam(
                    name = "user-store",
                    value = "The name of the user store which the particular role resides in.",
                    required = false)
            @QueryParam("user-store") String userStoreName);

    @DELETE
    @Path("/{roleName}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Deleting a Role",
            notes = "Roles become obsolete over time due to various reasons. In a situation where your Organization identifies that a specific role is no longer required, you " +
                    "can delete a role using this REST API.",
            tags = "Role Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:roles:delete")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully removed the specified role."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified role does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while removing the role.",
                    response = ErrorResponse.class)
    })
    Response deleteRole(
            @ApiParam(
                    name = "roleName",
                    value = "The name of the role that needs to de deleted.\n" +
                            "NOTE: Don't delete the admin role",
                    required = true)
            @PathParam("roleName") String roleName,
            @ApiParam(
                    name = "user-store",
                    value = "The name of the user store which the particular role resides in.",
                    required = false)
            @QueryParam("user-store") String userStoreName);

    @PUT
    @Path("/{roleName}/users")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Adding Users to a Role",
            notes = "Defining users to a role at the point of creating a new role is optional. " +
                    "You can update the users that belong to a given role after you have created " +
                    "a role using this REST API.\n" +
                    "Example: Your Organization hires 30 new engineers. Updating the role details for each user can " +
                    "be cumbersome. Therefore, you can define all the new employees that belong to the engineering " +
                    "role using this API.",
            tags = "Role Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:roles:add-users")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully added the users to the specified role.",
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified role does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported.\n" +
                                    "supported format.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while adding the user to the specified role.",
                            response = ErrorResponse.class)
    })
    Response updateUsersOfRole(
            @ApiParam(
                    name = "roleName",
                    value = "The name of the role.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("roleName") String roleName,
            @ApiParam(
                    name = "user-store",
                    value = "The name of the user store which the particular role resides in.",
                    required = false)
            @QueryParam("user-store") String userStoreName,
            @ApiParam(
                    name = "users",
                    value = "Define the users that belong to the role.\n" +
                            "Multiple users can be added to a role by using comma separated values. ",
                    required = true,
                    defaultValue = "[admin]"
            ) List<String> users);

}
