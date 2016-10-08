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
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.policy.mgt.common.DeviceGroupWrapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "GroupManagementAdmin", version = "1.0.0", context = "/api/device-mgt/v1.0/admin/groups", tags = {"device_management"})

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
            httpMethod = "GET",
            value = "Get groups by the name.",
            notes = "Get devices the name of device and tenant.",
            response = DeviceGroupWrapper.class,
            responseContainer = "List",
            tags = "Group Management Administrative Service")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of groups.",
                         response = DeviceGroupWrapper.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of the " +
                            "requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n Server error occurred while fetching the group list.")
    })
    @Permission(name = "View All Groups", permission = "/permission/admin/device-mgt/user/groups/list")
    Response getGroupsOfUser(
            @ApiParam(
                    name = "username",
                    value = "Username of the user.",
                    required = true)
            @QueryParam("username") String username,
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
}
