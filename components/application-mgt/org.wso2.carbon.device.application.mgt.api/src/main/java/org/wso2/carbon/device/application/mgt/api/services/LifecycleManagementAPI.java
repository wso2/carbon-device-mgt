/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

import io.swagger.annotations.Api;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Lifecycle Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "LifecycleManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/lifecycle"),
                        })
                }
        ),
        tags = {
                @Tag(name = "lifecycle_management", description = "Lifecycle Management related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get Lifecycle Details",
                        description = "Get lifecycle details",
                        key = "perm:lifecycle:get",
                        permissions = {"/device-mgt/lifecycle/get"}
                ),
                @Scope(
                        name = "Add a lifecycle state",
                        description = "Add a lifecycle state",
                        key = "perm:lifecycle:add",
                        permissions = {"/device-mgt/lifecycle/add"}
                ),
        }
)
@Path("/lifecycle")
@Api(value = "Lifecycle Management", description = "This API carries all lifecycle management related operations.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface LifecycleManagementAPI {

    @GET
    @Path("/states")
    Response getLifecycleStates();
}
