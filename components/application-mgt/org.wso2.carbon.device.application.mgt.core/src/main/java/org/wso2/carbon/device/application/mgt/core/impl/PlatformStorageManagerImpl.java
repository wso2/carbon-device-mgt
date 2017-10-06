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

package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.PlatformManager;
import org.wso2.carbon.device.application.mgt.common.services.PlatformStorageManager;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil.saveFile;

/**
 * This is the concrete implementation of {@link PlatformStorageManager}
 */
public class PlatformStorageManagerImpl implements PlatformStorageManager {
    private static final Log log = LogFactory.getLog(ApplicationStorageManagerImpl.class);
    private String storagePath;

    /**
     * This creates a new instance of PlatformStorageManager.
     * @param storagePath Storage path to store the artifacts related with platform.
     */
    public PlatformStorageManagerImpl(String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public void uploadIcon(String platformIdentifier, InputStream iconFileStream) throws ResourceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Platform platform = validatePlatform(tenantId, platformIdentifier);

        if (platform.isFileBased()) {
            throw new ApplicationStorageManagementException("Icons for the file based platforms need to be added "
                    + "directly to the deployment location inside icon folder");
        }
        if (platform.isShared() && tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new PlatformStorageManagementException("Platform " + platformIdentifier
                    + " is a shared platform from super-tenant. Only the super-tenant users can modify it");
        }
        if (log.isDebugEnabled()) {
            log.debug("Artifact Directory Path for saving the artifacts related with application " + platformIdentifier
                    + " is " + storagePath);
        }
        StorageManagementUtil.createArtifactDirectory(storagePath);
        if (iconFileStream != null) {
            try {
                saveFile(iconFileStream, storagePath + File.separator + platform.getId());
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "IO Exception while saving the icon file in the server for the platform " + platformIdentifier,
                        e);
            }
        }
    }

    @Override
    public ImageArtifact getIcon(String platformIdentifier) throws PlatformStorageManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Platform platform = validatePlatform(tenantId, platformIdentifier);
        String imageArtifactPath = storagePath + platform.getId();
        File imageFile = null;

        if (platform.isFileBased()) {
            imageFile = new File(MultitenantUtils.getAxis2RepositoryPath(CarbonContext.getThreadLocalCarbonContext().
                    getTenantId()) + Constants.PLATFORMS_DEPLOYMENT_DIR_NAME + File.separator
                    + Constants.IMAGE_ARTIFACTS[0] + File.separator + platformIdentifier);
        } else {
            imageFile = new File(imageArtifactPath);
        }

        if (!imageFile.exists()) {
            return null;
        } else {
            try {
                return StorageManagementUtil.createImageArtifact(imageFile, imageArtifactPath);
            } catch (FileNotFoundException e) {
                throw new PlatformStorageManagementException(
                        "File not found exception while trying to get the icon for the " + "platform "
                                + platformIdentifier, e);
            } catch (IOException e) {
                throw new PlatformStorageManagementException(
                        "IO Exception while trying to detect the file type of the platform icon  of "
                                + platformIdentifier, e);
            }
        }
    }

    @Override
    public void deleteIcon(String platformIdentifier) throws PlatformStorageManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Platform platform = validatePlatform(tenantId, platformIdentifier);
        String imageArtifactPath = storagePath + platform.getId();

        if (platform.isShared() && tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new PlatformStorageManagementException("Platform " + platformIdentifier + " is a shared platform "
                    + "from super-tenant. Only the super-tenant users can modify it");
        }
        if (platform.isFileBased()) {
            throw new PlatformStorageManagementException("Platform " + platformIdentifier + " is a file based one. "
                    + "Please remove the relevant icon file directly from file system.");
        }

        File imageFile = new File(imageArtifactPath);
        if (imageFile.exists()) {
            imageFile.delete();
        }
    }

    /**
     * To validate the platform, whether the given identifier has a valid platform.
     *
     * @param tenantId ID of the tenant
     * @param identifier Identifier of the platform
     * @return Platform related with the particular identifier.
     */
    private Platform validatePlatform(int tenantId, String identifier) throws PlatformStorageManagementException {
        Platform platform;
        try {
            PlatformManager platformManager = DataHolder.getInstance().getPlatformManager();
            platform = platformManager.getPlatform(tenantId, identifier);
        } catch (PlatformManagementException e) {
            throw new PlatformStorageManagementException(
                    "Platform Management Exception while getting the platform " + "related with the identifier "
                            + identifier);
        }

        if (platform == null) {
            throw new PlatformStorageManagementException("Platform does not exist with the identifier " + identifier);
        }
        return platform;
    }
}
