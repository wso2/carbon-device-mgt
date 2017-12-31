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
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.publisher.api.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.FileStreamingOutput;
import org.wso2.carbon.device.application.mgt.publisher.api.services.ApplicationReleaseManagementAPI;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of Application Management related APIs.
 */
@Produces({"application/json"})
@Path("/publisher/release")
public class ApplicationReleaseManagementAPIImpl implements ApplicationReleaseManagementAPI {

    private static final int DEFAULT_LIMIT = 20;
    private static Log log = LogFactory.getLog(ApplicationReleaseManagementAPIImpl.class);

    @Override
    @POST
    @Path("/{appType}/{appId}")
    public Response createApplicationRelease(
            @Multipart("applicationRelease") ApplicationRelease applicationRelease,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList,
            @PathParam("appType") String applicationType,
            @PathParam("appId") int applicationId) {

        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        InputStream iconFileStream;
        InputStream bannerFileStream;
        List<InputStream> attachments = new ArrayList<>();

        try {

            if (iconFile == null) {
                throw new ApplicationManagementException(
                        "Icon file is not uploaded for the application release of " + applicationId); }

            if (bannerFile == null) {
                throw new ApplicationManagementException(
                        "Banner file is not uploaded for the application release of " + applicationId); }

            if (attachmentList == null || attachmentList.isEmpty()) {
                throw new ApplicationManagementException(
                        "Screenshots are not uploaded for the application release of " + applicationId); }

            if (binaryFile == null){
                throw new ApplicationManagementException(
                        "Binary file is not uploaded for the application release of " + applicationId); }


            iconFileStream = iconFile.getDataHandler().getInputStream();
            bannerFileStream = bannerFile.getDataHandler().getInputStream();

            for (Attachment screenshot : attachmentList) {
                attachments.add(screenshot.getDataHandler().getInputStream());
            }

            applicationRelease = applicationStorageManager.uploadReleaseArtifacts(applicationId, applicationRelease,
                    binaryFile.getDataHandler().getInputStream());

            if(applicationRelease.getAppStoredLoc() == null || applicationRelease.getAppHashValue() == null){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            applicationRelease = applicationStorageManager.uploadImageArtifacts(applicationId, applicationRelease,
                    iconFileStream, bannerFileStream, attachments);

//            ToDo
            applicationRelease.setUuid(UUID.randomUUID().toString());
            applicationRelease = applicationReleaseManager.createRelease(applicationUUID, applicationRelease);


            return Response.status(Response.Status.CREATED).entity(applicationRelease).build();
        } catch (ApplicationManagementException e) {
            log.error("Error while creating an application release for the application ID " + applicationId,
                    e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            String errorMessage =
                    "Error while uploading binary file and resources for the application release of the application ID "
                            + applicationId;
            log.error(errorMessage, e);
            return APIUtil.getResponse(new ApplicationManagementException(errorMessage, e),
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the releases artifacts of the application id "
                    + applicationId + " the release version is " + applicationRelease.getVersion(), e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @POST
    @Path("/upload-image-artifacts/{uuid}")
    public Response uploadApplicationArtifacts(@PathParam("uuid") String applicationUUID,
            @Multipart("icon") Attachment iconFile, @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList) {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            InputStream iconFileStream;
            InputStream bannerFileStream;
            List<InputStream> attachments = new ArrayList<>();

            if (iconFile != null) {
                iconFileStream = iconFile.getDataHandler().getInputStream();
            } else {
                throw new ApplicationManagementException(
                        "Icon file is not uploaded for the application " + applicationUUID);
            }
            if (bannerFile != null) {
                bannerFileStream = bannerFile.getDataHandler().getInputStream();
            } else {
                throw new ApplicationManagementException(
                        "Banner file is not uploaded for the application " + applicationUUID);
            }
            if (attachmentList != null && !attachmentList.isEmpty()) {
                for (Attachment screenshot : attachmentList) {
                    attachments.add(screenshot.getDataHandler().getInputStream());
                }
            } else {
                throw new ApplicationManagementException(
                        "Screen-shot are not uploaded for the application " + applicationUUID);
            }
            applicationStorageManager
                    .uploadImageArtifacts(applicationUUID, iconFileStream, bannerFileStream, attachments);
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
                    "Exception while trying to read icon, " + "banner files for the application " +
                            applicationUUID, e), Response.Status.BAD_REQUEST);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the image artifacts of the application with the uuid "
                    + applicationUUID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PUT
    @Path("/upload-image-artifacts/{uuid}")
    public Response updateApplicationArtifacts(@PathParam("uuid") String applicationUUID,
            @Multipart("icon") Attachment iconFile, @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList) {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
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
            if (attachmentList != null) {
                for (Attachment screenshot : attachmentList) {
                    attachments.add(screenshot.getDataHandler().getInputStream());
                }
            }
            applicationStorageManager
                    .uploadImageArtifacts(applicationUUID, iconFileStream, bannerFileStream, attachments);
            return Response.status(Response.Status.OK)
                    .entity("Successfully updated artifacts for the application " + applicationUUID).build();
        } catch (IOException e) {
            log.error("Exception while trying to read icon, banner files for the application " + applicationUUID);
            return APIUtil.getResponse(new ApplicationManagementException(
                    "Exception while trying to read icon, banner files for the application " +
                            applicationUUID, e), Response.Status.BAD_REQUEST);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the image artifacts of the application with the uuid "
                            + applicationUUID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }




    @Override
    @PUT
    @Path("/release/{uuid}")
    public Response updateApplicationRelease(@PathParam("uuid") String applicationUUID, @Multipart
            ("applicationRelease") ApplicationRelease applicationRelease, @Multipart("binaryFile") Attachment
                                                     binaryFile) {
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            if (applicationRelease != null) {
                applicationRelease = applicationReleaseManager.updateRelease(applicationUUID, applicationRelease);
            }
            if (binaryFile != null) {
                String version = applicationRelease == null ? null : applicationRelease.getVersion();

                if (version == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Version cannot be null. Version is a "
                            + "mandatory parameter to update the release artifacts").build();
                }
                applicationStorageManager
                        .uploadReleaseArtifacts(applicationUUID, version, binaryFile.getDataHandler().getInputStream());
            }
            return Response.status(Response.Status.OK).entity(applicationRelease).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Error while updating the application release of the application with UUID " + applicationUUID);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("Error while updating the release artifacts of the application with UUID " + applicationUUID);
            return APIUtil.getResponse(new ApplicationManagementException(
                    "Error while updating the release artifacts of the application with UUID "
                            + applicationUUID), Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while updating the releases artifacts of the application with the uuid "
                    + applicationUUID + " for the release " + applicationRelease.getVersion(), e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/release-artifacts/{uuid}/{version}")
    public Response getApplicationReleaseArtifacts(@PathParam("uuid") String applicationUUID,
                                                   @PathParam("version") String version) {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            InputStream binaryFile = applicationStorageManager.getReleasedArtifacts(applicationUUID, version);
            FileStreamingOutput fileStreamingOutput = new FileStreamingOutput(binaryFile);
            Response.ResponseBuilder response = Response.status(Response.Status.OK).entity(fileStreamingOutput);
            response.header("Content-Disposition", "attachment; filename=\"" + version + "\"");
            return response.build();
        } catch (ApplicationStorageManagementException e) {
            log.error("Error while retrieving the binary file of the application release for the application UUID " +
                    applicationUUID + " and version " + version, e);
            if (e.getMessage().contains("Binary file does not exist")) {
                return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
            } else {
                return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    @Path("/release/{uuid}")
    @GET
    public Response getApplicationReleases(@PathParam("uuid") String applicationUUID,
                                           @QueryParam("version") String version) {
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        try {
            if (version == null || version.isEmpty()) {
                List<ApplicationRelease> applicationReleases = applicationReleaseManager.getReleases(applicationUUID);
                return Response.status(Response.Status.OK).entity(applicationReleases).build();
            } else {
                ApplicationRelease applicationRelease = applicationReleaseManager.getRelease(applicationUUID, version);
                return Response.status(Response.Status.OK).entity(applicationRelease).build();
            }
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Error while getting all the application releases for the application with the UUID "
                    + applicationUUID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DELETE
    @Path("/release/{uuid}")
    public Response deleteApplicationRelease(@PathParam("uuid") String applicationUUID,
                                             @QueryParam("version") String version) {
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            if (version != null && !version.isEmpty()) {
                applicationStorageManager.deleteApplicationReleaseArtifacts(applicationUUID, version);
                applicationReleaseManager.deleteApplicationRelease(applicationUUID, version);
                return Response.status(Response.Status.OK)
                        .entity("Successfully deleted Application release with " + "version " + version
                                + " for the application with UUID " + applicationUUID).build();
            } else {
                applicationStorageManager.deleteAllApplicationReleaseArtifacts(applicationUUID);
                applicationReleaseManager.deleteApplicationReleases(applicationUUID);
                return Response.status(Response.Status.OK)
                        .entity("Successfully deleted Application releases for the " + "application with UUID "
                                + applicationUUID).build();
            }
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Error while deleting application release with the application UUID " + applicationUUID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ApplicationStorageManagementException e) {
            log.error("Error occurred while deleting the releases artifacts of the application with the uuid "
                    + applicationUUID + " for the release " + version, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/image-artifacts/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplicationImageArtifacts(@PathParam("uuid") String applicationUUID,
                                                 @QueryParam("name") String name, @QueryParam("count") int count) {
        if (name == null || name.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Name should not be null. Name is mandatory to"
                    + " retrieve the particular image artifact of the release").build();
        }
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            ImageArtifact imageArtifact = applicationStorageManager.getImageArtifact(applicationUUID, name, count);
            Response.ResponseBuilder response = Response.status(Response.Status.OK).entity(imageArtifact);
            return response.build();
        } catch (ApplicationStorageManagementException e) {
            log.error("Application Storage Management Exception while getting the image artifact " + name + " of "
                    + "the application with UUID " + applicationUUID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
