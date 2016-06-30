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
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleList;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleWrapper;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@API(name = "Role", version = "1.0.0", context = "/devicemgt_admin/roles", tags = {"devicemgt_admin"})

@Path("/roles")
@Api(value = "Role Management", description = "Role management related operations can be found here.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RoleManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the list of roles.",
            notes = "If you wish to get the details of all the roles in EMM, you can do so using this REST "
                    + "API. All internal roles, roles created for Service-providers and application related "
                    + "roles are omitted.",
            tags = "Role Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the requested list of roles.",
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
                                            description = "Date and time the resource has been modified the"
                                                    + " last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the latest "
                                    + "version of the requested resource."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n Server error occurred while fetching requested list of roles.",
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "roles-view",
            permissions = {
            "/permission/admin/device-mgt/admin/roles/list",
            "/permission/admin/device-mgt/admin/users/view",
            "/permission/admin/device-mgt/admin/policies/add",
            "/permission/admin/device-mgt/admin/policies/update"}
    )
    Response getRoles(
            @ApiParam(
                    name = "filter",
                    value = "Role name or a part of it to search.",
                    required = false)
            @QueryParam("filter")
                    String filter,
            @ApiParam(
                    name = "user-store",
                    value = "From which user store the roles must be fetched.",
                    required = false)
            @QueryParam("user-store")
                    String userStoreName,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "Starting point within the complete list of items qualified.",
                    required = false)
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Maximum size of resource array to return.",
                    required = false)
            @QueryParam("limit")
                    int limit);

    @GET
    @Path("/{roleName}/permissions")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting permission details of a role.",
            notes = "In an organization an individual is associated a with set of responsibilities based "
                    + "on their role. In  EMM you are able to configure permissions based on the "
                    + "responsibilities carried out by a role. Therefore if you wish to retrieve the "
                    + "permission details of a role, you can do " +
                    "so using this REST API.",
            response = UIPermissionNode.class,
            responseContainer = "List",
            tags = "Role Management"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the permission list of the given role.",
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
                                            description = "Date and time the resource has been modified the "
                                                    + "last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the latest "
                                    + "version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n Server error occurred while fetching"
                                    + " the permission list of the requested role.",
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "roles-view",
            permissions = {"/permission/admin/device-mgt/admin/roles/list"}
    )
    Response getPermissionsOfRole(
            @ApiParam(
                    name = "roleName",
                    value = "Name of the role.",
                    required = true)
            @PathParam("roleName")
                    String roleName,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);

    @GET
    @Path("/{roleName}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get details of a role.",
            notes = "If you wish to get the details of a role in  EMM, you can do so using this REST API.",
            response = RoleWrapper.class,
            tags = "Role Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the requested role.",
                            response = RoleWrapper.class,
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
                                            description = "Date and time the resource has been modified the "
                                                    + "last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the "
                                    + "latest version of" +
                                    " the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n Server error occurred while fetching the " +
                                    "requested role.",
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "roles-view",
            permissions = {"/permission/admin/device-mgt/admin/roles/list"}
    )
    Response getRole(
            @ApiParam(
                    name = "roleName",
                    value = "Name of the role.",
                    required = true)
            @PathParam("roleName")
                    String roleName,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a role.",
            notes = "You are able to add a new role to EMM using the REST API.",
            tags = "Role Management")
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Role has successfully been created",
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
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")
                    }
            ),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")
                    }
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not "
                            + "supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n " +
                            "Server error occurred while adding a new role.",
                    response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "roles-modify",
            permissions = {"/permission/admin/device-mgt/admin/roles/add"}
    )
    Response addRole(
            @ApiParam(
                    name = "role",
                    value = "Details about the role to be added.",
                    required = true)
                    RoleWrapper role);

    @PUT
    @Path("/{roleName}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update a role.",
            notes = "There will be situations where you will need to update the role details, such as the permissions" +
                    " or the role name. In such situation you can update the role details.",
            tags = "Role Management")
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Role has been updated successfully",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the updated role."),
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
                                            "Used by caches, or in conditional requests.")
                    }
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be deleted does not exist."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n " +
                            "Server error occurred while updating the role.",
                    response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "roles-modify",
            permissions = {"/permission/admin/device-mgt/admin/roles/update"}
    )
    Response updateRole(
            @ApiParam(
                    name = "roleName",
                    value = "Name of the role.",
                    required = true)
            @PathParam("roleName")
                    String roleName,
            @ApiParam(
                    name = "role",
                    value = "Details about the role to be added.",
                    required = true)
                    RoleWrapper role);

    @DELETE
    @Path("/{roleName}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Delete a role.",
            notes = "In a situation when your Organization identifies that a specific role is no longer required you " +
                    "will need to remove the role details from  EMM.",
            tags = "Role Management")
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Role has successfully been removed"),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be deleted does not exist."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n " +
                            "Server error occurred while removing the role.",
                    response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "roles-modify",
            permissions = {"/permission/admin/device-mgt/admin/roles/remove"}
    )
    Response deleteRole(
            @ApiParam(
                    name = "roleName",
                    value = "Name of the role to de deleted.",
                    required = true)
            @PathParam("roleName")
                    String roleName);

    @PUT
    @Path("/{roleName}/users")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Add users to a role.",
            notes = "Defining the users to a role at the point of creating a new role is optional, " +
                    "therefore you are able to update the users that belong to a given role after you have created " +
                    "a role using this REST API." +
                    "Example: Your Organization hires 30 new engineers. Updating the role details for each user can " +
                    "be cumbersome, therefore you can define all the new employees that belong to the engineering " +
                    "role using this API.",
            tags = "Role Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n User list of the role has been updated successfully",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "URL of the updated user list."),
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
                                                    "Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource to be deleted does not exist."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while updating the user list of the role.",
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "roles-modify",
            permissions = {"/permission/admin/device-mgt/admin/roles/update"}
    )
    Response updateUsersOfRole(
            @ApiParam(
                    name = "roleName",
                    value = "Name of the role.",
                    required = true)
            @PathParam("roleName")
                    String roleName,
            @ApiParam(
                    name = "users",
                    value = "List of usernames to be added.",
                    required = true)
                    List<String> users);

}
