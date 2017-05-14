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
package org.wso2.carbon.device.application.mgt.core.components.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.core.components.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAO;
import org.wso2.carbon.device.application.mgt.core.dto.Application;
import org.wso2.carbon.device.application.mgt.core.dto.lists.ApplicationList;
import org.wso2.carbon.device.application.mgt.core.dto.Filter;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagerException;
import org.wso2.carbon.device.application.mgt.core.internal.ApplicationManagementDataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

public class ApplicationManagerImpl implements ApplicationManager {

    private static final Log log = LogFactory.getLog(ApplicationManagerImpl.class);


    private static ApplicationManagerImpl applicationManager = new ApplicationManagerImpl();

    private ApplicationManagerImpl() {

    }

    public static ApplicationManagerImpl getInstance() {
        return applicationManager;
    }


    @Override
    public void createApplication(Application application) {

    }

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagerException {
        ConnectionManagerUtil.openConnection();
        ApplicationManagementDAO applicationManagementDAO = ApplicationManagementDataHolder.getInstance().getApplicationManagementDAO();
        ApplicationList applications = applicationManagementDAO.getApplications(filter);
        ConnectionManagerUtil.closeConnection();
        return applications;
    }
}
