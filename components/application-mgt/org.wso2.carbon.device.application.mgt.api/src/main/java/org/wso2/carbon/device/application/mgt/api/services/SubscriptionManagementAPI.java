/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.InstallationDetails;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API to handle subscription management related tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Subscription Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "SubscriptionManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/subscription"),
                        })
                }
        ),
        tags = {
                @Tag(name = "Subscription_management, device_management", description = "Subscription Management " +
                        "related "
                        + "APIs")
        }
)
@Scopes(
        scopes = {
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Install an Application",
                        description = "Install an application",
                        key = "perm:subscription:install",
                        permissions = {"/device-mgt/subscription/install"}
                ),
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Install an Application",
                        description = "Install an application",
                        key = "perm:application-mgt:login",
                        permissions = {"/device-mgt/application-mgt/login"}
                )
        }
)
@Path("/subscription")
@Api(value = "Subscription Management", description = "This API carries all subscription management related " +
        "operations " +
        "such as install application to device, uninstall application from device, etc.")
@Produces(MediaType.APPLICATION_JSON)
public interface SubscriptionManagementAPI {

    String SCOPE = "scope";

    @POST
    @Path("/install-application")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Install an application",
            notes = "This will install an application to a given list of devices/users/roles",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:subscription:install")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully installed the application.",
                            response = Application.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the application is already installed."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while installing the application.",
                            response = ErrorResponse.class)
            })
    Response installApplication(
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list the devices/users/roles",
                    required = true)
            @Valid InstallationDetails installationDetails);
}
