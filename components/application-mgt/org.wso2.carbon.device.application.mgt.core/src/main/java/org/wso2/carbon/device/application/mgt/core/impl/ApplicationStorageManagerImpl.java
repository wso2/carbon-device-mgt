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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains the default concrete implementation of ApplicationStorage Management.
 */
public class ApplicationStorageManagerImpl implements ApplicationStorageManager {
    private static final Log log = LogFactory.getLog(ApplicationStorageManagerImpl.class);

    @Override
    public void uploadImageArtifacts(String applicationUUID, InputStream iconFileStream, InputStream bannerFileStream,
            List<InputStream> screenShotStreams) throws ApplicationStorageManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application application = validateApplication(applicationUUID);
        String artifactDirectoryPath = Constants.artifactPath + application.getId();
        if (log.isDebugEnabled()) {
            log.debug("Artifact Directory Path for saving the artifacts related with application " + applicationUUID
                    + " is " + artifactDirectoryPath);
        }
        createArtifactDirectory(artifactDirectoryPath);
        if (iconFileStream != null) {
            try {
                saveFile(iconFileStream, artifactDirectoryPath + File.separator + Constants.IMAGE_ARTIFACTS[0]);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "IO Exception while saving the icon file in the server for " + "the application "
                                + applicationUUID, e);
            }
        }
        if (bannerFileStream != null) {
            try {
                saveFile(bannerFileStream, artifactDirectoryPath + File.separator + Constants.IMAGE_ARTIFACTS[1]);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "IO Exception while saving the banner file in the server for" + " the application "
                                + applicationUUID, e);
            }
        }
        if (screenShotStreams != null) {
            int count = application.getScreenShotCount() + 1;
            String screenshotName;
            for (InputStream screenshotStream : screenShotStreams) {
                try {
                    screenshotName = Constants.IMAGE_ARTIFACTS[2] + count;
                    saveFile(screenshotStream, artifactDirectoryPath + File.separator + screenshotName);
                    count++;
                } catch (IOException e) {
                    throw new ApplicationStorageManagementException(
                            "IO Exception while saving the screens hots for the " + "application " + applicationUUID,
                            e);
                }
            }
            try {
                ConnectionManagerUtil.beginDBTransaction();
                DAOFactory.getApplicationDAO().updateScreenShotCount(applicationUUID, tenantId, count - 1);
                ConnectionManagerUtil.commitDBTransaction();
            } catch (TransactionManagementException e) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationStorageManagementException("Transaction Management exception while trying to "
                        + "update the screen-shot count of the application " + applicationUUID + " for the tenant "
                        + tenantId, e);
            } catch (DBConnectionException e) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationStorageManagementException("Database connection management exception while "
                        + "trying to update the screen-shot count for the application " + applicationUUID + " for the"
                        + " tenant " + tenantId, e);
            } catch (ApplicationManagementDAOException e) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationStorageManagementException("Application Management DAO exception while trying to"
                        + " update the screen-shot count for the application " + applicationUUID + " for the tenant "
                        + tenantId, e);
            } finally {
                ConnectionManagerUtil.closeDBConnection();
            }
        }
    }

    @Override
    public void uploadReleaseArtifacts(String applicationUUID, String versionName, InputStream binaryFile)
            throws ApplicationStorageManagementException {
        Application application = validateApplication(applicationUUID);
        String artifactDirectoryPath = Constants.artifactPath + application.getId();
        if (log.isDebugEnabled()) {
            log.debug("Artifact Directory Path for saving the application release related artifacts related with "
                    + "application " + applicationUUID + " is " + artifactDirectoryPath);
        }

        createArtifactDirectory(artifactDirectoryPath);
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
        String artifactPath = Constants.artifactPath + application.getId() + File.separator + versionName;

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
        String artifactDirectoryPath = Constants.artifactPath + application.getId();
        File artifactDirectory = new File(artifactDirectoryPath);

        if (artifactDirectory.exists()) {
            deleteDir(artifactDirectory);
        }
    }

    @Override
    public void deleteApplicationReleaseArtifacts(String applicationUUID, String version)
            throws ApplicationStorageManagementException {
        Application application = validateApplication(applicationUUID);
        String artifactPath = Constants.artifactPath + application.getId() + File.separator + version;
        File artifact = new File(artifactPath);

        if (artifact.exists()) {
            deleteDir(artifact);
        }
    }

    @Override
    public void deleteAllApplicationReleaseArtifacts(String applicationUUID) throws
            ApplicationStorageManagementException {
        Application application = validateApplication(applicationUUID);
        try {
            List<ApplicationRelease> applicationReleases = DataHolder.getInstance().getReleaseManager()
                    .getReleases(applicationUUID);
            for (ApplicationRelease applicationRelease : applicationReleases) {
                deleteApplicationReleaseArtifacts(applicationUUID, applicationRelease.getVersionName());
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
        String imageArtifactPath = Constants.artifactPath + application.getId() + File.separator + name.toLowerCase();

        if (name.equalsIgnoreCase(Constants.IMAGE_ARTIFACTS[2])) {
            imageArtifactPath += count;
        }
        File imageFile = new File(imageArtifactPath);
        if (!imageFile.exists()) {
            throw new ApplicationStorageManagementException(
                    "Image artifact " + name + " does not exist for the " + "application with UUID " + applicationUUID);
        } else {
            try {
                ImageArtifact imageArtifact = new ImageArtifact();
                imageArtifact.setName(imageFile.getName());
                imageArtifact.setType(Files.probeContentType(imageFile.toPath()));
                byte[] imageBytes = IOUtils.toByteArray(new FileInputStream(imageArtifactPath));
                imageArtifact.setEncodedImage(Base64.encodeBase64URLSafeString(imageBytes));
                return imageArtifact;
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
     * To save a file in a given location.
     *
     * @param inputStream Stream of the file.
     * @param path        Path the file need to be saved in.
     */
    private void saveFile(InputStream inputStream, String path) throws IOException {
        OutputStream outStream = null;
        try {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            outStream = new FileOutputStream(new File(path));
            outStream.write(buffer);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }
    }

    /**
     * This method is responsible for creating artifact parent directories in the given path.
     *
     * @param artifactDirectoryPath Path for the artifact directory.
     * @throws ApplicationStorageManagementException Application Storage Management Exception.
     */
    private void createArtifactDirectory(String artifactDirectoryPath) throws ApplicationStorageManagementException {
        File artifactDirectory = new File(artifactDirectoryPath);

        if (!artifactDirectory.exists()) {
            if (!artifactDirectory.mkdirs()) {
                throw new ApplicationStorageManagementException(
                        "Cannot create directories in the path to save the application related artifacts");
            }
        }
    }

    /**
     * To delete a directory recursively
     *
     * @param artifactDirectory Artifact Directory that need to be deleted.
     */
    private void deleteDir(File artifactDirectory) {
        File[] contents = artifactDirectory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteDir(file);
            }
        }
        artifactDirectory.delete();
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
        Application application = null;
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
