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
package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.store.api.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.FileStreamingOutput;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.store.api.services.ApplicationManagementAPI;

import java.io.InputStream;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Implementation of Application Management related APIs.
 */
@Produces({"application/json"})
@Path("/store/applications")
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI {

    private static final int DEFAULT_LIMIT = 20;
    private static Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);

    @GET
    @Consumes("application/json")
    @Override
    public Response getApplications(
            @QueryParam("query") String searchQuery,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();

        try {
            if (limit == 0) {
                limit = DEFAULT_LIMIT;
            }
            Filter filter = new Filter();
            filter.setOffset(offset);
            filter.setLimit(limit);
            filter.setSearchQuery(searchQuery);

            ApplicationList applications = applicationManager.getApplications(filter);

            for (Application application : applications.getApplications()) {
//                ToDo : use better approach to solve this
                String uuId = applicationManager.getUuidOfLatestRelease(application.getId());
                if (uuId != null){
                    application.setUuidOfLatestRelease(uuId);
                    ImageArtifact imageArtifact = applicationStorageManager.getImageArtifact(uuId, Constants.IMAGE_ARTIFACTS[0], 0);
                    application.setIconOfLatestRelease(imageArtifact);
                }else{
//                    ToDo set default icon
                }
            }
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application list";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (ApplicationStorageManagementException e) {
            log.error("Error occurred while getting the image artifacts of the application", e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Consumes("application/json")
    @Path("/{appType}")
    public Response getApplication(@PathParam("appType") String appType, @QueryParam("appName") String appName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            Application application = applicationManager.getApplication(appType, appName);
            if (application == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Application with UUID " + appType + " not found").build();
            }

            //                ToDo : use better approach to solve this
            String uuId = applicationManager.getUuidOfLatestRelease(application.getId());
            if (uuId != null){
                application.setUuidOfLatestRelease(uuId);
                ImageArtifact imageArtifact = applicationStorageManager.getImageArtifact(uuId, Constants.IMAGE_ARTIFACTS[0], 0);
                application.setIconOfLatestRelease(imageArtifact);
            }else{
                //                    ToDo set default icon
            }

            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while getting application with the uuid " + appType, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ApplicationStorageManagementException e) {
            log.error("Error occurred while getting the image artifacts of the application with the uuid " + appType, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    //todo WIP
    @Override
    @Path("/{uuid}")
    @GET
    public Response getApplicationRelease(@PathParam("uuid") String applicationUUID,
            @QueryParam("version") String version) {
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        return null;
        //        try {
        //            if (version == null || version.isEmpty()) {
        ////                List<ApplicationRelease> applicationReleases = applicationReleaseManager.getReleases(applicationUUID);
        ////                return Response.status(Response.Status.OK).entity(applicationReleases).build();
        //            } else {
        ////                ApplicationRelease applicationRelease = applicationReleaseManager.getRelease(applicationUUID, version);
        ////                return Response.status(Response.Status.OK).entity(applicationRelease).build();
        //            }
        //        } catch (NotFoundException e) {
        //            return Response.status(Response.Status.NOT_FOUND).build();
        //        } catch (ApplicationManagementException e) {
        //            log.error("Error while getting all the application releases for the application with the UUID "
        //                    + applicationUUID, e);
        //            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        //        }
    }

}
