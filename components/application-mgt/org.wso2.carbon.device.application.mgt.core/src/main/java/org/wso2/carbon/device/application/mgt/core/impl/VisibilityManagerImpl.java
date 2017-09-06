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

import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.Visibility;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.VisibilityManagementException;
import org.wso2.carbon.device.application.mgt.common.services.VisibilityManager;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

/**
 * This is the default implementation for the visibility manager.
 */
public class VisibilityManagerImpl implements VisibilityManager {

    @Override
    public void put(String applicationUUID, Visibility visibility) throws VisibilityManagementException {
        if (visibility.getAllowedList() == null && !visibility.getType().equals(Visibility.Type.PUBLIC)) {
            throw new VisibilityManagementException("Visibility is configured for '" + visibility.getType()
                    + "' but doesn't have any allowed list provided!");
        }
        boolean isTransactionStarted = false;
        try {
            Application application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUUID);
            if (application != null) {
                isTransactionStarted = ConnectionManagerUtil.isTransactionStarted();
                if (!isTransactionStarted) {
                    ConnectionManagerUtil.beginDBTransaction();
                }
                int id = application.getId();
                VisibilityDAO visibilityDAO = DAOFactory.getVisibilityDAO();
                int visibilityTypeId = visibilityDAO.getVisibilityID(visibility.getType());
                visibilityDAO.delete(id);
                visibilityDAO.add(id, visibilityTypeId, visibility.getAllowedList());
                if (!isTransactionStarted) {
                    ConnectionManagerUtil.commitDBTransaction();
                }
            } else {
                throw new VisibilityManagementException("No application was found with application UUID - " + applicationUUID);
            }
        } catch (ApplicationManagementException e) {
            if (!isTransactionStarted){
                ConnectionManagerUtil.rollbackDBTransaction();
            }
            throw new VisibilityManagementException("Problem occured when trying to fetch the application with UUID - "
                    + applicationUUID, e);
        } finally {
            if (!isTransactionStarted) {
                ConnectionManagerUtil.closeDBConnection();
            }
        }
    }

    @Override
    public Visibility get(String applicationUUID) throws VisibilityManagementException {
        try {
            Application application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUUID);
            if (application != null) {
                int id = application.getId();
                VisibilityDAO visibilityDAO = DAOFactory.getVisibilityDAO();
                return visibilityDAO.get(id);
            } else {
                throw new VisibilityManagementException("No application was found with application UUID - " + applicationUUID);
            }
        } catch (ApplicationManagementException e) {
            throw new VisibilityManagementException("Problem occured when trying to fetch the application with UUID - "
                    + applicationUUID, e);
        }
    }

    @Override
    public void remove(String applicationUUID) throws VisibilityManagementException {
        boolean isTransactionStarted = false;
        try {
            Application application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUUID);
            if (application != null) {
                isTransactionStarted = ConnectionManagerUtil.isTransactionStarted();
                if (!isTransactionStarted) {
                    ConnectionManagerUtil.beginDBTransaction();
                }
                int id = application.getId();
                VisibilityDAO visibilityDAO = DAOFactory.getVisibilityDAO();
                visibilityDAO.delete(id);
                if (!isTransactionStarted) {
                    ConnectionManagerUtil.commitDBTransaction();
                }
            } else {
                throw new VisibilityManagementException("No application was found with application UUID - " + applicationUUID);
            }
        } catch (ApplicationManagementException e) {
            if (!isTransactionStarted){
                ConnectionManagerUtil.rollbackDBTransaction();
            }
            throw new VisibilityManagementException("Problem occurred when trying to fetch the application with UUID - "
                    + applicationUUID, e);
        } finally {
            if (!isTransactionStarted) {
                ConnectionManagerUtil.closeDBConnection();
            }
        }
    }

}
