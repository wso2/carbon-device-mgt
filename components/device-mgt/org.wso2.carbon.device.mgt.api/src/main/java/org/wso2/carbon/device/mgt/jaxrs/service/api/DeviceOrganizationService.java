/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ResponseHeader;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationMetadataHolder;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceOrganization"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/device-organization"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_organization, device_management", description = "Device organization related " +
                        "REST-APIs. Can be used to manipulate device organization related")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Adding new Device to Organization",
                        description = "Adding new Device to Organization",
                        key = "perm:device-organization:add",
                        permissions = {"/device-mgt/device-organization/add"}
                ),
                @Scope(
                        name = "Getting Device Organization state by ID",
                        description = "Getting Device Organization state by ID",
                        key = "perm:device-organization:state",
                        permissions = {"/device-mgt/device-organization/state"}
                ),
                @Scope(
                        name = "Getting Device Organization parent by ID",
                        description = "Getting Device Organization parent by ID",
                        key = "perm:device-organization:parent",
                        permissions = {"/device-mgt/device-organization/parent"}
                ),
                @Scope(
                        name = "Getting if Device is gateway in Device Organization",
                        description = "Getting if Device is gateway in Device Organization",
                        key = "perm:device-organization:is-gateway",
                        permissions = {"/device-mgt/device-organization/is_gateway"}
                ),
                @Scope(
                        name = "Getting all devices in Organization",
                        description = "Getting all devices in Organization",
                        key = "perm:device-organization:devices",
                        permissions = {"/device-mgt/device-organization/devices"}
                ),
                @Scope(
                        name = "Getting Children of a Device by Parent ID",
                        description = "Getting Children of a Device by Parent ID",
                        key = "perm:device-organization:children",
                        permissions = {"/device-mgt/device-organization/children"}
                ),
                @Scope(
                        name = "Generate Nodes for visualization",
                        description = "Generate Nodes for visualization",
                        key = "perm:device-organization:nodes",
                        permissions = {"/device-mgt/device-organization/nodes"}
                ),
                @Scope(
                        name = "Generate Edges for visualization",
                        description = "Generate Edges for visualization",
                        key = "perm:device-organization:edges",
                        permissions = {"/device-mgt/device-organization/edges"}
                ),
                @Scope(
                        name = "Get hierarchy of device organization",
                        description = "Get hierarchy of device organization",
                        key = "perm:device-organization:hierarchy",
                        permissions = {"/device-mgt/device-organization/hierarchy"}
                ),
                @Scope(
                        name = "Update Device parent in Organization",
                        description = "Update Device parent in Organization",
                        key = "perm:device-organization:updateParent",
                        permissions = {"/device-mgt/device-organization/parent/update"}
                )
        }
)
@Path("/device-organization")
@Api(value = "Device Organization", description = "This API carries all device organization related " +
        "operations such as get all devices in the ")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceOrganizationService {

    @POST
    @Path("/devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "add a new device to organization",
            notes = "add a new device to organization",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:add")
                    })
            }

    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully added a new Device to organization.",
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
                            message = "Not Found",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error",
                            response = ErrorResponse.class)
            }
    )
    Response addDeviceOrganization(@ApiParam(name = "DeviceOrganizationMetadataHolder", value = "Device Organization" +
            "metadata object with data", required = true)
                                   @Valid DeviceOrganizationMetadataHolder deviceOrganizationMetadataHolder);

    @GET
    @Path("/devices/{deviceId}/state")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get state of device from ID",
            notes = "Returns the state of the device based on the ID",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:state")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the state of device.",
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
                            message = "Not Found. \n No device is found under the provided id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            }
    )
    Response getDeviceOrganizationStateById(@ApiParam(name = "deviceId", value = "Unique device identifier",
            required = true) @PathParam("deviceId") String deviceId);

    @GET
    @Path("/devices/{deviceId}/parent")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get parent of device from ID",
            notes = "Returns the parent of the device based on the ID",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:parent")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the parent of device.",
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
                            message = "Not Found. \n No device is found under the provided id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            }
    )
    Response getDeviceOrganizationParent(@ApiParam(name = "deviceId", value = "Unique device identifier", required = true)
                                         @PathParam("deviceId") String deviceId);

    @GET
    @Path("/devices/{deviceId}/isgateway")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get if device is a gateway",
            notes = "Returns if device is a gateway",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:is-gateway")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved if device is a gateway.",
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
                            message = "Not Found. \n No device is found under the provided id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            }
    )
    Response getDeviceOrganizationIsGateway(@ApiParam(name = "deviceId", value = "Unique device identifier",
            required = true) @PathParam("deviceId") String deviceId);

    @GET
    @Path("/devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get devices in Organization",
            notes = "Retrieves all the devices in the Organization",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:devices")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved devices in Organization.",
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
                            message = "Not Found. \n No device is found under the provided id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            }
    )
    Response getDevicesInOrganization();

    @GET
    @Path("/devices/{parentId}/children")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get children connected to a parent ID",
            notes = "Get the children of a device from the device ID",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:children")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved children of device.",
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
                            message = "Not Found. \n No device is found under the provided id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            }
    )
    Response getChildrenByParentId(@ApiParam(name = "parentId", value = "Unique device identifier, in this case " +
            "the parent", required = true) @PathParam("parentId") String parentId);

    /**
     * This method is used by the visualization library to generate the nodes
     *
     * @return list of nodes as an array
     */
    @GET
    @Path("/visualization/nodes")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get nodes for the visualization",
            notes = "Get the generated node data for the visualization library",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:nodes")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved generated nodes.",
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
                            message = "Not Found. \n No device is found under the provided id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            }
    )
    Response generateNodes();

    /**
     * This is used by the visualization library to generate edges
     *
     * @return list of edges as an array
     */
    @GET
    @Path("/visualization/edges")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get edges for the visualization",
            notes = "Get the generated edge data for the visualization library",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:edges")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved generated edges.",
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
                            message = "Not Found. \n No device is found under the provided id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            }
    )
    Response generateEdges();

    @PUT
    @Path("/update/{deviceId}/{newParentId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Parent of device",
            notes = "Update the parent of a device based on the ID",
            tags = "Device Organization",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device-organization:updateParent")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated device parent.",
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
                            message = "Not Found. \n No device is found under the provided id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            }
    )
    Response updateDeviceOrganizationParent(@ApiParam(name = "deviceId", value = "Unique device identifier",
            required = true) @PathParam("deviceId") String deviceId, @ApiParam(name = "newParentId",
            value = "Unique device identifier of parent", required = true) @PathParam("newParentId") String newParentId);
}