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
import org.wso2.carbon.device.application.mgt.common.services.LifecycleStateManager;
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
import java.util.UUID;
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
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application list for publisher ";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Path("/{appType}")
    public Response getApplication(
            @PathParam("appType") String appType,
            @QueryParam("appName") String appName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            Application application = applicationManager.getApplication(appType, appName);
            if (application == null) {
                return Response.status(Response.Status.NOT_FOUND).entity
                        ("Application with UUID " + appType + " not found").build();
            }

            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while getting application with the uuid " + appType, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes("application/json")
    public Response createApplication(
            @Valid Application application,
            @Valid ApplicationRelease applicationRelease,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        InputStream iconFileStream;
        InputStream bannerFileStream;
        List<InputStream> attachments = new ArrayList<>();
        try {
            if (iconFile == null) {
                throw new ApplicationManagementException(
                        "Icon file is not uploaded for the application release of " + application.getName() +
                                " of application type " + application.getType()); }

            if (bannerFile == null) {
                throw new ApplicationManagementException(
                        "Banner file is not uploaded for the application release of " + application.getName() +
                                " of application type " + application.getType()); }

            if (attachmentList == null || attachmentList.isEmpty()) {
                throw new ApplicationManagementException(
                        "Screenshots are not uploaded for the application release of " + application.getName() +
                                " of application type " + application.getType()); }

            if (binaryFile == null){
                throw new ApplicationManagementException(
                        "Binary file is not uploaded for the application release of " + application.getName() +
                                " of application type " + application.getType()); }


            iconFileStream = iconFile.getDataHandler().getInputStream();
            bannerFileStream = bannerFile.getDataHandler().getInputStream();

            for (Attachment screenshot : attachmentList) {
                attachments.add(screenshot.getDataHandler().getInputStream());
            }

            applicationRelease = applicationStorageManager.uploadReleaseArtifacts(applicationRelease,
                    binaryFile.getDataHandler().getInputStream());

            if(applicationRelease.getAppStoredLoc() == null || applicationRelease.getAppHashValue() == null){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            applicationRelease = applicationStorageManager.uploadImageArtifacts(applicationRelease, iconFileStream,
                    bannerFileStream, attachments);

            applicationRelease.setUuid(UUID.randomUUID().toString());
            Application createdApplication = applicationManager.createApplication(application);

            if (application != null){
                return Response.status(Response.Status.CREATED).entity(createdApplication).build();
            }else{
                log.error("Given device type is not matched with existing device types");
                return  Response.status(Response.Status.BAD_REQUEST).build();
            }
        }catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the releases artifacts of the application "
                    + application.getName(), e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            String errorMessage =
                    "Error while uploading binary file and resources for the application release of the application "
                            + application.getName();
            log.error(errorMessage, e);
            return APIUtil.getResponse(new ApplicationManagementException(errorMessage, e),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @POST
    @Path("/image-artifacts/{appId}/{uuid}")
    public Response updateApplicationImageArtifacts(
            @PathParam("appId") int appId,
            @PathParam("uuid") String applicationUUID,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList) {

        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        ApplicationRelease applicationRelease = null;

        try {
            InputStream iconFileStream = null;
            InputStream bannerFileStream = null;
            List<InputStream> attachments = new ArrayList<>();

            if (iconFile != null) {
                iconFileStream = iconFile.getDataHandler().getInputStream();
            }
            if (bannerFile != null) {
                bannerFileStream = bannerFile.getDataHandler().getInputStream();
            }
            if (attachmentList != null && !attachmentList.isEmpty()) {
                for (Attachment screenshot : attachmentList) {
                    attachments.add(screenshot.getDataHandler().getInputStream());
                }
            }
            applicationRelease = applicationStorageManager
                    .updateImageArtifacts(appId, applicationUUID, iconFileStream, bannerFileStream, attachments);
            applicationReleaseManager.updateRelease(appId, applicationRelease);
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application " + applicationUUID).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        } catch (IOException e) {
            log.error("Exception while trying to read icon, banner files for the application " + applicationUUID);
            return APIUtil.getResponse(new ApplicationManagementException(
                            "Exception while trying to read icon, " + "banner files for the application " + applicationUUID, e),
                    Response.Status.BAD_REQUEST);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the image artifacts of the application with the uuid "
                    + applicationUUID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PUT
    @Path("/app-artifacts/{appId}/{uuid}")
    public Response updateApplicationArtifact(
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationUuuid,
            @Multipart("binaryFile") Attachment binaryFile) {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        ApplicationRelease applicationRelease = null;
        try {

            if (binaryFile != null) {
                applicationRelease = applicationStorageManager.updateReleaseArtifacts(applicationId, applicationUuuid,
                        binaryFile.getDataHandler().getInputStream());
                applicationReleaseManager.updateRelease(applicationId, applicationRelease);
                return Response.status(Response.Status.OK)
                        .entity("Successfully uploaded artifacts for the application " + applicationUuuid).build();

            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Uploading artifacts for the application is failed " + applicationUuuid).build();
        } catch (IOException e) {
            log.error("Exception while trying to read icon, banner files for the application " + applicationUuuid);
            return APIUtil.getResponse(new ApplicationManagementException(
                            "Exception while trying to read icon, banner files for the application " + applicationUuuid, e),
                    Response.Status.BAD_REQUEST);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the image artifacts of the application with the uuid "
                    + applicationUuuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while updating the image artifacts of the application with the uuid "
                    + applicationUuuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
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

    @DELETE
    @Path("/{appid}")
    public Response deleteApplication(@PathParam("appid") int applicationId) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            applicationManager.deleteApplication(applicationId);
//            todo delete storage details
//            applicationStorageManager.deleteApplicationArtifacts(uuid);
            String responseMsg = "Successfully deleted the application: " + applicationId;
            return Response.status(Response.Status.OK).entity(responseMsg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + applicationId;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/{appid}/{uuid}")
    public Response deleteApplicationRelease(@PathParam("appid") int applicationId, @PathParam("uuid") String releaseUuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            applicationManager.deleteApplication(applicationId);
//            todo delete release storage details
//            applicationStorageManager.deleteApplicationArtifacts(uuid);
            String responseMsg = "Successfully deleted the application release of: " + applicationId + "";
            return Response.status(Response.Status.OK).entity(responseMsg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + applicationId;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PUT
    @Path("/{appId}/{uuid}")
    public Response updateApplicationRelease(
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") ApplicationRelease applicationRelease,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList) {
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        InputStream iconFileStream = null;
        InputStream bannerFileStream = null;
        List<InputStream> attachments = new ArrayList<>();

        try {

            if (binaryFile != null) {

                //todo add binary file validation
                applicationRelease = applicationStorageManager.updateReleaseArtifacts(applicationId, applicationUUID,
                        binaryFile.getDataHandler().getInputStream());
            }

            if (iconFile != null) {
                iconFileStream = iconFile.getDataHandler().getInputStream();
            }

            if (bannerFile != null) {
                bannerFileStream = bannerFile.getDataHandler().getInputStream();
            }

            if (!attachmentList.isEmpty()) {
                for (Attachment screenshot : attachmentList) {
                    attachments.add(screenshot.getDataHandler().getInputStream());
                }
            }

            applicationRelease = applicationStorageManager
                    .updateImageArtifacts(applicationId, applicationUUID, iconFileStream, bannerFileStream,
                            attachments);

            applicationRelease = applicationReleaseManager.updateRelease(applicationId, applicationRelease);

            return Response.status(Response.Status.OK).entity(applicationRelease).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Error while updating the application release of the application with UUID " + applicationUUID);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("Error while updating the release artifacts of the application with UUID " + applicationUUID);
            return APIUtil.getResponse(new ApplicationManagementException(
                            "Error while updating the release artifacts of the application with UUID " + applicationUUID),
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while updating the releases artifacts of the application with the uuid "
                    + applicationUUID + " for the release " + applicationRelease.getVersion(), e);
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
