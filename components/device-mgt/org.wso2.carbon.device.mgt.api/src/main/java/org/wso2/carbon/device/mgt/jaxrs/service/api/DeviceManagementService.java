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

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Device related REST-API. This can be used to manipulated device related details.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/devices"),
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
                        name = "Getting Details of Registered Devices",
                        description = "Getting Details of Registered Devices",
                        key = "perm:devices:view",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Details of a Device",
                        description = "Getting Details of a Device",
                        key = "perm:devices:details",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Update the device specified by device id",
                        description = "Update the device specified by device id",
                        key = "perm:devices:update",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Delete the device specified by device id",
                        description = "Delete the device specified by device id",
                        key = "perm:devices:delete",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Feature Details of a Device",
                        description = "Getting Feature Details of a Device",
                        key = "perm:devices:features",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Advanced Search for Devices",
                        description = "Advanced Search for Devices",
                        key = "perm:devices:search",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Installed Application Details of a Device",
                        description = "Getting Installed Application Details of a Device",
                        key = "perm:devices:applications",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Device Operation Details",
                        description = "Getting Device Operation Details",
                        key = "perm:devices:operations",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Get the details of the policy that is enforced on a device.",
                        description = "Get the details of the policy that is enforced on a device.",
                        key = "perm:devices:effective-policy",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Policy Compliance Details of a Device",
                        description = "Getting Policy Compliance Details of a Device",
                        key = "perm:devices:compliance-data",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                )
        }
)
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
            value = "Getting Details of Registered Devices",
            notes = "Provides details of all the devices enrolled with WSO2 EMM.",
            tags = "Device Management",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:view")
            })
    }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of devices.",
                    response = DeviceList.class,
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 400,
                    message = "The incoming request has more than one selection criteria defined via the query parameters.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "The search criteria did not match any device registered with the server.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response getDevices(
            @ApiParam(
                    name = "name",
                    value = "The device name, such as shamu, bullhead or angler Nexus device names. ",
                    required = false)
            @Size(max = 45)
            String name,
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = false)
            @QueryParam("type")
            @Size(max = 45)
            String type,
            @ApiParam(
                    name = "user",
                    value = "The username of the owner of the device.",
                    required = false)
            @QueryParam("user")
                    String user,
            @ApiParam(
                    name = "role",
                    value = "A role of device owners. Ex : store-admin",
                    required = false)
            @QueryParam("role")
            @Size(max = 45)
                    String role,
            @ApiParam(
                    name = "ownership",
                    allowableValues = "BYOD, COPE",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n" +
                            "- BYOD: Bring Your Own Device\n" +
                            "- COPE: Corporate-Owned, Personally-Enabled",
                    required = false)
            @QueryParam("ownership")
            @Size(max = 45)
            String ownership,
            @ApiParam(
                    name = "status",
                    value = "Provide the device status details, such as active or inactive.",
                    required = false)
            @QueryParam("status")
            @Size(max = 45)
            String status,
            @ApiParam(
                    name = "groupId",
                    value = "Id of the group which device belongs",
                    required = false)
            @QueryParam("groupId")
                    int groupId,
            @ApiParam(
                    name = "since",
                    value = "Checks if the requested variant was created since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @QueryParam("since")
            String since,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
            String timestamp,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
            int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
            int limit);


    @GET
    @Path("/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Device",
            notes = "Get the details of a device by specifying the device type and device identifier.",
            tags = "Device Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:details")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the details of the device.",
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest version" +
                                    " of the requested resource.\n"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n A device with the specified device type and id was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response getDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true,
                    allowableValues = "android, ios, windows")
            @PathParam("type")
            @Size(max = 45)
            String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
            String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. \n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
            String ifModifiedSince);

    //device rename request would looks like follows
    //POST devices/type/virtual_firealarm/id/us06ww93auzp/rename
    @POST
    @Path("/type/{device-type}/id/{device-id}/rename")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Update the device specified by device id",
            notes = "Returns the status of the updated device operation.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:update")
                    })
            }
    )
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
    Response renameDevice(
            @ApiParam(
                    name = "device",
                    value = "The payload containing new name for device with updated name.",
                    required = true)
                    Device device,
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

    //device remove request would looks like follows
    //DELETE devices/type/virtual_firealarm/id/us06ww93auzp
    @DELETE
    @Path("/type/{device-type}/id/{device-id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Remove the device specified by device id",
            notes = "Returns the status of the deleted device operation.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:delete")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the device.",
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

    @GET
    @Path("/{type}/{id}/features")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Feature Details of a Device",
            notes = "WSO2 EMM features enable you to carry out many operations based on the device platform. " +
                    "Using this REST API you can get the features that can be carried out on a preferred device type," +
                    " such as iOS, Android or Windows.",
            tags = "Device Management",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:features")
            })
    }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of features.",
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "The source can be retrieved from the URL specified in the location header.",
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
                            message = "Not Found. \n The specified device can not be found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the feature list for the device platform.",
                            response = ErrorResponse.class)
            })
    Response getFeaturesOfDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true,
                    allowableValues = "android, ios, windows")
            @PathParam("type")
            @Size(max = 45)
            String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.\n" +
                            "INFO: Make sure to add the ID of a device that is already registered with WSO2 EMM.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
            String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. \n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
            String ifModifiedSince);

    @POST
    @Path("/search-devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Advanced Search for Devices",
            notes = "Search for devices by filtering the search result through the specified search terms.",
            tags = "Device Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:search")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the device information.",
                            response = DeviceList.class,
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
                                            description = "Date and time the resource was last modified. \n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource.\n"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Acceptable.\n The existing device did not match the values specified in the device search.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while getting the device details.",
                            response = ErrorResponse.class)
            })
    Response searchDevices(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
            int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many activity details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
            int limit,
            @ApiParam(
                    name = "searchContext",
                    value = "The properties to advanced search devices.",
                    required = true)
            SearchContext searchContext);

    @GET
    @Path("/{type}/{id}/applications")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Installed Application Details of a Device",
            notes = "Get the list of applications subscribed to by a device.",
            tags = "Device Management",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:applications")
            })

    }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of applications.",
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
                                            description = "Date and time the resource was last modified\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "The source can be retrieved from the URL specified in the location header.\n",
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
                            message = "Not Found. \n The specified device does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the list of installed application on the device.",
                            response = ErrorResponse.class)
            })
    Response getInstalledApplications(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true,
                    allowableValues = "android, ios, windows")
            @PathParam("type")
            @Size(max = 45)
            String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
            String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
            String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
            int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many application details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
            int limit);


    @GET
    @Path("/{type}/{id}/operations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Device Operation Details",
            notes = "Get the details of operations carried out on a selected device.",
            tags = "Device Management",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:operations")
            })
    }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of operations scheduled for the device.",
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
                                            description = "Date and time the resource was last modified" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "The source can be retrieved from the URL specified in the location header.\n",
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
                            message = "Not Found. \n The specified device does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the operation list scheduled for the device.",
                            response = ErrorResponse.class)
            })
    Response getDeviceOperations(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true,
                    allowableValues = "android, ios, windows")
            @PathParam("type")
            @Size(max = 45)
            String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device you wish to get details.\n" +
                            "INFO: Make sure to add the ID of a device that is already registered with WSO2 EMM.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
            String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
            String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
            int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many activity details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
            int limit,
            @ApiParam(
                    name = "owner",
                    value = "Provides the owner of the required device.",
                    required = true,
                    defaultValue = "")
            @QueryParam("owner")
                    String owner);

    @GET
    @Path("/{type}/{id}/effective-policy")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the details of the policy that is enforced on a device.",
            notes = "A policy is enforced on all the devices that register with WSO2 EMM." +
                    "WSO2 EMM filters the policies based on the device platform (device type)," +
                    "the device ownership type, the user role or name and finally, the policy that matches these filters will be enforced on the device.",
            tags = "Device Management",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:effective-policy")
            })
    }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully returned the details of the policy enforced on the device.",
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "The source can be retrieved from the URL specified in the location header.\n",
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
                            message = "Not Found. \n The specified device does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the policy details that is enforced on the device.",
                            response = ErrorResponse.class)
            }
    )
    Response getEffectivePolicyOfDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true,
                    allowableValues = "android, ios, windows")
            @PathParam("type")
            @Size(max = 45)
            String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
            String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
            String ifModifiedSince);


    @GET
    @Path("{type}/{id}/compliance-data")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Policy Compliance Details of a Device",
            notes = "A policy is enforced on the devices that register with WSO2 EMM. " +
                    "The server checks if the settings in the device comply with the policy that is enforced on the device using this REST API.",
            tags = "Device Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:compliance-data")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK",
                            response = NonComplianceData.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Error occurred while getting the compliance data.",
                            response = ErrorResponse.class)
            }
    )
    Response getComplianceDataOfDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
            String type,
            @ApiParam(
                    name = "id",
                    value = "Device Identifier",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
            String id);
}
