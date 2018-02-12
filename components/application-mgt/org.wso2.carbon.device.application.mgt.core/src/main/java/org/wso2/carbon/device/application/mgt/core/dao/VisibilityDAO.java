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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.common.UnrestrictedRole;
import org.wso2.carbon.device.application.mgt.core.exception.VisibilityManagementDAOException;

import java.util.List;

/**
 * This interface provides the list of operations that are performed in the database
 * layer with respect to the visibility.
 * 
 */
public interface VisibilityDAO {

    /**
     * To add unrestricted roles for a particular application.
     *
     * @param unrestrictedRoles unrestrictedRoles that could available the application.
     * @throws VisibilityManagementDAOException Visiblity Management DAO Exception.
     */
    void addUnrestrictedRoles(List<UnrestrictedRole> unrestrictedRoles, int applicationId, int tenantId) throws
            VisibilityManagementDAOException;

    List<UnrestrictedRole> getUnrestrictedRoles(int applicationId, int tenantId) throws VisibilityManagementDAOException;

    void deleteUnrestrictedRoles(List<UnrestrictedRole> unrestrictedRoles, int applicationId, int tenantId) throws
            VisibilityManagementDAOException;

}
