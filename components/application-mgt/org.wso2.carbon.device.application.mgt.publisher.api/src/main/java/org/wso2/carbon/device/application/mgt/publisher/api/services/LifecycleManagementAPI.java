/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.application.mgt.publisher.api.services;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.publisher.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;

import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Lifecycle management related APIs.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Lifecycle Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "LifecycleManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/lifecycles"),
                        })
                }
        ),
        tags = {
                @Tag(name = "lifecycle_management", description = "Lifecycle Management related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get Lifecycle Details",
                        description = "Get lifecycle details",
                        key = "perm:lifecycle:get",
                        permissions = {"/device-mgt/lifecycles/get"}
                ),
                @Scope(
                        name = "Add a lifecycle state",
                        description = "Add a lifecycle state",
                        key = "perm:lifecycle:add",
                        permissions = {"/device-mgt/lifecycles/add"}
                ),
                @Scope(
                        name = "Delete a lifecycle state",
                        description = "Delete a lifecycle state",
                        key = "perm:lifecycle:delete",
                        permissions = {"/device-mgt/lifecycles/delete"}
                )
        }
)
@Path("/lifecycles")
@Api(value = "Lifecycle Management", description = "This API carries all lifecycle management related operations.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface LifecycleManagementAPI {

    String SCOPE = "scope";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get lifecycle states",
            notes = "Get all lifecycle states",
            tags = "Lifecycle Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:lifecycle:get")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved lifecycle states.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the lifecycle list.",
                            response = ErrorResponse.class)
            })
    Response getLifecycleStates();

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a lifecycle state",
            notes = "This will add a new lifecycle state",
            tags = "Lifecycle Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:lifecycle:add")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully add a lifecycle state.",
                            response = Application.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested "
                                    + "resource."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred adding a lifecycle state.",
                            response = ErrorResponse.class)
            })
    Response addLifecycleState(LifecycleState state);

    @Path("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Remove lifecycle state",
            notes = "Remove lifecycle state",
            tags = "Lifecycle Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:platform:remove")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the platform"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting the platform.",
                            response = ErrorResponse.class)
            })
    Response deleteLifecycleState(@PathParam("identifier") String identifier);

    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/lifecycle")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Change the life cycle state of the application",
            notes = "This will change the life-cycle state of the application",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:application-mgt:login")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully changed application state."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response changeLifecycleState(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "state",
                    value = "Lifecycle State that need to be changed to",
                    required = true)
            @QueryParam("state") String state);

    @GET
    @Consumes("application/json")
    @Path("/{uuid}/lifecycle")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Change the life cycle state of the application",
            notes = "This will retrieve the next life cycle states of the application based on the user and the "
                    + "current state",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:application-mgt:login")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the lifecycle states.",
                            response = List.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the life-cycle states.",
                            response = ErrorResponse.class)
            })
    Response getNextLifeCycleStates(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID);
}
