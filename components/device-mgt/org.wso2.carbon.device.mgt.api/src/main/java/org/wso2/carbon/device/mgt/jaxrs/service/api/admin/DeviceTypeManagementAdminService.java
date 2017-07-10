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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceTypeList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceTypeManagementAdminService"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/admin/device-types"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Path("/admin/device-types")
@Api(value = "Device Type Management Administrative Service", description = "This an  API intended to be used by " +
        "'internal' components to log in as an admin user and do a selected number of operations. " +
        "Further, this is strictly restricted to admin users only ")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(
        scopes = {
                @Scope(
                        name = "Getting Details of a Device",
                        description = "Getting Details of a Device",
                        key = "perm:admin:device-type",
                        permissions = {"/device-mgt/admin/device-type"}
                )
        }
)
public interface DeviceTypeManagementAdminService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Supported Device Type with Meta Definition",
            notes = "Get the list of device types supported by WSO2 IoT.",
            tags = "Device Type Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of supported device types.",
                            response = DeviceTypeList.class,
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
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                            "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message =
                                    "Not Modified. \n Empty body because the client already has the latest version " +
                                            "of the requested resource.\n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the " +
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            }
    )
    Response getDeviceTypes();

    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a Device Type",
            notes = "Add the details of a device type.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully added the device type.",
                         responseHeaders = {
                                 @ResponseHeader(
                                         name = "Content-Type",
                                         description = "The content type of the body")
                         }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response addDeviceType(@ApiParam(
            name = "type",
            value = "The device type such as ios, android, windows or fire-alarm.",
            required = true)DeviceType deviceType);

    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Device Type",
            notes = "Update the details of a device type.",
            response = DeviceType.class,
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully updated the device type.",
                         responseHeaders = {
                                 @ResponseHeader(
                                         name = "Content-Type",
                                         description = "The content type of the body")
                         }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response updateDeviceType(@ApiParam(
            name = "type",
            value = "The device type such as ios, android, windows or fire-alarm.",
            required = true) DeviceType deviceType);

}
