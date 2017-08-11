/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.api.services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.wso2.carbon.device.application.mgt.common.ApplicationList;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Application Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ApplicationManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management", description = "Application Management related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get Application Details",
                        description = "Get application details",
                        key = "perm:application:get",
                        permissions = {"/device-mgt/application/get"}
                ),
                @Scope(
                        name = "Create an Application",
                        description = "Create an application",
                        key = "perm:application:create",
                        permissions = {"/device-mgt/application/create"}
                ),
        }
)
@Path("/applications")
@Api(value = "Application Management", description = "This API carries all application management related operations " +
        "such as get all the applications, add application, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ApplicationManagementAPI {

    public final static String SCOPE = "scope";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get all applications",
            notes = "This will get all applications",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:application:get")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got application list.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response getApplications(
            @ApiParam(
                    name = "offset",
                    value = "Provide from which position apps should return",
                    required = false,
                    defaultValue = "20")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many apps it should return",
                    required = false,
                    defaultValue = "0")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "searchQuery",
                    value = "Relevant search query to search on",
                    required = false,
                    defaultValue = "*")
            @QueryParam("searchQuery") String searchQuery
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new application",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:application:create")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application.",
                            response = Application.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response createApplication(
            @ApiParam(
                    name = "application",
                    value = "The application that need to be created.",
                    required = true)
            @Valid Application application);

    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/lifecycle")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Change the life cycle state of the application",
            notes = "This will change the life-cycle state of the application",
            tags = "Application Management"
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
            tags = "Application Management"
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
    Response getLifeCycleStates(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID);
}
