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
package org.wso2.carbon.device.application.mgt.core.impl;

import org.wso2.carbon.device.application.mgt.common.UnrestrictedRole;
import org.wso2.carbon.device.application.mgt.common.Visibility;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.VisibilityManagementException;
import org.wso2.carbon.device.application.mgt.common.services.UnrestrictedRoleManager;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.util.List;

//todo need to work on business logic
/**
 * This is the default implementation for the visibility manager.
 */
public class UnrestrictedRoleManagerImpl implements UnrestrictedRoleManager {

    @Override
    public Visibility put(int applicationID, Visibility visibility) throws VisibilityManagementException {
        return null;
//        if (visibility == null) {
//            visibility = new Visibility();
//            visibility.setType(Visibility.Type.PUBLIC);
//        }
//        if (visibility.getAllowedList() == null && !visibility.getType().equals(Visibility.Type.PUBLIC)) {
//            throw new VisibilityManagementException("Visibility is configured for '" + visibility.getType()
//                    + "' but doesn't have any allowed list provided!");
//        }
//        boolean isTransactionStarted = false;
//        try {
//            isTransactionStarted = ConnectionManagerUtil.isTransactionStarted();
//            if (!isTransactionStarted) {
//                ConnectionManagerUtil.beginDBTransaction();
//            }
//            VisibilityDAO visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
//            int visibilityTypeId = visibilityDAO.getVisibilityID(visibility.getType());
//            visibilityDAO.delete(applicationID);
//            visibilityDAO.add(applicationID, visibilityTypeId, visibility.getAllowedList());
//            if (!isTransactionStarted) {
//                ConnectionManagerUtil.commitDBTransaction();
//            }
//            return visibility;
//        } catch (ApplicationManagementException e) {
//            if (!isTransactionStarted) {
//                ConnectionManagerUtil.rollbackDBTransaction();
//            }
//            throw new VisibilityManagementException("Problem occured when trying to fetch the application with ID - "
//                    + applicationID, e);
//        } finally {
//            if (!isTransactionStarted) {
//                ConnectionManagerUtil.closeDBConnection();
//            }
//        }
    }

    @Override
    public List<UnrestrictedRole> getUnrestrictedRoles(int applicationID, int tenantId) throws VisibilityManagementException {
        try {
            VisibilityDAO visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
            List<UnrestrictedRole> unrestrictedRoles = visibilityDAO.getUnrestrictedRoles(applicationID, tenantId);
            if (unrestrictedRoles == null) {
                return null;
            }
            return unrestrictedRoles;
        } catch (ApplicationManagementException e) {
            throw new VisibilityManagementException("Problem occured when trying to fetch the application with ID - "
                    + applicationID, e);
        }
    }

    @Override
    public void remove(int applicationID) throws VisibilityManagementException {
//        boolean isTransactionStarted = false;
//        try {
//            isTransactionStarted = ConnectionManagerUtil.isTransactionStarted();
//            if (!isTransactionStarted) {
//                ConnectionManagerUtil.beginDBTransaction();
//            }
//            VisibilityDAO visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
//            visibilityDAO.delete(applicationID);
//            if (!isTransactionStarted) {
//                ConnectionManagerUtil.commitDBTransaction();
//            }
//        } catch (ApplicationManagementException e) {
//            if (!isTransactionStarted) {
//                ConnectionManagerUtil.rollbackDBTransaction();
//            }
//            throw new VisibilityManagementException("Problem occurred when trying to fetch the application with ID - "
//                    + applicationID, e);
//        } finally {
//            if (!isTransactionStarted) {
//                ConnectionManagerUtil.closeDBConnection();
//            }
//        }
    }

}
