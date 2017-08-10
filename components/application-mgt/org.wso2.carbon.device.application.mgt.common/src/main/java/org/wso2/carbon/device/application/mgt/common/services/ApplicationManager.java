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

import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;

import java.util.List;

/**
 * This interface manages the application creation, deletion and editing of the application.
 */
public interface ApplicationManager {

    /**
     * Creates an application.
     * @param application Application that need to be created.
     * @return Created application
     * @throws ApplicationManagementException Application Management Exception
     */
    public Application createApplication(Application application) throws ApplicationManagementException;

    /**
     * Updates an already existing application.
     * @param application Application that need to be updated.
     * @return Updated Application
     * @throws ApplicationManagementException Application Management Exception
     */
    public Application editApplication(Application application) throws ApplicationManagementException;

    /**
     * Delete an application identified by the unique ID.
     * @param uuid Unique ID for tha application
     * @throws ApplicationManagementException Application Management Exception
     */
    public void deleteApplication(String uuid) throws ApplicationManagementException;

    /**
     * To get the applications based on the search filter.
     * @param filter Search filter
     * @return Applications that matches the given filter criteria.
     * @throws ApplicationManagementException Application Management Exception
     */
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException;

    void changeLifecycle(String applicationUUID, String lifecycleIdentifier) throws ApplicationManagementException;

    /**
     * To get the next possible life-cycle states for the application.
     *
     * @param applicationUUID UUID of the application.
     * @return the List of possible states
     * @throws ApplicationManagementException Application Management Exception
     */
    public List<LifecycleStateTransition> getLifeCycleStates(String applicationUUID)
            throws ApplicationManagementException;
}
