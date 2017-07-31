/*
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
package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.api.services.PlatformManagementAPI;
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/platforms")
public class PlatformManagementAPIImpl implements PlatformManagementAPI {

    private static final String ALL_STATUS = "ALL";
    private static final String ENABLED_STATUS = "ENABLED";
    private static final String DISABLED_STATUS = "DISABLED";

    private static Log log = LogFactory.getLog(PlatformManagementAPIImpl.class);

    @GET
    @Override
    public Response getPlatforms(@QueryParam("status") String status) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);

        if (log.isDebugEnabled()) {
            log.debug("API request received for getting the platforms with the status " + status);
        }
        try {
            List<Platform> platforms = APIUtil.getPlatformManager().getPlatforms(tenantDomain);
            List<Platform> results;
            if (status != null) {
                if (status.contentEquals(ALL_STATUS)) {
                    results = platforms;
                } else if (status.contentEquals(ENABLED_STATUS)) {
                    results = new ArrayList<>();
                    for (Platform platform : platforms) {
                        if (platform.isEnabled()) {
                            results.add(platform);
                        }
                    }
                } else if (status.contentEquals(DISABLED_STATUS)) {
                    results = new ArrayList<>();
                    for (Platform platform : platforms) {
                        if (!platform.isEnabled()) {
                            results.add(platform);
                        }
                    }
                } else {
                    results = platforms;
                }
            } else {
                results = platforms;
            }
            if (log.isDebugEnabled()) {
                log.debug("Number of platforms with the status " + status + " : " + results.size());
            }
            return Response.status(Response.Status.OK).entity(results).build();
        } catch (PlatformManagementException e) {
            log.error("Error while getting the platforms for tenant - " + tenantDomain, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Override
    @Path("/{identifier}")
    public Response getPlatform(@PathParam("identifier") String id) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        try {
            Platform platform = APIUtil.getPlatformManager().getPlatform(tenantDomain, id);
            return Response.status(Response.Status.OK).entity(platform).build();
        } catch (PlatformManagementDAOException e) {
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (PlatformManagementException e) {
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Override
    public Response addPlatform(Platform platform) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        try {
            if (platform != null) {
                if (platform.validate()) {
                    APIUtil.getPlatformManager().register(tenantDomain, platform);
                    return Response.status(Response.Status.CREATED).build();
                } else {
                    return APIUtil.getResponse("Invxalid payload! Platform ID and names are mandatory fields!",
                            Response.Status.BAD_REQUEST);
                }
            } else {
                return APIUtil.getResponse("Invalid payload! Platform needs to be passed as payload!",
                        Response.Status.BAD_REQUEST);
            }
        } catch (PlatformManagementException e) {
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{identifier}")
    @Override
    public Response updatePlatform(Platform platform, @PathParam("identifier") @Size(max = 45) String id) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        try {
            APIUtil.getPlatformManager().update(tenantDomain, id, platform);
            return Response.status(Response.Status.OK).build();
        } catch (PlatformManagementException e) {
            log.error("Error while updating the platform - " + id + " for tenant domain - " + tenantDomain, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
