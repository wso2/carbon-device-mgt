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
package org.wso2.carbon.device.mgt.jaxrs.service.api.admin;

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
import org.wso2.carbon.device.mgt.common.authorization.DeviceAuthorizationResult;
import org.wso2.carbon.device.mgt.jaxrs.beans.AuthorizationRequest;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/authorization")
@Api(value = "Device Authorization Administrative Service", description = "This an  API intended to be used by " +
        "'internal' components to log in as an admin user and validate whether the user/device are trusted entity." +
        "Further, this is strictly restricted to admin users only ")

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceAccessAuthorizationAdminService"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/admin/authorization"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Verify device authorization",
                        description = "Verify device authorization",
                        key = "perm:authorization:verify",
                        permissions = {"/device-mgt/authorization/verify"}
                )
        }
)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
/**
 * This interface provided the definition of the device - user access verification service.
 */
public interface DeviceAccessAuthorizationAdminService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Check for device access authorization\n",
            notes = "This is an internal API that can be used to check for authorization.",
            response = DeviceAuthorizationResult.class,
            tags = "Authorization Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:authorization:verify")
                    })
            })

    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Authorized device list will be delivered to the requested services",
                    response = DeviceAuthorizationResult.class),
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
    Response isAuthorized(AuthorizationRequest authorizationRequest);
}
