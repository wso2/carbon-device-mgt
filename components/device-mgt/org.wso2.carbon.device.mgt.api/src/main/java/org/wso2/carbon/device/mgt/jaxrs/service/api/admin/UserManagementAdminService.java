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
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.PasswordResetWrapper;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "UserManagementAdmin"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/admin/users"),
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
                        name = "View Users",
                        description = "View Users",
                        key = "perm:admin-users:view",
                        permissions = {"/device-mgt/users/manage"}
                ),
                @Scope(
                        name = "Delete Users Device Information",
                        description = "Delete users device details",
                        key = "perm:admin-users:remove",
                        permissions = {"/device-mgt/users/manage"}
                )
        }
)
@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "User Management Administrative Service", description = "This an  API intended to be used by " +
        "'internal' components to log in as an admin user and do a selected number of operations. " +
        "Further, this is strictly restricted to admin users only ")
public interface UserManagementAdminService {

    @POST
    @Path("/{username}/credentials")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Changing the User Password.",
            notes = "The EMM administrator is able to change the password of the users in " +
                    "the system and block them from logging into their EMM profile using this REST API.",
            tags = "User Management Administrative Service",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin-users:view")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the credentials of the user."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The resource to be deleted does not exist."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating credentials of the user.",
                    response = ErrorResponse.class)
    })
    Response resetUserPassword(
            @ApiParam(
                    name = "username",
                    value = "The username of the user." +
                            "INFO: Add a new user using the POST /users API that is under User Management.",
                    required = true)
            @PathParam("username")
            @Size(max = 45)
            String username,
            @ApiParam(
                name = "domain",
                value = "The domain name of the user store.",
                required = false)
            @QueryParam("domain") String domain,
            @ApiParam(
                    name = "credentials",
                    value = "Credential.",
                    required = true) PasswordResetWrapper credentials);



    @DELETE
    @Path("/{username}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Delete a users associated devices.",
            notes = "If you wish to remove an device details to comply with the privacy requirements, can be done with " +
                    "this resource.",
            tags = "Device details remove",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin-users:remove")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Users devices and details has been deleted successfully.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body."),
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while removing the group.",
                    response = ErrorResponse.class)
    })
    Response deleteDeviceOfUser(@ApiParam(
            name = "username",
            value = "Username of the users devices to be deleted.",
            required = true)
                         @PathParam("username") String username);



    //device remove request would looks like follows
    //DELETE devices/type/virtual_firealarm/id/us06ww93auzp
    @DELETE
    @Path("/type/{device-type}/id/{device-id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Remove the information of device specified by device id",
            notes = "Returns the status of the deleted device information.",
            tags = "Device details remove privacy compliance",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin-users:remove")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the device information.",
                            response = Device.class,
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
                            message = "Not Modified. Empty body because the client already has the latest " +
                                    "version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            })
    Response deleteDevice(
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("device-type")
            @Size(max = 45)
            String deviceType,
            @ApiParam(
                    name = "device-id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("device-id")
            @Size(max = 45)
            String deviceId);


}
