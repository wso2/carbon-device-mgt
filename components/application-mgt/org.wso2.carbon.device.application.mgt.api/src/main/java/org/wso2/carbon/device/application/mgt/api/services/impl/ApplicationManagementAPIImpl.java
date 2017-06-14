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
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.api.APIUtil;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;

@Produces({"application/json"})
@Consumes({"application/json"})
public class ApplicationManagementAPIImpl {

    public static final int DEFAULT_LIMIT = 20;

    public static final String APPLICATION_UPLOAD_EXTENSION = "ApplicationUploadExtension";

    private static Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);

    @GET
    @Consumes("application/json")
    @Path("applications")
    public Response getApplications(@QueryParam("offset") int offset, @QueryParam("limit") int limit,
                                    @QueryParam("query") String searchQuery) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            if (limit == 0) {
                limit = DEFAULT_LIMIT;
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
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Consumes("application/json")
    @Path("applications")
    public Response createApplication(@Valid Application application) {

        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        //TODO : Get username and tenantId
        User user = new User("admin", -1234);
        application.setUser(user);

        try {
            application = applicationManager.createApplication(application);

        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.OK).entity(application).build();
    }


    @PUT
    @Consumes("application/json")
    @Path("applications")
    public Response editApplication(@Valid Application application) {

        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        //TODO : Get username and tenantId
        User user = new User("admin", -1234);
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
