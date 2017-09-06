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

package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.Visibility;
import org.wso2.carbon.device.application.mgt.common.exception.VisibilityManagementException;

import java.sql.Connection;

/**
 * This interface manages all the operations related with Application Visibility.
 * This will be invoking the necessary backend calls for the data bases layer
 * and provide the functional implementation.
 */
public interface VisibilityManager {

    /**
     * Add (if there is no visibility configuration for the application) or
     * Update (if there is already existing configuration for the application)
     * the visibility related configuration for the application
     *
     * @param applicationUUID The ID of the application
     * @param visibility    The visibility configuration for the particular application.
     */
    void put(String applicationUUID, Visibility visibility) throws VisibilityManagementException;

    /**
     * Returns the Visibility configuration of the provided applicationUUID.
     *
     * @param applicationUUID The ID of the application
     * @return Visibility configuration
     */
    Visibility get(String applicationUUID) throws VisibilityManagementException;

    /**
     * Remove the visibility configuration mapping for the provided application.
     *
     * @param applicationUUID The ID of the application
     */
    void remove(String applicationUUID) throws VisibilityManagementException;
}
