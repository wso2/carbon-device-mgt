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
package org.wso2.carbon.device.mgt.jaxrs.service.api.admin;


import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceTypePublisherAdminService"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/admin/devicetype"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Path("/admin/devicetype")
@Api(value = "Devicetype deployment Administrative Service", description = "This an  API intended to be used to " +
        "deploy device type components" +
        "Further, this is strictly restricted to admin users only ")
@Scopes(
        scopes = {
                @Scope(
                        name = "Devicetype deployment",
                        description = "Deploy devicetype",
                        key = "perm:devicetype:deployment",
                        permissions = {"/device-mgt/devicetype/deploy"}
                )
        }
)

public interface DeviceTypePublisherAdminService {

    @POST
    @Path("/deploy/{type}")
    @ApiOperation(
            httpMethod = "POST",
            value = "Deploy device type\n",
            notes = "This is an API that can be used to deploy existing device type artifact for tenant",
            response = Response.class,
            tags = "Devicetype Deployment Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devicetype:deployment")
                    })
            })

    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "OK. \n  Successfully deployed the artifacts.",
                    response = Response.class),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while checking the authorization" +
                            " for a specified set of devices.",
                    response = ErrorResponse.class)
    })

    Response doPublish(
            @ApiParam(name = "type",
                value = "The type of deployment." +
                    "INFO: Deploy artifact with given type.",
                required = true)
           @PathParam("type") String type);

    @GET
    @Path("/deploy/{type}/status")
    @ApiOperation(
            httpMethod = "GET",
            value = "Check the status of device type artifact\n",
            notes = "This is an API that can be used to check the status of the artifact",
            response = Response.class,
            tags = "Devicetype Status Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devicetype:deployment")
                    })
            })

    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "OK. \n  Successfully deployed the artifacts.",
                    response = Response.class),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while checking the authorization" +
                            " for a specified set of devices.",
                    response = ErrorResponse.class)
    })

    Response getStatus(@PathParam("type") String deviceType);

}
