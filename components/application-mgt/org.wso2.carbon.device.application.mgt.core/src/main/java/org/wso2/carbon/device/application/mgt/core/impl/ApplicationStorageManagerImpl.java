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
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * This class contains the default concrete implementation of ApplicationStorage Management.
 */
public class ApplicationStorageManagerImpl implements ApplicationStorageManager {
    private static final Log log = LogFactory.getLog(ApplicationStorageManagerImpl.class);

    @Override
    public void uploadImageArtifacts(String applicationUUID, InputStream iconFileStream, InputStream bannerFileStream,
            List<InputStream> screenShotStreams) throws ApplicationStorageManagementException {
        Application application;
        try {
            application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUUID);
        } catch (ApplicationManagementException e) {
            throw new ApplicationStorageManagementException(
                    "Exception while retrieving the application details for " + "the application with UUID "
                            + applicationUUID);
        }
        if (application == null) {
            throw new ApplicationStorageManagementException("Application with UUID " + applicationUUID + " does not "
                    + "exist. Cannot upload the artifacts to non-existing application.");
        }
        String artifactDirectoryPath = Constants.artifactPath + application.getId();
        if (log.isDebugEnabled()) {
            log.debug("Artifact Directory Path for saving the artifacts related with application " + applicationUUID
                    + " is " + artifactDirectoryPath);
        }
        createArtifactDirectory(artifactDirectoryPath);
        if (iconFileStream != null) {
            String iconName = application.getIconName();
            iconName = (iconName == null) ? "icon" : iconName;
            try {
                saveFile(iconFileStream, artifactDirectoryPath + File.separator + iconName);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "IO Exception while saving the icon file in the server for " + "the application "
                                + applicationUUID, e);
            }
        }
        if (bannerFileStream != null) {
            String bannerName = application.getBannerName();
            bannerName = (bannerName == null) ? "banner" : bannerName;
            try {
                saveFile(bannerFileStream, artifactDirectoryPath + File.separator + bannerName);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "IO Exception while saving the banner file in the server for" + " the application "
                                + applicationUUID, e);
            }
        }
        if (screenShotStreams != null) {
            int count = 1;
            String screenshotName;
            List<String> screenShotNames = application.getScreenshots();
            boolean isScreenShotNameExist = (screenShotNames == null || screenShotNames.isEmpty());
            int screenShotNameLength = isScreenShotNameExist ? screenShotNames.size() : 0;
            for (InputStream screenshotStream : screenShotStreams) {
                try {
                    if (isScreenShotNameExist && count <= screenShotNameLength) {
                        screenshotName = screenShotNames.get(count);
                    } else {
                        screenshotName = "screenshot_" + count;
                    }
                    saveFile(screenshotStream, artifactDirectoryPath + File.separator + screenshotName);
                    count++;
                } catch (IOException e) {
                    throw new ApplicationStorageManagementException(
                            "IO Exception while saving the screens hots for the " + "application " + applicationUUID,
                            e);
                }
            }
        }
    }

    @Override
    public void uploadReleaseArtifacts(String applicationUUID, String versionName, InputStream binaryFile)
            throws ApplicationStorageManagementException {
        Application application;
        try {
            application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUUID);
        } catch (ApplicationManagementException e) {
            throw new ApplicationStorageManagementException(
                    "Exception while retrieving the application details for " + "the application with UUID "
                            + applicationUUID);
        }
        if (application == null) {
            throw new ApplicationStorageManagementException("Application with UUID " + applicationUUID + " does not "
                    + "exist. Cannot upload release artifacts for not existing application.");
        }
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
        Application application;
        try {
            application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUUID);
        } catch (ApplicationManagementException e) {
            throw new ApplicationStorageManagementException(
                    "Exception while retrieving the application details for " + "the application with UUID "
                            + applicationUUID);
        }
        if (application == null) {
            throw new ApplicationStorageManagementException("Application with UUID " + applicationUUID + " does not "
                    + "exist. Cannot retrieve release artifacts for not existing application.");
        }
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
        Application application;
        try {
            application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUUID);
        } catch (ApplicationManagementException e) {
            throw new ApplicationStorageManagementException(
                    "Exception while retrieving the application details for " + "the application with UUID "
                            + applicationUUID);
        }
        if (application == null) {
            throw new ApplicationStorageManagementException("Application with UUID " + applicationUUID + " does not "
                    + "exist. Cannot delete the artifacts of a non-existing application.");
        }
        String artifactDirectoryPath = Constants.artifactPath + application.getId();
        File artifactDirectory = new File(artifactDirectoryPath);

        if (artifactDirectory.exists()) {
            deleteDir(artifactDirectory);
        }
    }

    @Override
    public void deleteApplicationReleaseArtifacts(String applicationUUID, String version)
            throws ApplicationStorageManagementException {
        Application application;
        try {
            application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUUID);
        } catch (ApplicationManagementException e) {
            throw new ApplicationStorageManagementException(
                    "Exception while retrieving the application details for " + "the application with UUID "
                            + applicationUUID);
        }
        if (application == null) {
            throw new ApplicationStorageManagementException("Application with UUID " + applicationUUID + " does not "
                    + "exist. Cannot delete the artifacts of a non-existing application.");
        }
        String artifactPath = Constants.artifactPath + application.getId() + File.separator + version;
        File artifact = new File(artifactPath);

        if (artifact.exists()) {
            deleteDir(artifact);
        }
    }

    @Override public void deleteAllApplicationReleaseArtifacts(String applicationUUID)
            throws ApplicationStorageManagementException {
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
}
