/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.api.services.ApplicationManagementService;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.ApplicationUser;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.api.APIUtil;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/applications")
public class ApplicationManagementServiceImpl implements ApplicationManagementService {
    private static final int DEFAULT_LIMIT = 20;
    public static final String APPLICATION_UPLOAD_EXTENSION = "ApplicationUploadExtension";
    private static Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);

    @GET
    @Override
    public Response getApplications(@HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("query") String searchQuery) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        if (log.isDebugEnabled()) {
            log.debug("Received a query for getting applications : offset - " + offset + " limit - " + limit + " "
                    + "searchQuery - " + searchQuery);
        }
        try {
            if (limit == 0) {
                limit = DEFAULT_LIMIT;
                if (log.isDebugEnabled()) {
                    log.debug("Received a search query with the limit 0, hence using " + DEFAULT_LIMIT + " as limit "
                            + "for getting applications");
                }
            }
            Filter filter = new Filter();
            filter.setOffset(offset);
            filter.setLimit(limit);
            filter.setSearchQuery(searchQuery);
            ApplicationList applications = applicationManager.getApplications(filter);
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application list";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Consumes("application/json")
    public Response createApplication(@Valid Application application) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        //TODO : Get username and tenantId
        ApplicationUser applicationUser = new ApplicationUser(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername(),
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));
        application.setUser(applicationUser);

        if (log.isDebugEnabled()) {
            log.debug("Create Application request received from the user : " + applicationUser.toString());
        }
        try {
            application = applicationManager.createApplication(application);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.CREATED).entity(application).build();
    }

    @PUT
    @Consumes("application/json")
    @Path("applications")
    public Response editApplication(@Valid Application application) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        //TODO : Get username and tenantId
        ApplicationUser user = new ApplicationUser("admin", -1234);
        application.setUser(user);

        try {
            application = applicationManager.editApplication(application);

        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.OK).entity(application).build();
    }
}

