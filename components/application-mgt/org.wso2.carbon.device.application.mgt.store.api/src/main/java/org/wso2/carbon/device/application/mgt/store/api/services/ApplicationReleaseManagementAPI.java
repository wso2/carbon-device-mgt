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
package org.wso2.carbon.device.application.mgt.store.api.services;

import io.swagger.annotations.*;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.publisher.api.beans.ErrorResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * APIs to handle application management related tasks.
 */
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
                @Tag(name = "application_management, device_management", description = "Application Management related "
                        + "APIs")
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
                @Scope(
                        name = "Update an Application",
                        description = "Update an application",
                        key = "perm:application:update",
                        permissions = {"/device-mgt/application/update"}
                ),
                @Scope(
                        name = "Create an Application",
                        description = "Create an application",
                        key = "perm:application-mgt:login",
                        permissions = {"/device-mgt/application-mgt/login"}
                ),
                @Scope(
                        name = "Delete an Application",
                        description = "Delete an application",
                        key = "perm:application:delete",
                        permissions = {"/device-mgt/application/delete"}
                ),
                @Scope(
                        name = "Create an application category",
                        description = "Create an application category",
                        key = "perm:application-category:create",
                        permissions = {"/device-mgt/application/category/create"}
                ),
                @Scope(
                        name = "Delete an Application category",
                        description = "Delete an application category",
                        key = "perm:application-category:delete",
                        permissions = {"/device-mgt/application/category/delete"}
                )


        }
)
@Path("/publisher/release")
@Api(value = "Application Management", description = "This API carries all application management related operations " +
        "such as get all the applications, add application, etc.")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationReleaseManagementAPI {


    String SCOPE = "scope";

    @GET
    @Path("/release-artifacts/{uuid}/{version}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_OCTET_STREAM,
            httpMethod = "GET",
            value = "Get an application release",
            notes = "This will return the application release indicated by Application UUID and version",
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
                            message = "OK. \n Successfully retrieved the Application release.",
                            response = Attachment.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })
    Response getApplicationReleaseArtifacts(
            @ApiParam(name = "UUID", value = "Unique identifier of the Application", required = true) @PathParam("uuid") String applicationUUID,
            @ApiParam(name = "Version", value = "Version of the Application release need to be retrieved", required = true) @PathParam("version") String version);

    @GET
    @Path("/release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get all the releases or specific release of an application",
            notes = "This will retrieve the all the releases or specific release of an application",
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
                            message = "OK. \n Successfully retrieved the Application release."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })
    Response getApplicationReleasesById(
            @ApiParam(name = "ID", value = "Identifier of the Application", required = true) @PathParam("uuid") String applicationUUID,
            @ApiParam(name = "version", value = "Version of the application", required = false) @QueryParam("version") String version);

    @GET
    @Path("/image-artifacts/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete the releases of a particular applicaion",
            notes = "This will delete the releases or specific release of an application",
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
                            message = "OK. \n Successfully deleted the Application release."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting the release of a"
                                    + "particular application.",
                            response = ErrorResponse.class)
            })
    Response getApplicationImageArtifacts(
            @ApiParam(name = "UUID", value = "Unique identifier of the Application", required = true) @PathParam("uuid") String applicationUUID,
            @ApiParam(name = "name", value = "Name of the artifact to be retrieved", required = true) @QueryParam("name") String name,
            @ApiParam(name = "count", value = "Count of the screen-shot artifact to be retrieved", required = false) @QueryParam("count") int count);

}
