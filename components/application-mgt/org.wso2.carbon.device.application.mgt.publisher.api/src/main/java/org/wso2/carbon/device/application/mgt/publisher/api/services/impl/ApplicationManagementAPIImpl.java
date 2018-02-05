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
package org.wso2.carbon.device.application.mgt.publisher.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.device.application.mgt.publisher.api.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.FileStreamingOutput;
import org.wso2.carbon.device.application.mgt.publisher.api.services.ApplicationManagementAPI;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
@Path("/publisher/applications")
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI {

    private static final int DEFAULT_LIMIT = 20;
    private static Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);


    @GET
    @Override
    @Consumes("application/json")
    public Response getApplications(
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("query") String searchQuery) {
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
                String uuId = applicationManager.getUuidOfLatestRelease(application.getId());
                if (uuId != null){
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
            String msg = "Error occurred while getting the application list for publisher ";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (ApplicationStorageManagementException e) {
            log.error("Error occurred while getting the image artifacts of the application for publisher", e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Consumes("application/json")
    @Path("/{appType}")
    public Response getApplication(
            @PathParam("appType") String appType,
            @QueryParam("appName") String appName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            Application application = applicationManager.getApplication(appType, appName);
            if (application == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Application with UUID " + appType + " not found").build();
            }

            String uuId = applicationManager.getUuidOfLatestRelease(application.getId());
            if (uuId != null){
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

    @POST
    @Consumes("application/json")
    public Response createApplication(@Valid Application application) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            Application createdApplication = applicationManager.createApplication(application);

            if (application != null){
                return Response.status(Response.Status.CREATED).entity(createdApplication).build();
            }else{
                String msg = "Given device type is not matched with existing device types";
                log.error(msg);
                return  Response.status(Response.Status.BAD_REQUEST).build();
            }
        }catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


    //Todo Not complete
    @PUT
    @Consumes("application/json")
    public Response editApplication(@Valid Application application) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            application = applicationManager.editApplication(application);
        } catch (NotFoundException e) {
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        } catch (ApplicationManagementException e) {
             String msg = "Error occurred while modifying the application";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        }
        return Response.status(Response.Status.OK).entity(application).build();
    }

    //todo this need to be rethink and fix --- > This is somthing change lifecycle
    @DELETE
    @Path("/{appuuid}")
    public Response deleteApplication(@PathParam("appuuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        try {
            applicationReleaseManager.deleteApplicationReleases(uuid);
            applicationStorageManager.deleteApplicationArtifacts(uuid);
            applicationManager.deleteApplication(uuid);
            String responseMsg = "Successfully deleted the application: " + uuid;
            return Response.status(Response.Status.OK).entity(responseMsg).build();
        } catch (NotFoundException e) {
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + uuid;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ApplicationStorageManagementException e) {
            log.error("Error occurred while deleteing the image artifacts of the application with the uuid " + uuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // todo I think we must remove this
    @Override
    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/{version}/{channel}")
    public Response updateDefaultVersion(@PathParam("uuid") String applicationUUID, @PathParam("version") String
            version, @PathParam("channel") String channel, @QueryParam("isDefault") boolean isDefault) {
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        try {
            applicationReleaseManager.changeDefaultRelease(applicationUUID, version, isDefault, channel);
            return Response.status(Response.Status.OK)
                    .entity("Successfully changed the default version for the " + "release channel " + channel
                            + " for the application UUID " + applicationUUID).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Application Release Management Exception while changing the default release for the release "
                    + "channel " + channel + " for the application with UUID " + applicationUUID + " for the version "
                    + version);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
