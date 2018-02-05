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

package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.ImageArtifact;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;

import java.io.InputStream;
import java.util.List;

/**
 * This manages all the storage related requirements of Application.
 */
public interface ApplicationStorageManager {
    /**
     * To upload image artifacts related with an Application.
     *
     * @param applicationId ID of the application
     * @param applicationRelease ApplicationRelease Object
     * @param iconFile        Icon File input stream
     * @param bannerFile      Banner File input stream
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationRelease uploadImageArtifacts(int applicationId, ApplicationRelease applicationRelease,
            InputStream iconFile, InputStream bannerFile, List<InputStream> screenshots) throws ResourceManagementException;

    /**
     * To upload image artifacts related with an Application.
     *
     * @param applicationId ID of the application
     * @param uuid          Unique Identifier of the application
     * @param iconFile        Icon File input stream
     * @param bannerFile      Banner File input stream
     * @param screenshots   Input Streams of screenshots
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationRelease updateImageArtifacts(int applicationId, String uuid, InputStream iconFile,
            InputStream bannerFile, List<InputStream> screenshots)
            throws ResourceManagementException, ApplicationManagementException;

    /**
     * To upload release artifacts for an Application.
     *
     * @param applicationId UUID of the application related with the release.
     * @param applicationRelease Application Release Object.
     * @param binaryFile      Binary File for the release.
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationRelease uploadReleaseArtifacts(int applicationId, ApplicationRelease applicationRelease, InputStream binaryFile)
            throws ResourceManagementException;

    /**
     * To upload release artifacts for an Application.
     *
     * @param applicationId Id of the application.
     * @param applicationUuid UUID of the application related with the release.
     * @param binaryFile      Binary File for the release.
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationRelease updateReleaseArtifacts(int applicationId, String applicationUuid, InputStream binaryFile)
            throws ResourceManagementException;

    /**
     * To get released artifacts for the particular version of the application.
     *
     * @param applicationUUID UUID of the Application
     * @param versionName     Version of the release to be retrieved
     * @return the artifact related with the Application Release.
     * @throws ApplicationStorageManagementException Application Storage Management Exception.
     */
    InputStream getReleasedArtifacts(String applicationUUID, String versionName)
            throws ApplicationStorageManagementException;

    /**
     * To delete all the artifacts related with a particular Application.
     *
     * @param applicationUUID UUID of the Application.
     * @throws ApplicationStorageManagementException Application Storage Management Exception.
     */
    void deleteApplicationArtifacts(String applicationUUID) throws ApplicationStorageManagementException;

    /**
     * To delete the artifacts related with particular Application Release.
     *
     * @param applicationUUID UUID of the Application.
     * @param version         Version of ApplicationRelease that need to be deleted.
     * @throws ApplicationStorageManagementException Application Storage Management Exception.
     */
    void deleteApplicationReleaseArtifacts(String applicationUUID, String version)
            throws ApplicationStorageManagementException;

    /**
     * To delete all release artifacts related with particular Application Release.
     *
     * @param applicationUUID UUID of the Application.
     * @throws ApplicationStorageManagementException Application Storage Management Exception
     */
    void deleteAllApplicationReleaseArtifacts(String applicationUUID) throws ApplicationStorageManagementException;

    /**
     * To get particular image artifact of the application.
     *
     * @param applicationUUID UUID of the application, to retrieve the image artifact.
     * @param name            Name of the artifact - icon/banner/screenshot
     * @param count           Position of a parameter to get the image artifact.
     * @return the relevant image artifact.
     * @throws ApplicationStorageManagementException Application Storage Management Exception.
     */
    ImageArtifact getImageArtifact(String applicationUUID, String name, int count)
            throws ApplicationStorageManagementException;
}
