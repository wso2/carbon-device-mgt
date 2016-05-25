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
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.joda.time.DateTime;
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

@Path("/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list",
            notes = "Returns all devices enrolled with the system",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevices(@QueryParam("offset") int offset, @QueryParam("limit") int limit);

    Response getDevices(@HeaderParam("If-Modified-Since") Date timestamp, @QueryParam("offset") int offset,
                        @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list",
            notes = "Returns all devices enrolled with the system",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevices(@QueryParam("type") String type, @QueryParam("offset") int offset,
                        @QueryParam("limit") int limit);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Get devices information from the supplied device identifies",
            notes = "This will return device information such as CPU usage, memory usage etc for supplied device " +
                    "identifiers.",
            response = DeviceInfo.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 400, message = ""),
            @ApiResponse(code = 400, message = ""),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @Permission(scope = "device-info", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevices(List<DeviceIdentifier> deviceIds);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list",
            notes = "Returns the set of devices that matches a given username",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDeviceByUsername(@QueryParam("user") String user, @QueryParam("offset") int offset,
                                 @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list",
            notes = "Returns the set of devices that matches a given role",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevicesByRole(@QueryParam("roleName") String roleName, @QueryParam("offset") int offset,
                              @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list",
            notes = "Returns the set of devices that matches a given ownership scheme",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevicesByOwnership(@QueryParam("ownership") EnrolmentInfo.OwnerShip ownership,
                                   @QueryParam("offset") int offset, @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Returns device list",
            notes = "Returns the set of devices that matches a given enrollment status",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevicesByEnrollmentStatus(@QueryParam("status") EnrolmentInfo.Status status,
                                          @QueryParam("offset") int offset, @QueryParam("limit") int limit);

    @GET
    @Permission(scope = "device-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getDevice(@QueryParam("type") String type, @QueryParam("id") String id);

    @GET
    @Path("/{type}/{id}/location")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the device location",
            notes = "This will return the device location including latitude and longitude as well the "
                    + "physical address",
            response = DeviceLocation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 400, message = ""),
            @ApiResponse(code = 400, message = ""),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @Permission(scope = "device-info", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDeviceLocation(@PathParam("type") String type, @PathParam("id") String id);

    @GET
    @Path("/{type}/{id}/features")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Get Feature Details of a Device",
            notes = "WSO2 EMM features enable you to carry out many operations on a given device platform. " +
                    "Using this REST API you can get the features that can be carried out on a preferred device type," +
                    " such as iOS, Android or Windows.",
            response = org.wso2.carbon.device.mgt.common.Feature.class,
            responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "List of Features"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of features" +
                    ".") })
    @Permission(scope = "device-search", permissions = {"/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getFeaturesOfDevice(@PathParam("type") String type, @PathParam("id") String id);

    @POST
    @Path("/search-devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Advanced Search for Devices via the Console",
            notes = "Carry out an advanced search via the EMM console",
            response = DeviceWrapper.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = DeviceWrapper.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error occurred while searching the device information")
    })
    @Permission(scope = "device-search", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response searchDevices(@ApiParam(name = "filtering rules", value = "List of search conditions",
            required = true) SearchContext searchContext);

    @GET
    @Path("/{type}/{id}/applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting Installed Application Details of a Device.",
            responseContainer = "List",
            notes = "Get the list of applications that a device has subscribed.",
            response = Application.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "List of installed application details of a device.", response = Application.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the apps of the device" +
                    ".")})
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getInstalledApplications(
            @ApiParam(name = "type", value = "Define the device type as the value for {type}. " +
            "Example: ios, android or windows.", required = true)
            @PathParam("type") String type, @ApiParam(name = "id", value = "Define the device ID",
            required = true) @PathParam("id") String id);


    @GET
    @Path("/{type}/{id}/operations")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting Paginated Details for Operations on a Device.",
            notes = "You will carry out many operations on a device. In a situation where you wish to view the all" +
                    " the operations carried out on a device it is not feasible to show all the details on one page" +
                    " therefore the details are paginated." +
                    " Example: You carry out 21 operations via a given device. When you wish to see the operations " +
                    "carried out, the details of the 21 operations will be broken down into 3 pages with 10 operation" +
                    " details per page.",
            response = org.wso2.carbon.device.mgt.common.operation.mgt.Operation.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "List of Operations on a device."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the operations for the " +
                    "device.")})
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getDeviceOperations(@QueryParam("offset") int offset, @QueryParam("limit") int limit,
                                 @PathParam("type") String type, @PathParam("id") String id);

}
