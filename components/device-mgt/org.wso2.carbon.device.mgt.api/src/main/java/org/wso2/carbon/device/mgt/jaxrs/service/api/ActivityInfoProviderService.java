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
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.jaxrs.beans.ActivityList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Activity related REST-API implementation.
 */
@API(name = "Activity Info Provider", version = "1.0.0", context = "/api/device-mgt/v1.0/activities", tags = {"devicemgt_admin"})

@Path("/activities")
@Api(value = "Activity Info Provider", description = "Activity related information manipulation. For example operation details " +
        "and responses from devices.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ActivityInfoProviderService {

    @GET
    @Path("/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve details of a particular activity.",
            notes = "This will return information of a particular activity i.e. meta information of an operation, " +
                    "etc; including the responses from the devices.",
            tags = "Activity Info Provider")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Activity details are successfully fetched",
                    response = Activity.class,
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
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n No activity is found under the provided id.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching activity data.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "View Activities", permission = "/device-mgt/devices/owning-device/view")
    Response getActivity(
            @ApiParam(
                    name = "id",
                    value = "Activity id of the operation/activity to be retrieved.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
            String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);


    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve details of a particular activity.",
            notes = "This will return information of a particular activities i.e. meta information of operations, " +
                    "etc; including the responses from the devices which happened after given time.",
            tags = "Activity Info Provider")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Activity details are successfully fetched",
                    response = ActivityList.class,
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
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n No activities found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching activity data.",
                    response = ErrorResponse.class)
    })
    @Permission(name = "View Activities", permission = "/device-mgt/devices/owning-device/view")
    Response getActivities(
            @ApiParam(
                    name = "since",
                    value = "Validates if the requested variant has not been modified since the time specified, this " +
                            "should be provided in unix format in seconds.",
                    required = false)
            @QueryParam("since") String since,
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
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

}
