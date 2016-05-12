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
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Device related operations such as get all the available devices, etc.
 */

@Api(value = "Device", description = "Device related operations such as get all the available devices, etc.")
@SuppressWarnings("NonJaxWsWebServices")
public interface Device {

    /**
     * Get all devices. We have to use accept all the necessary query parameters sent by datatable.
     * Hence had to put lot of query params here.
     *
     * @return Device List
     */
    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list",
            notes = "Returns the set of devices that matches a given device type, user, role, "
            + "enrollment status, ownership type",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
            })
    Response getAllDevices(@ApiParam(name = "type", value = "Provide the device type, such as ios, android or"
                                + " windows", required = true) @QueryParam("type") String type,
                           @ApiParam(name = "user", value = "Get the details of the devices registered to a "
                                + "user by providing the user name", required = true) @QueryParam("user")
                                   String user,
                           @ApiParam(name = "role", value = "Get the details of the devices registered to a "
                                + "specific role by providing the role name", required = true)
                                @QueryParam("role") String role,
                           @ApiParam(name = "status", value = "Provide the device status details, such as "
                                + "active or inactive", required = true) @QueryParam("status")
                                EnrolmentInfo.Status status,
                           @ApiParam(name = "start", value = "Provide the starting pagination index",
                                required = true) @QueryParam("start") int startIdx,
                           @ApiParam(name = "length", value = "Provide how many device details you require "
                                + "from the starting pagination index", required = true)
                                @QueryParam("length") int length,
                           @ApiParam(name = "device-name", value = "Provide the name of a registered device "
                                + "and receive the specified device details", required = true)
                                @QueryParam("device-name") String deviceName,
                           @ApiParam(name = "ownership", value = "Provide the device ownership type and "
                                + "receive the specific device details", required = true)
                                @QueryParam("ownership") EnrolmentInfo.OwnerShip ownership);

    /**
     * Fetch device details for a given device type and device Id.
     *
     * @return Device wrapped inside Response
     */
    @GET
    @Path("view")
    @Produces({ MediaType.APPLICATION_JSON })
    Response getDevice(@QueryParam("type") String type, @QueryParam("id") String id);

    /**
     * Fetch device details of a given user.
     *
     * @param user         User Name
     * @return Device
     */
    @GET
    @Path("user/{user}")
    Response getDevice(@PathParam("user") String user);

    /**
     * Fetch device count of a given user.
     *
     * @param user User Name
     * @return Device
     */
    @GET
    @Path("user/{user}/count")
    Response getDeviceCount(@PathParam("user") String user);

    /**
     * Get current device count
     *
     * @return device count
     */
    @GET
    @Path("count")
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting the Device Count",
            notes = "Get the number of devices that are registered with WSO2 EMM.",
            response = Integer.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Device count"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device count")
            })
    Response getDeviceCount();

    /**
     * Get the list of devices that matches with the given name.
     *
     * @param deviceName   Device name
     * @param tenantDomain Callee tenant domain
     * @return list of devices.
     */
    @GET
    @Path("name/{name}/{tenantDomain}")
    @ApiOperation(
            httpMethod = "GET",
            value = "Get the device details of a specific device via the REST API",
            notes = "Get the device details of a specific device",
            response = DeviceType.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the devices list of device name")
            })
    Response getDevicesByName(@ApiParam(name = "name", value = "The name of the device or windows",
                                    required = true) @PathParam("name") String deviceName,
                              @ApiParam(name = "tenantDomain", value = "Tenant domain name. The default "
                                    + "tenant domain of WSO2 EMM is carbon.super", required = true)
                                    @PathParam("tenantDomain") String tenantDomain);

    /**
     * Get the list of available device types.
     *
     * @return list of device types.
     */
    @GET
    @Path("types")
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
    Response getDeviceTypes();

    /**
     * Update device.
     *
     * @return update status.
     */
    @PUT
    @Path("type/{type}/id/{deviceId}")
    Response updateDevice(@PathParam("type") String deviceType, @PathParam("deviceId") String deviceId,
                          org.wso2.carbon.device.mgt.common.Device updatedDevice);

    /**
     * disenroll device.
     *
     * @return disenrollment status.
     */
    @DELETE
    @Path("type/{type}/id/{deviceId}")
    Response disenrollDevice(@PathParam("type") String deviceType, @PathParam("deviceId") String deviceId);

}
