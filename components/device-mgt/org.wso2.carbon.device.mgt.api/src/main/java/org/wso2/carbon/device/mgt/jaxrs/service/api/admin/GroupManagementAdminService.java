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

package org.wso2.carbon.device.mgt.jaxrs.service.api.admin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "Group Management Admin", version = "1.0.0", context = "/api/device-mgt/v1.0/admin/groups", tags = {"devicemgt_admin"})

@Path("/admin/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Group Management Administrative Service", description = "This an  API intended to be used by " +
        "'internal' components to log in as an admin user and do a selected number of operations. " +
        "Further, this is strictly restricted to admin users only ")
public interface GroupManagementAdminService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get the list of groups.",
            notes = "Returns all groups enrolled with the system.",
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
    @Permission(name = "View Groups", permission = "/device-mgt/admin/groups/view")
    Response getGroups(@ApiParam(
                               name = "name",
                               value = "Name of the group.")
                       @QueryParam("name") String name,
                       @ApiParam(
                               name = "owner",
                               value = "Owner of the group.")
                       @QueryParam("owner") String owner,
                       @ApiParam(
                               name = "offset",
                               value = "Starting point within the complete list of items qualified.")
                       @QueryParam("offset") int offset,
                       @ApiParam(
                               name = "limit",
                               value = "Maximum size of resource array to return.")
                       @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get the count of groups belongs to current user.",
            notes = "Returns count of all permitted groups enrolled with the system.",
            tags = "Device Group Management")
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
                    code = 404,
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group count.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "View Groups", permission = "/device-mgt/admin/groups/view")
    Response getGroupCount();

}
