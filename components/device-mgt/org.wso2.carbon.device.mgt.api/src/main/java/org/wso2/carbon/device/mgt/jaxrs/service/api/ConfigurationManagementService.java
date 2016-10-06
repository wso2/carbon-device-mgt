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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * General Tenant Configuration REST-API.
 */
@API(name = "Configuration Management", version = "1.0.0", context = "/api/device-mgt/v1.0/configuration", tags = {"devicemgt_admin"})

@Path("/configuration")
@Api(value = "Configuration Management", description = "The general platform configuration management capabilities are exposed " +
        "through this API.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ConfigurationManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting General Platform Configurations",
            notes = "WSO2 EMM monitors policies to verify that the devices comply with the policies enforced on them. " +
                    "General platform configurations include the settings on how often the the device need to be monitored. " +
                    "Using this REST API you can get the general platform level configurations.",
            tags = "Configuration Management")
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the general platform configurations.",
                    response = PlatformConfiguration.class,
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
                    }
            ),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the general " +
                            "platform configurations.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "View Configurations", permission = "/device-mgt/platform-configurations/view")
    Response getConfiguration(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z." +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);

    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating General Platform Configurations",
            notes = "WSO2 EMM monitors policies to verify that the devices comply with the policies enforced on them." +
                    "General platform configurations include the settings on how often the the device need to be monitored." +
                    "Using this REST API you can update the general platform level configurations.",
            tags = "Configuration Management")
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the general platform configurations.",
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
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while modifying the general platform configurations.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "Manage configurations", permission = "/device-mgt/platform-configurations/manage")
    Response updateConfiguration(
            @ApiParam(
                    name = "configuration",
                    value = "The properties required to update the platform configurations.",
                    required = true)
                    PlatformConfiguration configuration);

}
