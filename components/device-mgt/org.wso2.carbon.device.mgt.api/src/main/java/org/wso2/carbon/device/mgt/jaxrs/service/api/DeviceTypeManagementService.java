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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/device-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceTypeManagementService {

    @GET
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting Details of the Devices Supported via WSO2 EMM",
            notes = "You are able to register Android, iOS and Windows devices with WSO2 EMM. This API will "
                    + "retrieve the device type details that can register with the EMM",
            response = DeviceType.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of devices based on the type"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the list of device types") })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getTypes();

    @POST
    @Path("/{type}/configuration")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "POST",
            value = "Configuring general platform settings",
            notes = "Configure the general platform settings using this REST API")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Tenant configuration saved successfully"),
            @ApiResponse(code = 500, message = "Error occurred while saving the tenant configuration")
    })
    @Permission(scope = "configuration-modify", permissions = {"/permission/admin/device-mgt/admin/platform-configs/modify"})
    Response saveConfiguration(@PathParam("type") String type, PlatformConfiguration config);

    @GET
    @Path("/{type}/configuration")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting General Platform Configurations",
            notes = "Get the general platform level configuration details using this REST API",
            response = PlatformConfiguration.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the tenant configuration")
    })
    @Permission(scope = "configuration-view", permissions = {"/permission/admin/device-mgt/admin/platform-configs/view"})
    Response getConfiguration(@PathParam("type") String type);

    @PUT
    @Path("/{type}/configuration")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "PUT",
            value = "Updating General Platform Configurations",
            notes = "Update the notification frequency using this REST API")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Tenant configuration updated successfully"),
            @ApiResponse(code = 500, message = "Error occurred while updating the tenant configuration")
    })
    @Permission(scope = "configuration-modify", permissions = {"/permission/admin/device-mgt/admin/platform-configs/modify"})
    Response updateConfiguration(@PathParam("type") String type, PlatformConfiguration config);

}
