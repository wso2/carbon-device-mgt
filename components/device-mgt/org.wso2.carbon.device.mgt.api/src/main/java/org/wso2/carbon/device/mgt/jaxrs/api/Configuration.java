/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * General Tenant Configuration REST-API implementation.
 * All end points support JSON, XMl with content negotiation.
 */
@Api(value = "Configuration", description = "General Tenant Configuration implementation")
@Path("/configuration")
@SuppressWarnings("NonJaxWsWebServices")
@Produces({ "application/json", "application/xml" })
@Consumes({ "application/json", "application/xml" })
public interface Configuration {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "POST",
            value = "Configuring general platform settings",
            notes = "Configure the general platform settings using this REST API",
            response = ResponsePayload.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tenant configuration saved successfully"),
            @ApiResponse(code = 500, message = "Error occurred while saving the tenant configuration")
            })
    Response saveTenantConfiguration(@ApiParam(name = "configuration", value = "The required properties to "
                                    + "update the platform configurations the as the <JSON_PAYLOAD> value",
                                    required = true) TenantConfiguration configuration);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting General Platform Configurations",
            notes = "Get the general platform level configuration details using this REST API",
            response = TenantConfiguration.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the tenant configuration")
            })
    Response getConfiguration();

    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "PUT",
            value = "Updating General Platform Configurations",
            notes = "Update the notification frequency using this REST API",
            response = ResponsePayload.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tenant configuration updated successfully"),
            @ApiResponse(code = 500, message = "Error occurred while updating the tenant configuration")
            })
    Response updateConfiguration(@ApiParam(name = "configuration", value = "The required properties to update"
                                + " the platform configurations the as the <JSON_PAYLOAD> value",
                                required = true) TenantConfiguration configuration);

}
