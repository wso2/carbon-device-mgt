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
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationList;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceAgent Service"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/device/agent"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_agent, device_management", description = "")
        }
)
@Api(value = "Device Agent", description = "Device Agent Service")
@Path("/device/agent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(
        scopes = {
                @Scope(
                        name = "Enroll Device",
                        description = "Register a device",
                        key = "perm:device:enroll",
                        permissions = {"/device-mgt/devices/owning-device/add"}
                ),
                @Scope(
                        name = "Modify Device",
                        description = "Modify a device",
                        key = "perm:device:modify",
                        permissions = {"/device-mgt/devices/owning-device/modify"}
                ),
                @Scope(
                        name = "Disenroll Device",
                        description = "Disenroll a device",
                        key = "perm:device:disenroll",
                        permissions = {"/device-mgt/devices/owning-device/remove"}
                ),
                @Scope(
                        name = "Publish Event",
                        description = "publish device event",
                        key = "perm:device:publish-event",
                        permissions = {"/device-mgt/devices/owning-device/event"}
                ),
                @Scope(
                        name = "Getting Device Operation Details",
                        description = "Getting Device Operation Details",
                        key = "perm:device:operations",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                )
        }
)
public interface DeviceAgentService {

    @POST
    @Path("/enroll")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create a device instance",
            notes = "Create a device Instance",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:enroll")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully created a device instance.",
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
                            message = "Not Found. \n A deviceType with the specified device type was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response enrollDevice(@ApiParam(name = "device", value = "Device object with data.", required = true)
                          @Valid Device device);

    @DELETE
    @Path("/enroll/{type}/{id}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Unregistering a Device",
            notes = "Use this REST API to unregister a device.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:disenroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully disenrolled the device."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while dis-enrolling the device.")
    })
    Response disEnrollDevice(
            @ApiParam(name = "type", value = "The unique device identifier.") @PathParam("type") String type,
            @ApiParam(name = "id", value = "The unique device identifier.") @PathParam("id") String id);

    @PUT
    @Path("/enroll/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "modify device",
            notes = "modify device",
            tags = "Device Agent Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:modify")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated device instance.",
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
                            message = "Not Found. \n A deviceType with the specified device type was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response updateDevice(@ApiParam(name = "type", value = "The device type, such as ios, android or windows....etc", required = true)
                          @PathParam("type") String type,
                          @ApiParam(name = "id", value = "The device id.", required = true)
                          @PathParam("id") String deviceId,
                          @ApiParam(name = "device", value = "Device object with data.", required = true)
                          @Valid Device updateDevice);

    @POST
    @Path("/events/publish/{type}/{deviceId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Publishing Events",
            notes = "Publish events received by the device client to the WSO2 Data Analytics Server (DAS) using this API.",
            tags = "Device Agent Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:publish-event")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK. \n Successfully published the event",
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
                                                    "Used by caches, or in conditional requests.")
                            }),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while publishing events.")
            })
    Response publishEvents(
            @ApiParam(
                    name = "payloadData",
                    value = "Information of the agent event to be published on DAS.")
            @Valid
            Map<String, Object> payloadData,
            @ApiParam(
                    name = "type",
                    value = "name of the device type")
            @PathParam("type") String type,
            @ApiParam(
                    name = "deviceId",
                    value = "deviceId of the device")
            @PathParam("deviceId") String deviceId);

    @POST
    @Path("/events/publish/data/{type}/{deviceId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Publishing Events data only",
            notes = "Publish events received by the device client to the WSO2 Data Analytics Server (DAS) using this API.",
            tags = "Device Agent Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:publish-event")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK. \n Successfully published the event",
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
                                                         "Used by caches, or in conditional requests.")
                                 }),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while publishing events.")
            })
    Response publishEvents(
            @ApiParam(
                    name = "payloadData",
                    value = "Information of the agent event to be published on DAS.")
            @Valid
            List<Object> payloadData,
            @ApiParam(
                    name = "type",
                    value = "name of the device type")
            @PathParam("type") String type,
            @ApiParam(
                    name = "deviceId",
                    value = "deviceId of the device")
            @PathParam("deviceId") String deviceId);

    @GET
    @Path("/pending/operations/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get pending operation of the given device",
            notes = "Returns the Operations.",
            tags = "Device Agent Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:operations")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the operations.",
                            response = OperationList.class,
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
    Response getPendingOperations(@ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
                                  @PathParam("type") String type,
                                  @ApiParam(name = "id", value = "The device id.", required = true)
                                  @PathParam("id") String deviceId);

    @GET
    @Path("/next-pending/operation/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get pending operation of the given device",
            notes = "Returns the Operation.",
            tags = "Device Agent Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:operations")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the operation.",
                            response = Operation.class,
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
    Response getNextPendingOperation(@ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
                                     @PathParam("type") String type,
                                     @ApiParam(name = "id", value = "The device id.", required = true)
                                     @PathParam("id") String deviceId);

    @PUT
    @Path("/operations/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Operation",
            notes = "Update the Operations.",
            tags = "Device Agent Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:operations")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the operations.",
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
    Response updateOperation(@ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
                             @PathParam("type") String type,
                             @ApiParam(name = "id", value = "The device id.", required = true)
                             @PathParam("id") String deviceId,
                             @ApiParam(name = "operation", value = "Operation object with data.", required = true)
                             @Valid Operation operation);

    @GET
    @Path("/status/operations/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get pending operation of the given device",
            notes = "Returns the Operations.",
            tags = "Device Agent Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:operations")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the operations.",
                            response = OperationList.class,
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
    Response getOperationsByDeviceAndStatus(@ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
                                            @PathParam("type") String type,
                                            @ApiParam(name = "id", value = "The device id.", required = true)
                                            @PathParam("id") String deviceId,
                                            @ApiParam(name = "status", value = "status of the operation.", required = true)
                                            @QueryParam("status")Operation.Status status);

}
