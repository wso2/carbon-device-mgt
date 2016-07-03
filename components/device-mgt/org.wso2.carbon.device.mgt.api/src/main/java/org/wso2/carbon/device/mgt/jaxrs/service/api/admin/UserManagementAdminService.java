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
package org.wso2.carbon.device.mgt.jaxrs.service.api.admin;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.PasswordResetWrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "User Management Admin", version = "1.0.0", context = "/api-device-mgt-v1.0/admin/users", tags = {"devicemgt_admin"})

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "User Management Administrative Service", description = "This an  API intended to be used by " +
        "'internal' components to log in as an admin user and do a selected number of operations. " +
        "Further, this is strictly restricted to admin users only ")
public interface UserManagementAdminService {

    @PUT
    @Path("/{username}/credentials")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Change the user password.",
            notes = "A user is able to change the password to secure their EMM profile via this REST API.",
            tags = "User Management Administrative Service")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Credentials of the user have been updated successfully"),
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
                            "Server error occurred while updating credentials of the user.",
                    response = ErrorResponse.class)
    })
    @Permission(scope = "user-modify", roles = {"emm-admin"})
    Response resetUserPassword(
            @ApiParam(
                    name = "username",
                    value = "Username of the user.",
                    required = true)
            @PathParam("username") String username,
            @ApiParam(
                    name = "credentials",
                    value = "Credential.",
                    required = true) PasswordResetWrapper credentials);

}
