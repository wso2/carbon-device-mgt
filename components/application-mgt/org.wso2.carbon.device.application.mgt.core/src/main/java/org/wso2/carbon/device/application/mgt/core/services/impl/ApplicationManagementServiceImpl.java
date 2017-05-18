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
package org.wso2.carbon.device.application.mgt.core.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManagementService;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagerException;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

public class ApplicationManagementServiceImpl implements ApplicationManagementService {

    private static final Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);

    @Override
    public void createApplication(Application application) {

    }

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagerException {
        try {
            ConnectionManagerUtil.openConnection();
            ApplicationDAO applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
            return applicationDAO.getApplications(filter);
        } catch (ApplicationManagementDAOException e) {
            throw new ApplicationManagerException("Error occurred while obtaining the applications for " +
                    "the given filter.", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagerException("Error occurred while opening a connection to the APPM data source", e);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }
}
