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
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil.saveFile;

/**
 * This class contains the default concrete implementation of ApplicationStorage Management.
 */
public class ApplicationStorageManagerImpl implements ApplicationStorageManager {
    private static final Log log = LogFactory.getLog(ApplicationStorageManagerImpl.class);
    private String storagePath;
    private int screenShotMaxCount;

    /**
     * Create a new ApplicationStorageManager Instance
     *
     * @param storagePath        Storage Path to save the binary and image files.
     * @param screenShotMaxCount Maximum Screen-shots count
     */
    public ApplicationStorageManagerImpl(String storagePath, String screenShotMaxCount) {
        this.storagePath = storagePath;
        this.screenShotMaxCount = Integer.parseInt(screenShotMaxCount);
    }

    @Override
    public void uploadImageArtifacts(String applicationUUID, InputStream iconFileStream, InputStream bannerFileStream,
            List<InputStream> screenShotStreams) throws ResourceManagementException {
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
//        Application application = validateApplication(applicationUUID);
//        String artifactDirectoryPath = storagePath + application.getId();
//        if (log.isDebugEnabled()) {
//            log.debug("Artifact Directory Path for saving the artifacts related with application " + applicationUUID
//                    + " is " + artifactDirectoryPath);
//        }
//        StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);
//        if (iconFileStream != null) {
//            try {
//                saveFile(iconFileStream, artifactDirectoryPath + File.separator + Constants.IMAGE_ARTIFACTS[0]);
//            } catch (IOException e) {
//                throw new ApplicationStorageManagementException(
//                        "IO Exception while saving the icon file in the server for " + "the application "
//                                + applicationUUID, e);
//            }
//        }
//        if (bannerFileStream != null) {
//            try {
//                saveFile(bannerFileStream, artifactDirectoryPath + File.separator + Constants.IMAGE_ARTIFACTS[1]);
//            } catch (IOException e) {
//                throw new ApplicationStorageManagementException(
//                        "IO Exception while saving the banner file in the server for" + " the application "
//                                + applicationUUID, e);
//            }
//        }
//        if (screenShotStreams != null) {
//            int count = application.getScreenShotCount() + 1;
//            boolean maxCountReached = false;
//
//            if (count > screenShotMaxCount) {
//                log.error("Maximum limit for the screen-shot is " + screenShotMaxCount
//                        + " Cannot upload another screenshot for the application with the UUID " + applicationUUID);
//                maxCountReached = true;
//            }
//            String screenshotName;
//
//            if (maxCountReached) {
//                return;
//            }
//            for (InputStream screenshotStream : screenShotStreams) {
//                try {
//                    screenshotName = Constants.IMAGE_ARTIFACTS[2] + count;
//                    saveFile(screenshotStream, artifactDirectoryPath + File.separator + screenshotName);
//                    count++;
//                    if (count > screenShotMaxCount) {
//                        log.error("Maximum limit for the screen-shot is " + screenShotMaxCount
//                                + " Cannot upload another screenshot for the application with the UUID "
//                                + applicationUUID);
//                        break;
//                    }
//                } catch (IOException e) {
//                    throw new ApplicationStorageManagementException(
//                            "IO Exception while saving the screens hots for the " + "application " + applicationUUID,
//                            e);
//                }
//            }
//            try {
//                ConnectionManagerUtil.beginDBTransaction();
//                ApplicationManagementDAOFactory.getApplicationDAO().updateScreenShotCount(applicationUUID, tenantId, count - 1);
//                ConnectionManagerUtil.commitDBTransaction();
//            } catch (TransactionManagementException e) {
//                ConnectionManagerUtil.rollbackDBTransaction();
//                throw new ApplicationStorageManagementException("Transaction Management exception while trying to "
//                        + "update the screen-shot count of the application " + applicationUUID + " for the tenant "
//                        + tenantId, e);
//            } catch (DBConnectionException e) {
//                ConnectionManagerUtil.rollbackDBTransaction();
//                throw new ApplicationStorageManagementException("Database connection management exception while "
//                        + "trying to update the screen-shot count for the application " + applicationUUID + " for the"
//                        + " tenant " + tenantId, e);
//            } catch (ApplicationManagementDAOException e) {
//                ConnectionManagerUtil.rollbackDBTransaction();
//                throw new ApplicationStorageManagementException("Application Management DAO exception while trying to"
//                        + " update the screen-shot count for the application " + applicationUUID + " for the tenant "
//                        + tenantId, e);
//            } finally {
//                ConnectionManagerUtil.closeDBConnection();
//            }
//        }
    }

