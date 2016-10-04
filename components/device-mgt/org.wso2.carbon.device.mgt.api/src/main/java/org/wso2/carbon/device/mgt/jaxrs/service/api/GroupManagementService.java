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
import io.swagger.annotations.ResponseHeader;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupList;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupShare;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupUsersList;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;

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
@API(name = "Group Management", version = "1.0.0", context = "/api/device-mgt/v1.0/groups", tags = {"devicemgt_admin"})

@Path("/groups")
@Api(value = "Device Group Management", description = "This API carries all device group management related operations " +
                                                      "such as get all the available groups, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GroupManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get the list of groups belongs to current user.",
            notes = "Returns all permitted groups enrolled with the system.",
            tags = "Device Group Management")
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
                    code = 404,
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the groups list.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "View Groups", permission = "/device-mgt/groups/view")
    Response getGroups(@ApiParam(
                               name = "offset",
                               value = "Starting point within the complete list of items qualified.")
                       @QueryParam("offset") int offset,
                       @ApiParam(
                               name = "limit",
                               value = "Maximum size of resource array to return.")
                       @QueryParam("limit") int limit);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Add new device group to the system.",
            notes = "Add device group with current user as the owner.",
            tags = "Device Group Management")
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
    @Permission(name = "Add Group", permission = "/device-mgt/groups/add")
    Response createGroup(@ApiParam(
                                 name = "group",
                                 value = "Group object with data.",
                                 required = true)
                         @Valid DeviceGroup group);

    @Path("/{groupName}")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "View group specified with name.",
            notes = "Returns details of group enrolled with the system.",
            tags = "Device Group Management")
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
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group details.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "View Groups", permission = "/device-mgt/groups/view")
    Response getGroup(@ApiParam(
                              name = "groupName",
                              value = "Name of the group to view.",
                              required = true)
                      @PathParam("groupName") String groupName);

    @Path("/{groupName}")
    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_PUT,
            value = "Update a group.",
            notes = "If you wish to make changes to an existing group, that can be done by updating the group using " +
                    "this resource.",
            tags = "Device Group Management")
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
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while updating the group.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "Update Group", permission = "/device-mgt/groups/update")
    Response updateGroup(@ApiParam(
                                 name = "groupName",
                                 value = "Name of the group to be updated.",
                                 required = true)
                         @PathParam("groupName") String groupName,
                         @ApiParam(
                                 name = "group",
                                 value = "Group object with data.",
                                 required = true)
                         @Valid DeviceGroup deviceGroup);

    @Path("/{groupName}")
    @DELETE
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Delete a group.",
            notes = "If you wish to remove an existing group, that can be done by updating the group using " +
                    "this resource.",
            tags = "Device Group Management")
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
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while removing the group.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "Remove Group", permission = "/device-mgt/groups/remove")
    Response deleteGroup(@ApiParam(
                                 name = "groupName",
                                 value = "Name of the group to be deleted.",
                                 required = true)
                         @PathParam("groupName") String groupName);

    @Path("/{groupName}/share")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Manage group sharing with a user.",
            notes = "If you wish to share /un share an existing group with a user under defined sharing roles, " +
                    "that can be done using this resource.",
            tags = "Device Group Management")
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
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while sharing the group.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "Share Group", permission = "/device-mgt/groups/share")
    Response manageGroupSharing(@ApiParam(
                                        name = "groupName",
                                        value = "Name of the group to be shared or unshared.",
                                        required = true)
                                @PathParam("groupName") String groupName,
                                @ApiParam(
                                        name = "deviceGroupShare",
                                        value = "User name and the assigned roles for the share.",
                                        required = true)
                                @Valid DeviceGroupShare deviceGroupShare);

    @Path("/{groupName}/users")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "View list of users of a device group.",
            notes = "Returns details of users which particular group has been shared with.",
            tags = "Device Group Management")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the users.",
                    response = DeviceGroupUsersList.class,
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
                    message = "Internal Server Error. \n Server error occurred while fetching the users.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "View users", permission = "/device-mgt/groups/users/view")
    Response getUsersOfGroup(@ApiParam(
                                     name = "groupName",
                                     value = "Name of the group.",
                                     required = true)
                             @PathParam("groupName") String groupName);

    @Path("/{groupName}/devices")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "View list of devices in the device group.",
            notes = "Returns list of devices in the device group.",
            tags = "Device Group Management")
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
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the devices.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "View devices", permission = "/device-mgt/groups/devices/view")
    Response getDevicesOfGroup(@ApiParam(
                                       name = "groupName",
                                       value = "Name of the group.",
                                       required = true)
                               @PathParam("groupName") String groupName,
                               @ApiParam(
                                       name = "offset",
                                       value = "Starting point within the complete list of items qualified.")
                               @QueryParam("offset") int offset,
                               @ApiParam(
                                       name = "limit",
                                       value = "Maximum size of resource array to return.")
                               @QueryParam("limit") int limit);

    @Path("/{groupName}/devices")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Add devices to group.",
            notes = "Add existing devices to the device group.",
            tags = "Device Group Management")
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
    @Permission(name = "Add devices", permission = "/device-mgt/groups/devices/add")
    Response addDevicesToGroup(@ApiParam(
                                       name = "groupName",
                                       value = "Name of the group.",
                                       required = true)
                               @PathParam("groupName") String groupName,
                               @ApiParam(
                                       name = "deviceIdentifiers",
                                       value = "Device identifiers of the devices which needed be added.",
                                       required = true)
                               @Valid List<DeviceIdentifier> deviceIdentifiers);

    @Path("/{groupName}/devices")
    @DELETE
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Remove devices from group.",
            notes = "Remove existing devices from the device group.",
            tags = "Device Group Management")
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
    @Permission(name = "Remove devices", permission = "/device-mgt/groups/devices/remove")
    Response removeDevicesFromGroup(@ApiParam(
                                            name = "groupName",
                                            value = "Name of the group.",
                                            required = true)
                                    @PathParam("groupName") String groupName,
                                    @ApiParam(
                                            name = "deviceIdentifiers",
                                            value = "Device identifiers of the devices which needed to be removed.",
                                            required = true)
                                    @Valid List<DeviceIdentifier> deviceIdentifiers);

}
