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
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;

import java.util.List;
import javax.validation.Valid;
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
                )

        }
)
@Path("/applications")
@Api(value = "Application Management", description = "This API carries all application management related operations " +
        "such as get all the applications, add application, etc.")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationManagementAPI {

    String SCOPE = "scope";

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
                            message = "Not Modified. Empty body because the client already has the latest version "
                                    + "of the requested resource."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response getApplications(
            @ApiParam(
                    name = "offset",
                    value = "Provide from which position apps should return", defaultValue = "20")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many apps it should return", defaultValue = "0")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "searchQuery",
                    value = "Relevant search query to search on", defaultValue = "*")
            @QueryParam("searchQuery") String searchQuery
    );

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get the application specified by the UUID",
            notes = "This will get the application identified by the UUID, if exists",
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
                            message = "OK. \n Successfully retrieved relevant application.",
                            response = Application.class),
                    @ApiResponse(
                            code = 404,
                            message = "Application not found"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting relevant application.",
                            response = ErrorResponse.class)
            })
    Response getApplication(
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the application",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "isWithImages",
                    value = "Whether to return application with images",
                    required = false)
            @QueryParam("isWithImages") Boolean IsWithImages
    );

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Edit an application",
            notes = "This will edit the new application",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:application:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully edited the application.",
                            response = Application.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while editing the application.",
                            response = ErrorResponse.class)
            })
    Response editApplication(
            @ApiParam(
                    name = "application",
                    value = "The application that need to be edited.",
                    required = true)
            @Valid Application application);

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
                                    "Empty body because the client already has the latest version of the requested "
                                    + "resource."),
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

    @POST
    @Path("upload-artifacts/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upload artifacts",
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
                            message = "OK. \n Successfully uploaded artifacts."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response uploadApplicationArtifacts(
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @Multipart(value = "icon") Attachment iconFile,
            @Multipart(value = "banner") Attachment bannerFile,
            @Multipart(value = "screenshot") List<Attachment> screenshots);

    @PUT
    @Path("upload-artifacts/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upload artifacts",
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
                            message = "OK. \n Successfully uploaded artifacts."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response updateApplicationArtifacts(
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @Multipart(value = "icon", required = false) Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart(value = "screenshot", required = false) List<Attachment> screenshots);



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

    @DELETE
    @Consumes("application/json")
    @Path("/{appuuid}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete the application with the given UUID",
            notes = "This will delete the application with the given UUID",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:application:delete")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the application identified by UUID.",
                            response = List.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleteing the application.",
                            response = ErrorResponse.class)
            })
    Response deleteApplication(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("appuuid") String applicationUUID);

    @POST
    @Path("/release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application release",
            notes = "This will create a new application release",
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
                            message = "OK. \n Successfully created an application release.",
                            response = ApplicationRelease.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })

    Response createApplicationRelease(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @Multipart(value = "applicationRelease", type = "application/json") ApplicationRelease applicationRelease,
            @Multipart(value = "binaryFile") Attachment binaryFile);

    @PUT
    @Path("/release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update an application release",
            notes = "This will update a new application release",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:application:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application release.",
                            response = ApplicationRelease.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })

    Response updateApplicationRelease(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @Multipart(value = "applicationRelease", required = false, type = "application/json")
                    ApplicationRelease applicationRelease,
            @Multipart(value = "binaryFile", required = false) Attachment binaryFile);

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
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "Version",
                    value = "Version of the Application release need to be retrieved",
                    required = true)
            @PathParam("version") String version);

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
    Response getApplicationReleases(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "version",
                    value = "Version of the application",
                    required = false)
            @QueryParam("version") String version);

    @DELETE
    @Path("/release/{uuid}")
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
                            @ExtensionProperty(name = SCOPE, value = "perm:application:delete")
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
    Response deleteApplicationRelease(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "version",
                    value = "Version of the application")
            @QueryParam("version") String version);

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
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "name",
                    value = "Name of the artifact to be retrieved",
                    required = true)
            @QueryParam("name") String name,
            @ApiParam(
                    name = "count",
                    value = "Count of the screen-shot artifact to be retrieved",
                    required = false)
            @QueryParam("count") int count);

    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/{version}/{channel}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Make the particular application release as default or not",
            notes = "Make the particular application release as default or not",
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
    Response updateDefaultVersion(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "Version",
                    value = "Version of the Application Release",
                    required = true)
            @PathParam("version") String version,
            @ApiParam(
                    name = "Release Channel",
                    value = "Release Channel",
                    required = true)
            @PathParam("channel") String channel,
            @ApiParam(
                    name = "isDefault",
                    value = "Whether to make it default or not",
                    required = false)
            @QueryParam("isDefault") boolean isDefault);
}
