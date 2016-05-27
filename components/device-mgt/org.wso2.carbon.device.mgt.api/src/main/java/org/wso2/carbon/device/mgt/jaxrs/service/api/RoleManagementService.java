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
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleWrapper;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@API(name = "Role", version = "1.0.0", context = "/devicemgt_admin/roles", tags = {"devicemgt_admin"})
@Path("/roles")
@Api(value = "Role", description = "Role management related operations can be found here.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RoleManagementService {

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the list of roles.",
            responseContainer = "List",
            notes = "If you wish to get the details of all the roles in EMM, you can do so using this REST API. All " +
                    "internal roles, roles created for Service-providers and application related roles are omitted.",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of available roles",
                    response = String.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No roles found."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the role list.")
    })
    @Permission(scope = "roles-view", permissions = {
            "/permission/admin/device-mgt/admin/roles/list",
            "/permission/admin/device-mgt/admin/users/view",
            "/permission/admin/device-mgt/admin/policies/add",
            "/permission/admin/device-mgt/admin/policies/update"})
    Response getRoles(
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many role details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the list of roles in a user store.",
            responseContainer = "List",
            notes = "If you wish to get the details of all the roles in  EMM, you can do so using this REST API.",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of available roles",
                    response = String.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No roles found."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the role list.")
    })
    @Permission(scope = "roles-view", permissions = {
            "/permission/admin/device-mgt/admin/users/add",
            "/permission/admin/device-mgt/admin/roles/list"})
    Response getRoles(
            @ApiParam(name = "user-store", value = "From which user store the roles must be fetched.",required = true)
            @QueryParam("user-store") String userStoreName,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many role details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Searching for roles via the role name.",
            responseContainer = "List",
            notes = "You will have many roles created within EMM. As the admin you will need to confirm if a " +
                    "given role exists in the EMM. In such situation you can search for the role by giving a " +
                    "character or a few characters of the role name. The search will give you a list of roles that" +
                    " have the name in the exact order of the characters you provided.",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of matching roles.",
            response = String.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No roles found."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the matching role list.")
    })
    @Permission(scope = "roles-view", permissions = {
            "/permission/admin/device-mgt/admin/users/add",
            "/permission/admin/device-mgt/admin/roles/list"})
    Response searchRoles(
            @ApiParam(name = "filter", value = "Role name or a part of it to search.",required = true)
            @QueryParam("filter") String filter,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many role details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @Path("/{roleName}/permissions")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting permission details of a role.",
            notes = "In an organization an individual is associated a with set of responsibilities based on their " +
                    "role. In  EMM you are able to configure permissions based on the responsibilities carried " +
                    "out by a role. Therefore if you wish to retrieve the permission details of a role, you can do " +
                    "so using this REST API.",
            response = UIPermissionNode.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Permission details of a role", response = UIPermissionNode.class),
            @ApiResponse(code = 404, message = "No permissions found for the role."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the permission details of a role.")
    })
    @Permission(scope = "roles-view", permissions = {"/permission/admin/device-mgt/admin/roles/list"})
    Response getPermissionsOfRole(
            @ApiParam(name = "roleName", value = "Name of the role.",required = true)
            @PathParam("roleName") String roleName);

    @GET
    @Path("/{roleName}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting details of a role.",
            notes = "If you wish to get the details of a role in  EMM, you can do so using this REST API.",
            response = RoleWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the role details.", response = RoleWrapper.class),
            @ApiResponse(code = 404, message = "No role details found for the provided role name."),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the role details.")
    })
    @Permission(scope = "roles-view", permissions = {"/permission/admin/device-mgt/admin/roles/list"})
    Response getRole(
            @ApiParam(name = "roleName", value = "Name of the role.",required = true)
            @PathParam("roleName") String roleName);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a role.",
            notes = "You are able to add a new role to EMM using the REST API.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Added the role."),
            @ApiResponse(code = 500, message = "Error occurred while adding the user role.")
    })
    @Permission(scope = "roles-modify", permissions = {"/permission/admin/device-mgt/admin/roles/add"})
    Response addRole(
            @ApiParam(name = "role", value = "Details about the role to be added.",required = true)
                    RoleWrapper role);

    @PUT
    @Path("/{roleName}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating a role.",
            notes = "There will be situations where you will need to update the role details, such as the permissions" +
                    " or the role name. In such situation you can update the role details.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Updated the role."),
            @ApiResponse(code = 500, message = "Error occurred while updating the user role details.")
    })
    @Permission(scope = "roles-modify", permissions = {"/permission/admin/device-mgt/admin/roles/update"})
    Response updateRole(
            @ApiParam(name = "roleName", value = "Name of the role.",required = true)
            @PathParam("roleName") String roleName,
            @ApiParam(name = "role", value = "Details about the role to be added.",required = true)
                    RoleWrapper role);

    @DELETE
    @Path("/{roleName}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Deleting a role.",
            notes = "In a situation when your Organization identifies that a specific role is no longer required you " +
                    "will need to remove the role details from  EMM.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deleted the role."),
            @ApiResponse(code = 500, message = "Error occurred while deleting the user role details.")
    })
    @Permission(scope = "roles-modify", permissions = {"/permission/admin/device-mgt/admin/roles/remove"})
    Response deleteRole(
            @ApiParam(name = "roleName", value = "Name of the role to de deleted.",required = true)
            @PathParam("roleName") String roleName);

    @POST
    @Path("/{roleName}/users")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Adding users to a role.",
            notes = "Defining the users to a role at the point of creating a new role is optional, " +
                    "therefore you are able to update the users that belong to a given role after you have created " +
                    "a role using this REST API." +
                    "Example: Your Organization hires 30 new engineers. Updating the role details for each user can " +
                    "be cumbersome, therefore you can define all the new employees that belong to the engineering " +
                    "role using this API.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Added Users to a Role."),
            @ApiResponse(code = 500, message = "Error occurred while saving the users of the role.")
    })
    @Permission(scope = "roles-modify", permissions = {"/permission/admin/device-mgt/admin/roles/update"})
    Response updateUsersOfRole(
            @ApiParam(name = "roleName", value = "Name of the role.",required = true)
            @PathParam("roleName") String roleName,
            @ApiParam(name = "users", value = "List of usernames to be added.",required = true)
            List<String> users);

}
