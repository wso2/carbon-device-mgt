package org.wso2.carbon.device.application.mgt.api.services;/*
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

import io.swagger.annotations.*;
import org.wso2.carbon.device.application.mgt.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.Platform;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "Platform Management", description = "This API carries all platform management related operations " +
        "such as get all the available platform for a tenant, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/platforms")
public interface PlatformManagementAPI {
    public final static String SCOPE = "scope";

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
                            @ExtensionProperty(name = SCOPE, value = "perm:get-platform")
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
            @ApiParam(
                    name = "status",
                    allowableValues = "ENABLED, DISABLED, ALL",
                    value = "Provide the status of platform for that tenant:\n" +
                            "- ENABLED: The platforms that are currently enabled for the tenant\n" +
                            "- DISABLED: The platforms that can be used by the tenant but disabled to be used for tenant\n" +
                            "- ALL: All the list of platforms that can be used by the tenant",
                    required = false)
            @QueryParam("status")
            @Size(max = 45)
                    String status
    );

    @GET
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get platform",
            notes = "This will get application which was registered with {code}",
            tags = "Platform Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:get-platform")
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
                    name = "code",
                    required = true)
            @PathParam("code")
            @Size(max = 45)
                    String code
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
                            @ExtensionProperty(name = SCOPE, value = "perm:add-platform")
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

}
