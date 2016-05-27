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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.annotations.api.Permission;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GroupManagementAdminService {

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get groups by the name.",
            notes = "Get devices the name of device and tenant.",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched group details.",
                    response = org.wso2.carbon.device.mgt.common.Device.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Device not found."),
            @ApiResponse(code = 500, message = "Error while fetching group information.")
    })
    @Permission(scope = "group-view", permissions = {"/permission/admin/device-mgt/user/groups/list"})
    Response getGroupsOfUser(
            @ApiParam(name = "username", value = "Username of the user.",required = true)
            @QueryParam("username") String username,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many policy details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);



}
