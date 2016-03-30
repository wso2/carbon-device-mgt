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

package org.wso2.carbon.apimgt.application.extension.api;

import org.wso2.carbon.apimgt.application.extension.api.util.RegistrationProfile;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the application registration service that exposed for apimApplicationRegistration
 */
public interface ApiApplicationRegistrationService {

    /**
     * This method is used to register an APIM application for tenant domain.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response register(@PathParam("tenantDomain") String tenantDomain,
                      @QueryParam("applicationName") String applicationName);

    /**
     * This method is used to register api application
     *
     * @param registrationProfile contains the necessary attributes that are needed in order to register an app.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response register(RegistrationProfile registrationProfile);

    @DELETE
    Response unregister(@QueryParam("applicationName") String applicationName);
}
