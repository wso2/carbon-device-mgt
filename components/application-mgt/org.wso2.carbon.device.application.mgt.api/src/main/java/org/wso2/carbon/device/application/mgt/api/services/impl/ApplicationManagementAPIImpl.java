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
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.api.FileStreamingOutput;
import org.wso2.carbon.device.application.mgt.api.services.ApplicationManagementAPI;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

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
@Path("/applications")
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI {

    private static final int DEFAULT_LIMIT = 20;
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
        try {
            Application application = applicationManager.getApplication(uuid);
            if (application == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Application with UUID " + uuid + " not found").build();
            }
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while getting application with the uuid " + uuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/lifecycle")
    public Response changeLifecycleState(@PathParam("uuid") String applicationUUID,
            @QueryParam("state") String state) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        if (!Arrays.asList(Constants.LIFE_CYCLES).contains(state)) {
            log.warn("Provided lifecycle state " + state + " is not valid. Please select one from"
                    + Arrays.toString(Constants.LIFE_CYCLES));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Provided lifecycle state " + state + " is not valid. Please select one from "
                            + Arrays.toString(Constants.LIFE_CYCLES)).build();
        }
        try {
            applicationManager.changeLifecycle(applicationUUID, state);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while changing the lifecycle of application: " + applicationUUID;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        }
        return Response.status(Response.Status.OK)
                .entity("Successfully changed the lifecycle state of the application: " + applicationUUID).build();
    }

    @GET
    @Path("/{uuid}/lifecycle")
    @Override
    public Response getNextLifeCycleStates(@PathParam("uuid") String applicationUUID) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            if (applicationManager.getApplication(applicationUUID) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Application with the UUID '" + applicationUUID + "' is not found.");
                }
                return Response.status(Response.Status.NOT_FOUND).entity("Application with the UUID '" +
                        applicationUUID + "'  is not found.").build();
            }

            if (log.isDebugEnabled()) {
                log.debug("Application with UUID '" + applicationUUID + "' is found. Request received for getting "
                        + "next life-cycle states for the particular application.");
            }
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

    @Override
    @POST
    @Path("upload-image-artifacts/{uuid}")
    public Response uploadApplicationArtifacts(@PathParam("uuid") String applicationUUID,
            @Multipart("icon")Attachment iconFile, @Multipart("banner") Attachment bannerFile, @Multipart
            ("screenshot") List<Attachment> attachmentList) {
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
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        } catch (IOException e) {
            log.error("Exception while trying to read icon, banner files for the application " + applicationUUID);
            return APIUtil.getResponse(new ApplicationManagementException(
                            "Exception while trying to read icon, " + "banner files for the application " +
                                    applicationUUID, e), Response.Status.BAD_REQUEST);
        }
    }

    @Override
    @PUT
    @Path("upload-image-artifacts/{uuid}")
    public Response updateApplicationArtifacts(@PathParam("uuid") String applicationUUID,
            @Multipart("icon")Attachment iconFile, @Multipart("banner") Attachment bannerFile, @Multipart
            ("screenshot") List<Attachment> attachmentList) {
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
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the artifact for the application " + applicationUUID;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        } catch (IOException e) {
            log.error("Exception while trying to read icon, banner files for the application " + applicationUUID);
            return APIUtil.getResponse(new ApplicationManagementException(
                    "Exception while trying to read icon, banner files for the application " +
                            applicationUUID, e), Response.Status.BAD_REQUEST);
        }
    }

    @PUT
    @Consumes("application/json")
    public Response editApplication(@Valid Application application) {

        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            application = applicationManager.editApplication(application);

        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        String responseMsg = "Successfully deleted the application: " + uuid;
        return Response.status(Response.Status.OK).entity(responseMsg).build();
    }

    @Override
    @POST
    @Path("/release/{uuid}")
    public Response createApplicationRelease(@PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") ApplicationRelease applicationRelease,
            @Multipart("binaryFile") Attachment binaryFile) {
        ApplicationReleaseManager applicationReleaseManager = APIUtil.getApplicationReleaseManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            applicationRelease = applicationReleaseManager.createRelease(applicationUUID, applicationRelease);

            if (binaryFile != null) {
                applicationStorageManager.uploadReleaseArtifacts(applicationUUID, applicationRelease.getVersionName(),
                        binaryFile.getDataHandler().getInputStream());
            }
            return Response.status(Response.Status.CREATED).entity(applicationRelease).build();
        } catch (ApplicationManagementException e) {
            log.error("Error while creating an application release for the application with UUID " + applicationUUID,
                    e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            String errorMessage =
                    "Error while uploading binary file for the application release of the application with UUID "
                            + applicationUUID;
            log.error(errorMessage, e);
            return APIUtil.getResponse(new ApplicationManagementException(errorMessage, e),
                    Response.Status.INTERNAL_SERVER_ERROR);
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
            log.error("Error while retrieving the binary file of the applcation release for the application UUID " +
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
        } catch (ApplicationManagementException e) {
            log.error("Error while getting all the application releases for the application with the UUID "
                    + applicationUUID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
