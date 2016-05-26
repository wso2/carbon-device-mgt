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

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Features
 */
@API(name = "Feature", version = "1.0.0", context = "/devicemgt_admin/features", tags = {"devicemgt_admin"})

// Below Api is for swagger annotations
@Api(value = "Feature", description = "Feature management related operations can be found here.")
@SuppressWarnings("NonJaxWsWebServices")
@Path("/features")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface Feature {

    /**
     * Get all features for Mobile Device Type
     *
     * @return Feature
     */
    @GET
    @Path("/{type}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Get Feature Details of a Device",
            notes = "WSO2 EMM features enable you to carry out many operations on a given device platform. " +
                    "Using this REST API you can get the features that can be carried out on a preferred device type," +
                    " such as iOS, Android or Windows.",
            response = org.wso2.carbon.device.mgt.common.Feature.class,
            responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "List of Features"),
                            @ApiResponse(code = 500, message = "Error occurred while retrieving the list of features" +
                                                               ".") })
    @Permission(scope = "feature-view", permissions = {"/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getFeatures(@ApiParam(name = "type", value = "Provide the device type, such as ios, android or windows",
                                   required = true) @PathParam("type") String type);

}
