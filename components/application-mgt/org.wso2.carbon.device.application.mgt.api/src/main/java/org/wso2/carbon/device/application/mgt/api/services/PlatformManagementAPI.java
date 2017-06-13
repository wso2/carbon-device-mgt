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
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Platform;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "Platform Management", description = "This API carries all platform management related operations " +
        "such as get all the available platform for a tenant, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PlatformManagementAPI {
    public final static String SCOPE = "scope";

    @GET
    @Path("platforms")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get all platforms",
            notes = "This will get all applications",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:get-platforms")
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
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
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
}
