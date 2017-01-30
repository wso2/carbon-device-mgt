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
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupList;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceToGroupsAssignment;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleList;
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

/**
 * Device group related REST-API. This can be used to manipulated device group related details.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "GroupManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/groups"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "Device group related REST-API. " +
                                                               "This can be used to manipulated device group related " +
                                                               "details.")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get the list of groups belongs to current user.",
                        description = "Get the list of groups belongs to current user.",
                        key = "perm:groups:groups",
                        permissions = {"/device-mgt/groups/view"}
                ),
                @Scope(
                        name = "Get the count of groups belongs to current user.",
                        description = "Get the count of groups belongs to current user.",
                        key = "perm:groups:count",
                        permissions = {"/device-mgt/groups/view"}
                ),
                @Scope(
                        name = "Add new device group to the system.",
                        description = "Add new device group to the system.",
                        key = "perm:groups:add",
                        permissions = {"/device-mgt/groups/add"}
                ),
                @Scope(
                        name = "View group specified",
                        description = "View group specified",
                        key = "perm:groups:groups-view",
                        permissions = {"/device-mgt/groups/view"}
                ),
                @Scope(
                        name = "Update a group",
                        description = "Update a group",
                        key = "perm:groups:update",
                        permissions = {"/device-mgt/groups/update"}
                ),
                @Scope(
                        name = "Delete a group",
                        description = "Delete a group",
                        key = "perm:groups:remove",
                        permissions = {"/device-mgt/groups/remove"}
                ),
                @Scope(
                        name = "Manage group sharing with a user",
                        description = "Manage group sharing with a user",
                        key = "perm:groups:share",
                        permissions = {"/device-mgt/groups/share"}
                ),
                @Scope(
                        name = "View list of roles of a device group",
                        description = "View list of roles of a device group",
                        key = "perm:groups:roles",
                        permissions = {"/device-mgt/groups/roles/view"}
                ),
                @Scope(
                        name = "View list of devices in the device group",
                        description = "View list of devices in the device group",
                        key = "perm:groups:devices",
                        permissions = {"/device-mgt/groups/devices/view"}
                ),
                @Scope(
                        name = "View list of device count in the device group",
                        description = "View list of device count in the device group",
                        key = "perm:groups:devices-count",
                        permissions = {"/device-mgt/groups/devices/view"}
                ),
                @Scope(
                        name = "Add devices to group",
                        description = "Add devices to group",
                        key = "perm:groups:devices-add",
                        permissions = {"/device-mgt/groups/devices/add"}
                ),
                @Scope(
                        name = "Remove devices from group",
                        description = "Remove devices from group",
                        key = "perm:groups:devices-remove",
                        permissions = {"/device-mgt/groups/devices/remove"}
                ),
                @Scope(
                        name = "Assign devices to groups",
                        description = "Assign devices to groups",
                        key = "perm:groups:assign",
                        permissions = {"/device-mgt/groups/devices/add"}
                ),
                @Scope(
                        name = "List of groups that have the device",
                        description = "List of groups that have the device",
                        key = "perm:groups:device",
                        permissions = {"/device-mgt/groups/devices/view"}
                )
        }
)
@Path("/groups")
@Api(value = "Device Group Management", description = "This API carries all device group management related " +
                                                      "operations such as get all the available groups, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GroupManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get the list of groups belongs to current user.",
            notes = "Returns all permitted groups enrolled with the system.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:groups")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of device groups.",
                    response = DeviceGroupList.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the groups list.",
                    response = ErrorResponse.class)
    })
    Response getGroups(@ApiParam(
                               name = "name",
                               value = "Name of the group.")
                       @QueryParam("name")
                               String name,
                       @ApiParam(
                               name = "owner",
                               value = "Owner of the group.")
                       @QueryParam("owner")
                               String owner,
                       @ApiParam(
                               name = "offset",
                               value = "The starting pagination index for the complete list of qualified items.",
                               defaultValue = "0")
                       @QueryParam("offset")
                               int offset,
                       @ApiParam(
                               name = "limit",
                               value = "Provide how many device details you require from the starting pagination index/offset.",
                               defaultValue = "5")
                       @QueryParam("limit")
                               int limit);

    @Path("/count")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get the count of groups belongs to current user.",
            notes = "Returns count of all permitted groups enrolled with the system.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:count")
                })
            }

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device group count.",
                    response = DeviceGroupList.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group count.",
                    response = ErrorResponse.class)
    })
    Response getGroupCount();

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Add new device group to the system.",
            notes = "Add device group with current user as the owner.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:add")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Device group has successfully been created",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the added group."),
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
                                                          "Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 401,
                            message = "Unauthorized. \n Current logged in user is not authorized to add device groups.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                      "Server error occurred while adding a new device group.",
                            response = ErrorResponse.class)
            })
    Response createGroup(@ApiParam(
                                 name = "group",
                                 value = "Group object with data.",
                                 required = true)
                         @Valid DeviceGroup group);

    @Path("/id/{groupId}")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "View group specified.",
            notes = "Returns details of group enrolled with the system.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:groups-view")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device group.",
                    response = DeviceGroup.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group details.",
                    response = ErrorResponse.class)
    })
    Response getGroup(@ApiParam(
                              name = "groupId",
                              value = "ID of the group to view.",
                              required = true)
                      @PathParam("groupId") int groupId);

    @Path("/id/{groupId}")
    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_PUT,
            value = "Update a group.",
            notes = "If you wish to make changes to an existing group, that can be done by updating the group using " +
                    "this resource.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:update")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Group has been updated successfully.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body."),
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while updating the group.",
                    response = ErrorResponse.class)
    })
    Response updateGroup(@ApiParam(
                                 name = "groupId",
                                 value = "ID of the group to be updated.",
                                 required = true)
                         @PathParam("groupId") int groupId,
                         @ApiParam(
                                 name = "group",
                                 value = "Group object with data.",
                                 required = true)
                         @Valid DeviceGroup deviceGroup);

    @Path("/id/{groupId}")
    @DELETE
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Delete a group.",
            notes = "If you wish to remove an existing group, that can be done by updating the group using " +
                    "this resource.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:remove")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Group has been deleted successfully.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body."),
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while removing the group.",
                    response = ErrorResponse.class)
    })
    Response deleteGroup(@ApiParam(
                                 name = "groupId",
                                 value = "ID of the group to be deleted.",
                                 required = true)
                         @PathParam("groupId") int groupId);

    @Path("/id/{groupId}/share")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Manage group sharing with a user.",
            notes = "If you wish to share /un share an existing group with a user under defined sharing roles, " +
                    "that can be done using this resource.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:share")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Sharing has been updated successfully.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body."),
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while sharing the group.",
                    response = ErrorResponse.class)
    })
    Response manageGroupSharing(@ApiParam(
                                        name = "groupName",
                                        value = "Name of the group to be shared or unshared.",
                                        required = true)
                                @PathParam("groupId") int groupId,
                                @ApiParam(
                                        name = "userRoles",
                                        value = "User roles to share group with.",
                                        required = true)
                                @Valid List<String> userRoles);

    @Path("/id/{groupId}/roles")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "View list of roles of a device group.",
            notes = "Returns details of roles which particular group has been shared with.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:roles")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the users.",
                    response = RoleList.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the roles.",
                    response = ErrorResponse.class)
    })
    Response getRolesOfGroup(@ApiParam(
                                     name = "groupId",
                                     value = "ID of the group.",
                                     required = true)
                             @PathParam("groupId") int groupId);

    @Path("/id/{groupId}/devices")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "View list of devices in the device group.",
            notes = "Returns list of devices in the device group.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:devices")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the devices.",
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
                                    description = "Date and time the resource has been modified the last time.\n" +
                                                  "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the devices.",
                    response = ErrorResponse.class)
    })
    Response getDevicesOfGroup(@ApiParam(
                                       name = "groupId",
                                       value = "ID of the group.",
                                       required = true)
                               @PathParam("groupId")
                                       int groupId,
                               @ApiParam(
                                       name = "offset",
                                       value = "The starting pagination index for the complete list of qualified items.",
                                       defaultValue = "0")
                               @QueryParam("offset")
                                       int offset,
                               @ApiParam(
                                       name = "limit",
                                       value = "Provide how many device details you require from the starting pagination index/offset.",
                                       defaultValue = "5")
                               @QueryParam("limit")
                                       int limit);

    @Path("/id/{groupId}/devices/count")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "View list of device count in the device group.",
            notes = "Returns device count in the device group.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:devices-count")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device count.",
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
                                    description = "Date and time the resource has been modified the last time.\n" +
                                                  "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching device count.",
                    response = ErrorResponse.class)
    })
    Response getDeviceCountOfGroup(@ApiParam(
                                           name = "groupId",
                                           value = "ID of the group.",
                                           required = true)
                               @PathParam("groupId") int groupId);

    @Path("/id/{groupId}/devices/add")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Add devices to group.",
            notes = "Add existing devices to the device group.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:devices-add")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully add devices to the group.",
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding devices to the group.",
                    response = ErrorResponse.class)
    })
    Response addDevicesToGroup(@ApiParam(
                                       name = "groupId",
                                       value = "ID of the group.",
                                       required = true)
                               @PathParam("groupId") int groupId,
                               @ApiParam(
                                       name = "deviceIdentifiers",
                                       value = "Device identifiers of the devices which needed be added.",
                                       required = true)
                               @Valid List<DeviceIdentifier> deviceIdentifiers);

    @Path("/id/{groupId}/devices/remove")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Remove devices from group.",
            notes = "Remove existing devices from the device group.",
            tags = "Device Group Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:devices-remove")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully removed devices from the group.",
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while removing devices from the group.",
                    response = ErrorResponse.class)
    })
    Response removeDevicesFromGroup(@ApiParam(
                                            name = "groupId",
                                            value = "ID of the group.",
                                            required = true)
                                    @PathParam("groupId") int groupId,
                                    @ApiParam(
                                            name = "deviceIdentifiers",
                                            value = "Device identifiers of the devices which needed to be removed.",
                                            required = true)
                                    @Valid List<DeviceIdentifier> deviceIdentifiers);

    @Path("/device/assign")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Assign devices to groups",
            notes = "Add existing device to device groups.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:assign")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully assign the device to groups.",
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding devices to the group.",
                    response = ErrorResponse.class)
    })
    Response updateDeviceAssigningToGroups(
            @ApiParam(
                    name = "deviceToGroupsAssignment",
                    value = "Device to groups assignment",
                    required = true)
            @Valid DeviceToGroupsAssignment deviceToGroupsAssignment);

    @Path("/device")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "List of groups that have the device",
            notes = "List of groups that have the device.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:groups:device")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK.",
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred.",
                    response = ErrorResponse.class)
    })
    Response getGroups(
            @ApiParam(
                    name = "deviceId",
                    value = "Id of the device.",
                    required = true)
            @QueryParam("deviceId")
                    String deviceId,
            @ApiParam(
                    name = "deviceType",
                    value = "Type of the device.",
                    required = true)
            @QueryParam("deviceType")
                    String deviceType);

}
