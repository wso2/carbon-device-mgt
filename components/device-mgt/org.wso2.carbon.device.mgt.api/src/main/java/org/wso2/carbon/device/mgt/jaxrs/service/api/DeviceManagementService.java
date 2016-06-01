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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.device.details.DeviceWrapper;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.policy.mgt.common.Policy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Device related REST-API. This can be used to manipulated device related details.
 */
@API(name = "Device", version = "1.0.0", context = "/api/device-mgt/admin/devices", tags = {"devicemgt_admin"})

@Path("/devices")
@Api(value = "Device Management", description = "This API carries all device management related operations " +
        "such as get all the available devices, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the list of devices enrolled with the system.",
            notes = "Returns all devices enrolled with the system.",
            response = Device.class,
            responseContainer = "List",
            tags = "Device Management")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of devices.",
                    response = Device.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n Server error occurred while fetching the device list.")
    })
    @Permission(scope = "device-list", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevices(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = false)
            @QueryParam("type") String type,
            @ApiParam(
                    name = "user", value = "Username of owner of the devices.",
                    required = false)
            @QueryParam("user") String user,
            @ApiParam(
                    name = "roleName",
                    value = "Role name of the devices to be fetched.",
                    required = false)
            @QueryParam("roleName") String roleName,
            @ApiParam(
                    name = "ownership",
                    allowableValues = "BYOD, COPE",
                    value = "Ownership of the devices to be fetched registered under.",
                    required = false)
            @QueryParam("ownership") String ownership,
            @ApiParam(
                    name = "status",
                    value = "Enrollment status of devices to be fetched.",
                    required = false)
            @QueryParam("status") String status,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Timestamp of the last modified date",
                    required = false)
            @HeaderParam("If-Modified-Since") String timestamp,
            @ApiParam(
                    name = "offset",
                    value = "Starting point within the complete list of items qualified.",
                    required = false)
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Maximum size of resource array to return.",
                    required = false)
            @QueryParam("limit") int limit);

    @GET
    @Path("{type}/{id}/info")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve devices information from the supplied device identifier.",
            notes = "This will return device information such as CPU usage, memory usage etc for supplied device " +
                    "identifier.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Information of the submitted list of devices is returned",
                            response = DeviceInfo.class,
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
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while retrieving information of the list of the devices submitted.",
                            response = ErrorResponse.class)
            })
    @Permission(scope = "device-info", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDeviceInfo(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("type") String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @POST
    @Path("/get-info")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Retrieve devices information from the supplied device identifies.",
            notes = "This will return device information such as CPU usage, memory usage etc for supplied device " +
                    "identifiers.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Information of the submitted list of devices is returned",
                            response = DeviceInfo.class,
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while retrieving information of the list of the devices submitted.")
            })
    @Permission(scope = "device-info", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDevicesInfo(
            @ApiParam(
                    name = "deviceIds",
                    value = "List of device identifiers",
                    required = true) List<DeviceIdentifier> deviceIds,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Timestamp of the last modified date",
                    required = false)
            @HeaderParam("If-Modified-Since") String timestamp);


    @GET
    @Path("/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get information of the requested device.",
            notes = "Returns information of the requested device.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched information of the device.",
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
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            })
    @Permission(scope = "device-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("type") String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @GET
    @Path("/{type}/{id}/location")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the device location of a given device and a device type.",
            notes = "This will return the device location including latitude and longitude as well the "
                    + "physical address.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "Successfully fetched the device location.",
                            response = DeviceLocation.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id."),
                    @ApiResponse(
                            code = 500,
                            message = "ErrorResponse occurred while getting the device location.",
                            response = ErrorResponse.class)
            })
    @Permission(scope = "device-info", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDeviceLocation(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("type") String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);


    @POST
    @Path("/locations")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Get the device location of a given devices and a device type.",
            notes = "This will return the device locations including latitude and longitude as well the "
                    + "physical address of the given devices.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "Successfully fetched the device location.",
                            response = DeviceLocation.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 404,
                            message = "Location details are not available for the given devices."),
                    @ApiResponse(
                            code = 500,
                            message = "ErrorResponse occurred while getting the device location.",
                            response = ErrorResponse.class)
            })
    @Permission(scope = "device-info", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response getDeviceLocations(
            @ApiParam(
                    name = "deviceIds",
                    value = "List of device identifiers",
                    required = true) List<DeviceIdentifier> deviceIds,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

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
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n List of features of the device is returned",
                            response = Feature.class,
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Device of which the feature list is requested, is not found."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while retrieving feature list of the device.",
                            response = ErrorResponse.class)
            })
    @Permission(scope = "device-search", permissions = {"/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getFeaturesOfDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("type") String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @POST
    @Path("/search-devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Advanced search for devices.",
            notes = "Carry out an advanced search of devices.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Device list searched for has successfully been retrieved. Location header " +
                                    "contains URL of newly enrolled device",
                            response = DeviceWrapper.class,
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while enrolling the device.",
                            response = ErrorResponse.class)
            })
    @Permission(scope = "device-search", permissions = {"/permission/admin/device-mgt/admin/devices/list"})
    Response searchDevices(
            @ApiParam(
                    name = "offset",
                    value = "Starting point within the complete list of items qualified.",
                    required = false)
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Maximum size of resource array to return.",
                    required = false)
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "searchContext",
                    value = "List of search conditions.",
                    required = true)
            SearchContext searchContext);

    @GET
    @Path("/{type}/{id}/applications")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting installed application details of a device.",
            notes = "Get the list of applications that a device has subscribed.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n List of applications installed into the device is returned",
                            response = Application.class,
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Device of which the application list is requested, is not found."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while retrieving installed application list of the device.",
                    response = ErrorResponse.class)
            })
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"
    })
    Response getInstalledApplications(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.", required = true)
            @PathParam("type") String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "Starting point within the complete list of items qualified.",
                    required = false)
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Maximum size of resource array to return.",
                    required = false)
            @QueryParam("limit") int limit);


    @GET
    @Path("/{type}/{id}/operations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting paginated details for operations on a device.",
            notes = "You will carry out many operations on a device. In a situation where you wish to view the all" +
                    " the operations carried out on a device it is not feasible to show all the details on one page" +
                    " therefore the details are paginated.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n List of operations scheduled for the device is returned",
                            response = Operation.class,
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Device of which the operation list is requested, is not found."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while retrieving operation list scheduled for the device.",
                            response = ErrorResponse.class)
            })
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"
    })
    Response getDeviceOperations(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("type") String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "Starting point within the complete list of items qualified.",
                    required = false)
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Maximum size of resource array to return.",
                    required = false)
            @QueryParam("limit") int limit);

    @GET
    @Path("/{type}/{id}/effective-policy")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the effective policy calculated for a device.",
            notes = "When a device registers with WSO2 EMM a policy is enforced on the device. Initially the EMM " +
                    "filters the policies based on the Platform (device type), filters based on the device ownership" +
                    " type , filters based on the user role or name and finally the policy is enforced on the device.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Effective policy calculated for the device is returned",
                            response = Policy.class,
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
                            code = 303,
                            message = "See Other. \n " +
                                    "Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Device of which the effective policy is requested, is not found."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server ErrorResponse. \n " +
                                    "Server error occurred while retrieving the effective policy calculated for the device.",
                            response = ErrorResponse.class)
            })
    Response getEffectivePolicyOfDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("type") String type,
            @ApiParam(
                    name = "id",
                    value = "Device Identifier",
                    required = true)
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);


}
