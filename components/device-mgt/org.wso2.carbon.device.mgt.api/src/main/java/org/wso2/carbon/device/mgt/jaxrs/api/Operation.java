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
import org.wso2.carbon.device.mgt.jaxrs.api.context.DeviceOperationContext;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationWrapper;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 */
@Api(value = "Operation")
public interface Operation {

    /* @deprecated */
    @GET
    Response getAllOperations();

    @GET
    @Path("paginate/{type}/{id}")
    Response getDeviceOperations(@PathParam("type") String type, @PathParam("id") String id,
                                 @QueryParam("start") int startIdx, @QueryParam("length") int length,
                                 @QueryParam("search") String search);

    @GET
    @Path("{type}/{id}")
    Response getDeviceOperations(@PathParam("type") String type, @PathParam("id") String id);

    /* @deprecated */
    @POST
    Response addOperation(DeviceOperationContext operationContext);

    @GET
    @Path("{type}/{id}/apps")
    Response getInstalledApps(@PathParam("type") String type, @PathParam("id") String id);

    @POST
    @Path("installApp/{tenantDomain}")
    Response installApplication(ApplicationWrapper applicationWrapper,
                                @PathParam("tenantDomain") String tenantDomain);

    @POST
    @Path("uninstallApp/{tenantDomain}")
    Response uninstallApplication(ApplicationWrapper applicationWrapper,
                                  @PathParam("tenantDomain") String tenantDomain);
}
