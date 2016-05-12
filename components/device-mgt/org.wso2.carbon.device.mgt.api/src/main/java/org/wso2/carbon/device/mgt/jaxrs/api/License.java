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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class represents license related operations.
 */
@Api(value = "License")
@Path("/license")
@SuppressWarnings("NonJaxWsWebServices")
public interface License {

    /**
     * This method returns the license text related to a given device type and language code.
     *
     * @param deviceType   Device type, ex: android, ios
     * @param languageCode Language code, ex: en_US
     * @return Returns the license text
     */
    @GET
    @Path("{deviceType}/{languageCode}")
    @Produces({ MediaType.APPLICATION_JSON })
    Response getLicense(@PathParam("deviceType") String deviceType,
                        @PathParam("languageCode") String languageCode);

    /**
     * This method is used to add license to a specific device type.
     *
     * @param deviceType Device type, ex: android, ios
     * @param license License object
     * @return Returns the acknowledgement for the action
     */
    @POST
    @Path("{deviceType}")
    Response addLicense(@PathParam("deviceType") String deviceType,
                        org.wso2.carbon.device.mgt.common.license.mgt.License license);
}
