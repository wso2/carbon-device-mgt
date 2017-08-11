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
import org.wso2.carbon.device.application.mgt.api.services.ApplicationManagementAPI;
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;

@Produces({"application/json"})
@Consumes({"application/json"})
@Path("/applications")
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI{

    public static final int DEFAULT_LIMIT = 20;

    public static final String APPLICATION_UPLOAD_EXTENSION = "ApplicationUploadExtension";

    private static Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);

    @GET
    @Consumes("application/json")
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

    @GET
    @Consumes("application/json")
    @Path("/{uuid}")
    public Response getApplication(@PathParam("uuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        return null;
    }

    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/lifecycle")
    public Response changeLifecycleState(@PathParam("uuid") String applicationUUID, @QueryParam("state") String state) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.changeLifecycle(applicationUUID, state);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while changing the lifecycle of application: " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.OK)
                .entity("Successfully changed the lifecycle state of the application: " + applicationUUID).build();
    }

    @GET
    @Path("/{uuid}/lifecycle")
    @Override
    public Response getLifeCycleStates(String applicationUUID) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            return Response.status(Response.Status.OK).entity(applicationManager.getLifeCycleStates(applicationUUID))
                    .build();
        } catch (ApplicationManagementException e) {
            log.error("Application Management Exception while trying to get next states for the applications with "
                    + "the application ID", e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes("application/json")
    public Response createApplication(@Valid Application application) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            application = applicationManager.createApplication(application);
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


    @PUT
    @Consumes("application/json")
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

    @DELETE
    @Path("/{appuuid}")
    public Response deleteApplication(@PathParam("appuuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.deleteApplication(uuid);

        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        String responseMsg = "Successfully deleted the application: " + uuid;
        return Response.status(Response.Status.OK).entity(responseMsg).build();
    }
}
