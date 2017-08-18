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

import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;

import java.io.InputStream;
import java.util.List;

/**
 * This manages all the storage related requirements of Application.
 */
public interface ApplicationStorageManager {
    /**
     * To upload image artifacts related with an Application.
     *
     * @param applicationUUID UUID of the application
     * @param iconFile        Icon File input stream
     * @param bannerFile      Banner File input stream
     * @throws ApplicationStorageManagementException Application Storage Management Exception.
     */
    public void uploadImageArtifacts(String applicationUUID, InputStream iconFile, InputStream bannerFile,
            List<InputStream> screenshots) throws ApplicationStorageManagementException;

    /**
     * To upload release artifacts for an Application.
     * @param applicationUUID UUID of the application related with the release.
     * @param versionName Name of version of the Applcation Release.
     * @param binaryFile Binary File for the release.
     * @throws ApplicationStorageManagementException Application Storage Management Exception.
     */
    public void uploadReleaseArtifacts(String applicationUUID, String versionName, InputStream binaryFile) throws
            ApplicationStorageManagementException;
}
