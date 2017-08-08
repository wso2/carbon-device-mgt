/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
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
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.Platform;

import javax.validation.constraints.Size;
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

/**
 * API for handling platform related operations in application management.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Platform Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "PlatformManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/platforms"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management, application_management", description = "Platform Management APIS "
                        + "related with Application Management")
        }
)
@Scopes (
        scopes = {
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Get platform details",
                        description = "Get platform details",
                        key = "perm:platform:get",
                        permissions = {"/device-mgt/platform/get"}
                ),
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Add a platform",
                        description = "Add a platform",
                        key = "perm:platform:add",
                        permissions = {"/device-mgt/platform/add"}
                ),
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Update a platform",
                        description = "Update a platform",
                        key = "perm:platform:update",
                        permissions = {"/device-mgt/platform/update"}
                ),
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Remove a platform",
                        description = "Remove a platform",
                        key = "perm:platform:remove",
                        permissions = {"/device-mgt/platform/remove"}
                )
        }
)
@Api(value = "Platform Management", description = "This API carries all platform management related operations " +
        "such as get all the available platform for a tenant, etc.")
@Path("/platforms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PlatformManagementAPI {
    String SCOPE = "scope";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get all platforms",
            notes = "This will get all platforms that is visible for tenants",
            tags = "Platform Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:platform:get")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got platforms list.",
                            response = Platform.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the platform list.",
                            response = ErrorResponse.class)
            })
    Response getPlatforms(
            @ApiParam(name = "status", allowableValues = "ENABLED, DISABLED, ALL", value =
                    "Provide the status of platform for that tenant:\n"
                            + "- ENABLED: The platforms that are currently enabled for the tenant\n"
                            + "- DISABLED: The platforms that can be used by the tenant but disabled "
                            + "to be used for tenant\n"
                            + "- ALL: All the list of platforms that can be used by the tenant", required = false)
            @QueryParam("status")
            @Size(max = 45)
                    String status
    );

    @GET
    @Path("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get platform",
            notes = "This will return the platform which is registered with {identifier}",
            tags = "Platform Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:platform:get")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got requested platform.",
                            response = Platform.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the platform.",
                            response = ErrorResponse.class)
            })
    Response getPlatform(
            @ApiParam(
                    name = "identifier",
                    required = true)
            @PathParam("identifier")
            @Size(max = 45)
                    String identifier
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add Platform",
            notes = "This will a platform for the tenant space",
            tags = "Platform Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:platform:add")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully added the platform"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request parameters passed."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the platform list.",
                            response = ErrorResponse.class)
            })
    Response addPlatform(
            @ApiParam(
                    name = "platform",
                    value = "The payload of the platform",
                    required = true)
                    Platform platform
    );

    @PUT
    @Path("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Platform",
            notes = "This will update the platform configuration for the tenant space",
            tags = "Platform Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:platform:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the platform"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request parameters passed."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the platform list.",
                            response = ErrorResponse.class)
            })
    Response updatePlatform(
            @ApiParam(
                    name = "platform",
                    value = "The payload of the platform",
                    required = true)
                    Platform platform,
            @ApiParam(
                    name = "identifier",
                    required = true)
            @PathParam("identifier")
            @Size(max = 45)
                    String identifier
    );

    @DELETE
    @Path("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Remove Platform",
            notes = "This will remove the relevant platform.",
            tags = "Platform Management",
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
    Response removePlatform(
            @ApiParam(
                    name = "identifier",
                    required = true)
            @PathParam("identifier")
            @Size(max = 45)
                    String identifier
    );

    @PUT
    @Path("update-status/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Platform status",
            notes = "This will update the platform status for the tenant space",
            tags = "Platform Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:platform:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the platform."),
                    @ApiResponse(
                            code = 404,
                            message = "Not found. \n Non-file based platform not found to update."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the platform list.",
                            response = ErrorResponse.class)
            })
    Response updatePlatformStatus(
            @ApiParam(
                    name = "identifier",
                    required = true)
            @PathParam("identifier")
            @Size(max = 45)
                    String identifier,
            @ApiParam(name = "status", allowableValues = "ENABLED, DISABLED", value =
                    "Provide the status of platform for that tenant:\n"
                            + "- ENABLED: The platforms that are currently enabled for the tenant\n"
                            + "- DISABLED: The platforms that currently disabled "
                            + "to be used for tenant\n", required = true)
            @QueryParam("status")
                    String status
    );

}