    @Override
    public void uploadReleaseArtifacts(String applicationUUID, String versionName, InputStream binaryFile)
            throws ResourceManagementException {
        Application application = validateApplication(applicationUUID);
        String artifactDirectoryPath = storagePath + application.getId();
        if (log.isDebugEnabled()) {
            log.debug("Artifact Directory Path for saving the application release related artifacts related with "
                    + "application " + applicationUUID + " is " + artifactDirectoryPath);
        }
        StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);
        if (binaryFile != null) {
            try {
                saveFile(binaryFile, artifactDirectoryPath + File.separator + versionName);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "IO Exception while saving the release artifacts in the server for the application "
                                + applicationUUID, e);
            }
        }
    }

    @Override
    public InputStream getReleasedArtifacts(String applicationUUID, String versionName)
            throws ApplicationStorageManagementException {
        Application application = validateApplication(applicationUUID);
        String artifactPath = storagePath + application.getId() + File.separator + versionName;

        if (log.isDebugEnabled()) {
            log.debug("ApplicationRelease artifacts are searched in the location " + artifactPath);
        }

        File binaryFile = new File(artifactPath);

        if (!binaryFile.exists()) {
            throw new ApplicationStorageManagementException("Binary file does not exist for this release");
        } else {
            try {
                return new FileInputStream(artifactPath);
            } catch (FileNotFoundException e) {
                throw new ApplicationStorageManagementException(
                        "Binary file does not exist for the version " + versionName + " for the application ", e);
            }
        }
    }

    @Override
    public void deleteApplicationArtifacts(String applicationUUID) throws ApplicationStorageManagementException {
        Application application = validateApplication(applicationUUID);
        String artifactDirectoryPath = storagePath + application.getId();
        File artifactDirectory = new File(artifactDirectoryPath);

        if (artifactDirectory.exists()) {
            StorageManagementUtil.deleteDir(artifactDirectory);
        }
    }

    @Override
    public void deleteApplicationReleaseArtifacts(String applicationUUID, String version)
            throws ApplicationStorageManagementException {
        Application application = validateApplication(applicationUUID);
        String artifactPath = storagePath + application.getId() + File.separator + version;
        File artifact = new File(artifactPath);

        if (artifact.exists()) {
            StorageManagementUtil.deleteDir(artifact);
        }
    }

    @Override
    public void deleteAllApplicationReleaseArtifacts(String applicationUUID) throws
            ApplicationStorageManagementException {
        validateApplication(applicationUUID);
        try {
            List<ApplicationRelease> applicationReleases = DataHolder.getInstance().getReleaseManager()
                    .getReleases(applicationUUID);
            for (ApplicationRelease applicationRelease : applicationReleases) {
                deleteApplicationReleaseArtifacts(applicationUUID, applicationRelease.getVersion());
            }
        } catch (ApplicationManagementException e) {
            throw new ApplicationStorageManagementException(
                    "Application Management Exception while getting releases " + "for the application "
                            + applicationUUID, e);
        }
    }

    @Override
    public ImageArtifact getImageArtifact(String applicationUUID, String name, int count) throws
            ApplicationStorageManagementException {
        Application application = validateApplication(applicationUUID);
        validateImageArtifactNames(name);
        String imageArtifactPath = storagePath + application.getId() + File.separator + name.toLowerCase();

        if (name.equalsIgnoreCase(Constants.IMAGE_ARTIFACTS[2])) {
            imageArtifactPath += count;
        }
        File imageFile = new File(imageArtifactPath);
        if (!imageFile.exists()) {
            throw new ApplicationStorageManagementException(
                    "Image artifact " + name + " does not exist for the " + "application with UUID " + applicationUUID);
        } else {
            try {
                return StorageManagementUtil.createImageArtifact(imageFile, imageArtifactPath);
            } catch (FileNotFoundException e) {
                throw new ApplicationStorageManagementException(
                        "File not found exception while trying to get the image artifact " + name + " for the "
                                + "application " + applicationUUID, e);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException("IO Exception while trying to detect the image "
                        + "artifact " + name + " for the application " + applicationUUID, e);
            }
        }
    }

    /**
     * To validate the image artifact names.
     * @param name Name of the image artifact.
     * @throws ApplicationStorageManagementException Application Storage Management Exception
     */
    private void validateImageArtifactNames(String name) throws ApplicationStorageManagementException {
        if (name == null || name.isEmpty()) {
            throw new ApplicationStorageManagementException("Image artifact name cannot be null or empty. It is a "
                    + "required parameter");
        }
        if (!Arrays.asList(Constants.IMAGE_ARTIFACTS).contains(name.toLowerCase())) {
            throw new ApplicationStorageManagementException("Provide artifact name is not valid. Please provide the "
                    + "name among " + Arrays.toString(Constants.IMAGE_ARTIFACTS));
        }
    }

    /**
     * To validate the Application before storing and retrieving the artifacts of a particular application.
     *
     * @param uuid UUID of the Application
     * @return {@link Application} if it is validated
     * @throws ApplicationStorageManagementException Application Storage Management Exception will be thrown if a
     *                                               valid application related with the specific UUID
     *                                               could not be found.
     */
    private Application validateApplication(String uuid) throws ApplicationStorageManagementException {
        Application application;
        try {
            application = DataHolder.getInstance().getApplicationManager().getApplication(uuid);
        } catch (ApplicationManagementException e) {
            throw new ApplicationStorageManagementException(
                    "Exception while retrieving the application details for the application with UUID "
                            + uuid);
        }
        if (application == null) {
            throw new ApplicationStorageManagementException("Application with UUID " + uuid + " does not exist.");
        }
        return application;
    }
}
