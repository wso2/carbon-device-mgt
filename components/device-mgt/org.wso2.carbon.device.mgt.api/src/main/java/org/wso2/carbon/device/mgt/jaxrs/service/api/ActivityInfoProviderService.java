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
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.jaxrs.beans.ActivityList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.common.ActivityIdList;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Activity related REST-API implementation.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ActivityInfoProvider"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/activities"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Path("/activities")
@Api(value = "Getting Activity Details", description = "Get the details of the operations/activities executed by the" +
        " server on the registered devices during a defined time period.")
@Scopes(
        scopes = {
        @Scope(
                name = "Get activities",
                description = "Get activities",
                key = "perm:get-activity",
                permissions = {"/device-mgt/devices/owning-device/view"}
                )
        }
)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ActivityInfoProviderService {

    @GET
    @Path("/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Details of a Specific Activity",
            notes = "Retrieve the details of a specific activity/operation, such as the meta information of an " +
                    "operation, and the responses from the devices.",
            tags = "Activity Info Provider",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:get-activity")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the activity details.",
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
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
                    message = "Not Found. \n No activity found with the given ID.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the activity data.",
                    response = ErrorResponse.class)
    })
    Response getActivity(
            @ApiParam(
                    name = "id",
                    value = "Activity ID of the operation/activity.",
                    required = true,
                    defaultValue = "ACTIVITY_1")
            @PathParam("id")
            @Size(max = 45)
            String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time\n." +
                            "Provide the value in the Java Date Format: EEE, d MMM yyyy HH:mm:ss Z\n." +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @GET
    @Path("/ids")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Activity Details of Specified Activity/Operation IDs",
            notes = "Retrieve the details of activities or operations, such as the meta information of an operation," +
                    " and the responses from the devices."+
                    "Define the activity or operation IDs as comma separated values.",
            tags = "Activity Info Provider",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:get-activity")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the activity details.",
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Activity Ids shouldn't be empty",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized operation! Only admin role can perform this "
                            + "operation."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n No activity found with the given IDs.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse occurred while fetching the activity "
                            + "list for the supplied ids.",
                    response = ErrorResponse.class)
    })
    Response getActivities(
            @ApiParam(
                    name = "ids",
                    value = "Comma separated activity/operation IDs",
                    required = true,
                    defaultValue = "ACTIVITY_0")
            @QueryParam("ids") ActivityIdList activityIdList);


    @GET
    @Path("/{id}/{devicetype}/{deviceid}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting a Specific Activity Details of a Device",
            notes = "Retrieve the details of a specific activity/operation, that was sent to a specific device.",
            tags = "Activity Info Provider",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:get-activity")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the activity details.",
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
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
                    message = "Not Found. \n No activity found with the given ID.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the activity data.",
                    response = ErrorResponse.class)
    })
    Response getActivityByDevice(
            @ApiParam(
                    name = "id",
                    value = "Activity id of the operation/activity.",
                    required = true,
                    defaultValue = "ACTIVITY_1")
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "devicetype",
                    value = "The device type name, such as ios, android, windows, or fire-alarm.",
                    required = true)
            @PathParam("devicetype")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "deviceid",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("deviceid")
            @Size(max = 45)
                    String deviceid,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time\n." +
                            "Provide the value in the Java Date Format: EEE, d MMM yyyy HH:mm:ss Z\n." +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @GET
    @Path("/type/{operationCode}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Activity Details",
            notes = "Get the details of the operations/activities executed by the server on the devices registered" +
                    " with WSO2 EMM, during a defined time period.",
            tags = "Activity Info Provider",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:get-activity")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the activity details.",
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n No activities found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the activity data.",
                    response = ErrorResponse.class)
    })
    Response getActivities(
            @ApiParam(
                    name = "operationCode",
                    value = "Operation Code of the Activity",
                    required = true)
            @PathParam("operationCode") String operationCode,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = true,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many activity details you require from the starting pagination index/offset.",
                    required = true,
                    defaultValue = "5")
            @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Activity Details",
            notes = "Get the details of the operations/activities executed by the server on the devices registered" +
                    " with WSO2 EMM, during a defined time period.",
            tags = "Activity Info Provider",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:get-activity")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the activity details.",
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the" +
                            " requested resource.\n"),
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
                    message = "Internal Server Error. \n Server error occurred while fetching the activity data.",
                    response = ErrorResponse.class)
    })
    Response getActivities(
            @ApiParam(
                    name = "since",
                    value = "Checks if the requested variant was created since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @QueryParam("since") String since,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many activity details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time\n." +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z\n." +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

}
