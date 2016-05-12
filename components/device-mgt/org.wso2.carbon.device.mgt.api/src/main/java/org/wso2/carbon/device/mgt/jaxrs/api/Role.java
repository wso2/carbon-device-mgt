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

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleWrapper;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;

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
 *
 */
@Api(value = "Role")
public interface Role {

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the List of Roles.",
            responseContainer = "List",
            notes = "If you wish to get the details of all the roles in WSO2 EMM, you can do so using this REST API.",
            response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "List of available roles"),
                            @ApiResponse(code = 500, message = "Error occurred while fetching the role list.") })
    Response getRoles();

    @GET
    @Path("{userStore}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the List of Roles in a User Store.",
            responseContainer = "List",
            notes = "If you wish to get the details of all the roles in WSO2 EMM, you can do so using this REST API.",
            response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "List of available roles"),
                            @ApiResponse(code = 500, message = "Error occurred while fetching the role list.") })
    Response getRoles(@ApiParam(name = "userStore", value = "Provide the name of the UserStore you wish to get the" +
                                                            " details from ",
                                 required = true) @PathParam("userStore") String userStore);

    @GET
    @Path("search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Searching for Roles via the Role Name.",
            responseContainer = "List",
            notes = "You will have many roles created within WSO2 EMM. As the admin you will need to confirm if a " +
                    "given role exists in the EMM. In such situation you can search for the role by giving a " +
                    "character or a few characters of the role name. The search will give you a list of roles that" +
                    " have the name in the exact order of the characters you provided.",
            response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "List of matching roles"),
                            @ApiResponse(code = 500, message = "Error occurred while fetching the matching role list" +
                                                               ".") })
    Response getMatchingRoles(@ApiParam(name = "filter", value = "Provide a character or a few characters in the" +
                                                                 " role name.",
                                        required = true) @QueryParam("filter") String filter);

    @GET
    @Path("permissions")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Permission Details of a Role.",
            notes = "In an organization an individual is associated a with set of responsibilities based on their " +
                    "role. In WSO2 EMM you are able to configure permissions based on the responsibilities carried " +
                    "out by a role. Therefore if you wish to retrieve the permission details of a role, you can do " +
                    "so using this REST API.",
            response = UIPermissionNode.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Permission details of a role"),
                            @ApiResponse(code = 500, message = "Error occurred while fetching the permission " +
                                                               "details of a role.") })
    Response getPermissions(@ApiParam(name = "rolename", value = "Provide the name of the role you wish to get the " +
                                                                  "permission details.",
                                       required = true) @QueryParam("rolename") String roleName);

    @GET
    @Path("role")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Role.",
            notes = "If you wish to get the details of a role in WSO2 EMM, you can do so using this REST API.",
            response = RoleWrapper.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Details of a role."),
                            @ApiResponse(code = 500, message = "Error occurred while retrieving the user role.") })
    Response getRole(@ApiParam(name = "rolename", value = "Provide the name of the role you wish to get the " +
                                                          "details.",
                               required = true) @QueryParam("rolename") String roleName);

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Role.",
            notes = "You are able to add a new role to WSO2 EMM using the REST API.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Added the role."),
                            @ApiResponse(code = 500, message = "Error occurred while adding the user role.") })
    Response addRole(@ApiParam(name = "roleWrapper", value = "Role and permission details.",
                               required = true) RoleWrapper roleWrapper);

    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating a Role.",
            notes = "There will be situations where you will need to update the role details, such as the permissions" +
                    " or the role name. In such situation you can update the role details.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Updated the role."),
                            @ApiResponse(code = 500, message = "Error occurred while updating the user role details" +
                                                               ".") })
    Response updateRole(@ApiParam(name = "rolename", value = "Provide the name of the role you wish to update.",
                                  required = true) @QueryParam("rolename") String roleName,
                        @ApiParam(name = "roleWrapper", value = "Role and permission details.",
                                  required = true) RoleWrapper roleWrapper);

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Deleting a Role.",
            notes = "In a situation when your Organization identifies that a specific role is no longer required you " +
                    "will need to remove the role details from WSO2 EMM.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Deleted the role."),
                            @ApiResponse(code = 500, message = "Error occurred while deleting the user role details" +
                                                               ".") })
    Response deleteRole(@ApiParam(name = "rolename", value = "Provide the name of the role you wish to delete.",
                                  required = true) @QueryParam("rolename") String roleName);

    @PUT
    @Path("users")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Adding Users to a Role.",
            notes = "Defining the users to a role at the point of creating a new role is optional, " +
                    "therefore you are able to update the users that belong to a given role after you have created " +
                    "a role using this REST API." +
                    "Example: Your Organization hires 30 new engineers. Updating the role details for each user can " +
                    "be cumbersome, therefore you can define all the new employees that belong to the engineering " +
                    "role using this API.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Added Users to a Role."),
                            @ApiResponse(code = 500, message = "Error occurred while saving the users of the role.") })
    Response updateUsers(@ApiParam(name = "rolename", value = "Provide the name of the role you wish to update.",
                                   required = true) @QueryParam("rolename") String roleName,
                         @ApiParam(name = "userList", value = "Provide the names of the users you will to update.",
                                   required = true) List<String> userList);

    @GET
    @Path("count")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Role Count.",
            response = Integer.class,
            notes = "Get the number of roles in WSO2 EMM.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Retrieved the role count."),
                            @ApiResponse(code = 500, message = "Error occurred while retrieving the role count.") })
    Response getRoleCount();
}
