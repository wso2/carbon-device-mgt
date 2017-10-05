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
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.api.services.PlatformManagementAPI;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.PlatformStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Size;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Implementation of PlatformManagement APIs.
 */
@Path("/platforms")
public class PlatformManagementAPIImpl implements PlatformManagementAPI {

    private static final String ALL_STATUS = "ALL";
    private static final String ENABLED_STATUS = "ENABLED";
    private static final String DISABLED_STATUS = "DISABLED";

    private static Log log = LogFactory.getLog(PlatformManagementAPIImpl.class);

    @GET
    @Override
    public Response getPlatforms(@QueryParam("status") String status, @QueryParam("tag") String tag) {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        PlatformStorageManager platformStorageManager = APIUtil.getPlatformStorageManager();

        if (log.isDebugEnabled()) {
            log.debug("API request received for getting the platforms with the status " + status);
        }
        try {
            List<Platform> platforms = APIUtil.getPlatformManager().getPlatforms(tenantID);
            List<Platform> results;
            List<Platform> filteredPlatforms = new ArrayList<>();
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
            if (results != null) {
                for (Platform platform : results) {
                    if (tag == null || tag.isEmpty() || (platform.getTags() != null && platform.getTags()
                            .contains(tag))) {
                        platform.setIcon(platformStorageManager.getIcon(platform.getIdentifier()));
                        filteredPlatforms.add(platform);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Number of platforms with the status " + status + " : " + results.size());
                }
            }
            return Response.status(Response.Status.OK).entity(filteredPlatforms).build();
        } catch (PlatformManagementException e) {
            log.error("Error while getting the platforms for tenant - " + tenantID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (PlatformStorageManagementException e) {
            log.error("Error while getting platform icons for the tenant : " + tenantID, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Override
    @Path("/{identifier}")
    public Response getPlatform(@PathParam("identifier") String id) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            Platform platform = APIUtil.getPlatformManager().getPlatform(tenantId, id);

            if (platform == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Platform not found").build();
            }
            ImageArtifact icon = APIUtil.getPlatformStorageManager().getIcon(id);
            if (icon != null) {
                platform.setIcon(icon);
            }
            return Response.status(Response.Status.OK).entity(platform).build();
        } catch (PlatformManagementDAOException e) {
            log.error("Error while trying the get the platform with the identifier : " + id + " for the tenant :"
                    + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (PlatformManagementException e) {
            log.error("Error while trying the get the platform with the identifier : " + id + " for the tenant :"
                    + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        } catch (PlatformStorageManagementException e) {
            log.error("Platform Storage Management Exception while trying to get the icon for the platform : " + id
                    + " for the tenant : " + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Override
    public Response addPlatform(@Multipart("platform") Platform platform, @Multipart("icon")Attachment icon) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            if (platform != null) {
                if (platform.validate()) {
                    APIUtil.getPlatformManager().register(tenantId, platform);

                    if (icon != null) {
                        InputStream iconFileStream = icon.getDataHandler().getInputStream();
                        APIUtil.getPlatformStorageManager().uploadIcon(platform.getIdentifier(), iconFileStream);
                    }
                    return Response.status(Response.Status.CREATED).build();
                } else {
                    return APIUtil
                            .getResponse("Invalid payload! Platform 'identifier' and 'name' are mandatory fields!",
                                    Response.Status.BAD_REQUEST);
                }
            } else {
                return APIUtil.getResponse("Invalid payload! Platform needs to be passed as payload!",
                        Response.Status.BAD_REQUEST);
            }
        } catch (PlatformManagementException e) {
            log.error("Platform Management Exception while trying to add the platform with identifier : " + platform
                    .getIdentifier() + " for the tenant : " + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("IO Exception while trying to save platform icon for the platform : " + platform.getIdentifier(),
                    e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ResourceManagementException e) {
            log.error("Storage Exception while trying to save platform icon for the platform : " + platform
                    .getIdentifier(), e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{identifier}")
    @Override
    public Response updatePlatform(Platform platform, @PathParam("identifier") @Size(max = 45) String id) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            APIUtil.getPlatformManager().update(tenantId, id, platform);
            return Response.status(Response.Status.OK).build();
        } catch (PlatformManagementException e) {
            log.error("Error while updating the platform - " + id + " for tenant domain - " + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/{identifier}")
    @Override
    public Response removePlatform(@PathParam("identifier") @Size(max = 45) String id) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            APIUtil.getPlatformStorageManager().deleteIcon(id);
            APIUtil.getPlatformManager().unregister(tenantId, id, false);
            return Response.status(Response.Status.OK).build();
        } catch (PlatformManagementException e) {
            log.error(
                    "Platform Management Exception while trying to un-register the platform with the identifier : " + id
                            + " for the tenant : " + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (PlatformStorageManagementException e) {
            log.error("Platform Storage Management Exception while trying to delete the icon of the platform with "
                    + "identifier for the tenant :" + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("update-status/{identifier}")
    @Override
    public Response updatePlatformStatus(@PathParam("identifier") @Size(max = 45) String id, @QueryParam("status")
            String status) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            APIUtil.getPlatformManager().updatePlatformStatus(tenantId, id, status);
            return Response.status(Response.Status.OK).build();
        } catch (PlatformManagementDAOException e) {
            log.error("Platform Management Database Exception while trying to update the status of the platform with "
                    + "the identifier : " + id + " for the tenant : " + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (PlatformManagementException e) {
            log.error("Platform Management Exception while trying to update the status of the platform with the "
                    + "identifier : " + id + " for the tenant : " + tenantId, e);
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("tags/{name}")
    @Override
    public Response getPlatformTags(@PathParam("name") String name) {
        if (name == null || name.isEmpty() || name.length() < 3) {
            return APIUtil.getResponse("In order to get platform tags, it is required to pass the first 3 "
                    + "characters of the platform tag name", Response.Status.INTERNAL_SERVER_ERROR);
        }
        try {
            List<String> platformTags = APIUtil.getPlatformManager().getPlatformTags(name);
            return Response.status(Response.Status.OK).entity(platformTags).build();
        } catch (PlatformManagementException e) {
            log.error("Platform Management Exception while trying to get the platform tags with starting character "
                    + "sequence " + name, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("{identifier}/icon")
    @Override
    public Response updatePlatformIcon(@PathParam("identifier") String identifier, @Multipart("icon") Attachment
            icon) {
        try {
            if (icon != null) {
                InputStream iconFileStream = icon.getDataHandler().getInputStream();
                APIUtil.getPlatformStorageManager().uploadIcon(identifier, iconFileStream);
                return Response.status(Response.Status.OK)
                        .entity("Icon file is successfully updated for the platform :" + identifier).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Icon file is not provided to update")
                        .build();
            }
        } catch (ResourceManagementException e) {
            log.error("Resource Management exception while trying to update the icon for the platform " + identifier);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("IO exception while trying to update the icon for the platform " + identifier);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
