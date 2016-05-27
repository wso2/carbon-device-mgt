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
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * General Tenant Configuration REST-API.
 */
@API(name = "Configuration", version = "1.0.0", context = "/devicemgt_admin/configuration", tags = {"devicemgt_admin"})

@Path("/configuration")
@Api(value = "Configuration", description = "General Tenant Configuration management capabilities are exposed " +
        "through this API")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ConfigurationManagementService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring general platform settings",
            notes = "Configure the general platform settings using this REST API.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Tenant configuration saved successfully"),
            @ApiResponse(code = 500, message = "Error occurred while saving the tenant configuration")
    })
    @Permission(scope = "configuration-modify",
            permissions = {"/permission/admin/device-mgt/admin/platform-configs/modify"})
    Response saveConfiguration(@ApiParam(name = "configuration", value = "The required properties to "
            + "update the platform configurations.",
            required = true) PlatformConfiguration configuration);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting General Platform Configurations.",
            notes = "Get the general platform level configuration details using this REST API.",
            response = PlatformConfiguration.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tenant configuration saved successfully."),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the tenant configuration.")
    })
    @Permission(scope = "configuration-view",
            permissions = {"/permission/admin/device-mgt/admin/platform-configs/view"})
    Response getConfiguration();

    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating General Platform Configurations.",
            notes = "Update the notification frequency using this REST API.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Tenant configuration updated successfully."),
            @ApiResponse(code = 500, message = "Error occurred while updating the tenant configuration.")
    })
    @Permission(scope = "configuration-modify",
            permissions = {"/permission/admin/device-mgt/admin/platform-configs/modify"})
    Response updateConfiguration(@ApiParam(name = "configuration", value = "The required properties to "
            + "update the platform configurations.",
            required = true) PlatformConfiguration configuration);

}
