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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.device.details.DeviceWrapper;
import org.wso2.carbon.device.mgt.common.search.SearchContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Device related REST-API. This can be used to manipulated device related details.
 */
@API(name = "Device", version = "1.0.0", context = "/devicemgt_admin/devices", tags = {"devicemgt_admin"})
@Path("/devices")
@Api(value = "Device", description = "Device related operations such as get all the available devices, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementService {

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the device list.",
            notes = "Returns all devices enrolled with the system.",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of devices.", response = org.wso2.carbon
                    .device.mgt.common.Device.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No device has currently been under the provided type."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list.")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevices(
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many device details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    Response getDevices(@HeaderParam("If-Modified-Since") Date timestamp, @QueryParam("offset") int offset,
                        @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the device list corresponding to a device type.",
            notes = "Returns all devices enrolled with the system under the provided type.",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of devices.", response = org.wso2.carbon
                    .device.mgt.common.Device.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No device has currently been under the provided type."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list.")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevices(
            @ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
            @QueryParam ("type") String type,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many device details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Get devices information from the supplied device identifies.",
            notes = "This will return device information such as CPU usage, memory usage etc for supplied device " +
                    "identifiers.",
            response = DeviceInfo.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched device information.", response = DeviceInfo.class,
                    responseContainer = "List"),
            @ApiResponse(code = 404, message = "No device information is available for the device list submitted."),
            @ApiResponse(code = 500, message = "Error occurred while getting the device information.")
    })
    @Permission(scope = "device-info", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevicesInfo(
            @ApiParam(name = "deviceIds", value = "List of device identifiers",
            required = true) List<DeviceIdentifier> deviceIds);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the device list of a user.",
            notes = "Returns the set of devices that matches a given username.",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of devices.", response = org.wso2.carbon
                    .device.mgt.common.Device.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No device has currently been enrolled by the user."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list.")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDeviceByUsername(
            @ApiParam(name = "user", value = "Username of owner of the devices.", required = true)
            @QueryParam("user") String user,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many device details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list in a role.",
            notes = "Returns the set of devices that matches a given role.",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of devices.", response = org.wso2.carbon
                    .device.mgt.common.Device.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No device has currently been enrolled under the role."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list.")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevicesByRole(
            @ApiParam(name = "roleName", value = "Role name of the devices to be fetched.", required = true)
            @QueryParam("roleName") String roleName,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many device details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list of an ownership scheme.",
            notes = "Returns the set of devices that matches a given ownership scheme.",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of devices.", response = org.wso2.carbon
                    .device.mgt.common.Device.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No device has currently been enrolled under the given ownership " +
                    "scheme."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list.")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevicesByOwnership(
            @ApiParam(name = "ownership", value = "Ownership of the devices to be fetched registered under.",
                    required = true) EnrolmentInfo.OwnerShip ownership,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many device details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list",
            notes = "Returns the set of devices that matches a given enrollment status",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of devices.",
                    response = org.wso2.carbon.device.mgt.common.Device.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No device is currently in the given enrollment status."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list.")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevicesByEnrollmentStatus(
            @ApiParam(name = "status", value = "Enrollment status of devices to be fetched.", required = true)
                    EnrolmentInfo.Status status,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many device details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @Permission(scope = "device-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getDevice(@QueryParam("type") String type, @QueryParam("id") String id);

    @GET
    @Path("/{type}/{id}/location")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the device location of a given device and a device type.",
            notes = "This will return the device location including latitude and longitude as well the "
                    + "physical address.",
            response = DeviceLocation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the device location.",
                    response = DeviceLocation.class),
            @ApiResponse(code = 404, message = "Location details are not available for the given device."),
            @ApiResponse(code = 500, message = "Error occurred while getting the device location.")
    })
    @Permission(scope = "device-info", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDeviceLocation(
            @ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
            @PathParam("type") String type,
            @ApiParam(name = "id", value = "The device identifier of the device.", required = true)
            @PathParam("id") String id);

    @GET
    @Path("/{type}/{id}/features")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get Feature Details of a Device",
            notes = "WSO2 EMM features enable you to carry out many operations on a given device platform. " +
                    "Using this REST API you can get the features that can be carried out on a preferred device type," +
                    " such as iOS, Android or Windows.",
            response = org.wso2.carbon.device.mgt.common.Feature.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the features.",
                    response = org.wso2.carbon.device.mgt.common.Feature.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of features.")
    })
    @Permission(scope = "device-search", permissions = {"/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getFeaturesOfDevice(
            @ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
            @PathParam("type") String type,
            @ApiParam(name = "id", value = "The device identifier of the device.", required = true)
            @PathParam("id") String id);

    @POST
    @Path("/search-devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Advanced search for devices.",
            notes = "Carry out an advanced search of devices.",
            response = DeviceWrapper.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched matching devices.", response = DeviceWrapper.class,
                    responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error occurred while searching the device information.")
    })
    @Permission(scope = "device-search", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response searchDevices(
            @ApiParam(name = "searchContext", value = "List of search conditions.",
            required = true) SearchContext searchContext);

    @GET
    @Path("/{type}/{id}/applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting installed application details of a device.",
            responseContainer = "List",
            notes = "Get the list of applications that a device has subscribed.",
            response = Application.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of installed application details of a device.",
            response = Application.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No installed applications found on the device searched."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the apps of the device.")
    })
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"
    })
    Response getInstalledApplications(
            @ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
            @PathParam("type") String type,
            @ApiParam(name = "id", value = "The device identifier of the device.", required = true)
            @PathParam("id") String id);


    @GET
    @Path("/{type}/{id}/operations")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting paginated details for operations on a device.",
            notes = "You will carry out many operations on a device. In a situation where you wish to view the all" +
                    " the operations carried out on a device it is not feasible to show all the details on one page" +
                    " therefore the details are paginated.",
            response = org.wso2.carbon.device.mgt.common.operation.mgt.Operation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Operations on a device.",
                    response = org.wso2.carbon.device.mgt.common.operation.mgt.Operation.class,
                    responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the operations for the " +
                    "device.")
    })
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"
    })
    Response getDeviceOperations(
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many device details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit,
            @ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
            @PathParam("type") String type,
            @ApiParam(name = "id", value = "The device identifier of the device.", required = true)
            @PathParam("id") String id);

}
