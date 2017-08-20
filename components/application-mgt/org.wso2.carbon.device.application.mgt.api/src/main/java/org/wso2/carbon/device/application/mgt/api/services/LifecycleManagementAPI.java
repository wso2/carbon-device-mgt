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
package org.wso2.carbon.device.application.mgt.api.services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
}
