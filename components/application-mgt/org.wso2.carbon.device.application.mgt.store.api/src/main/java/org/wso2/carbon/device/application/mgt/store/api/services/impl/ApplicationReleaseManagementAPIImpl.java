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
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.store.api.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.FileStreamingOutput;
import org.wso2.carbon.device.application.mgt.store.api.services.ApplicationReleaseManagementAPI;

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
    public Response getApplicationReleasesById(@PathParam("uuid") String applicationUUID,
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
