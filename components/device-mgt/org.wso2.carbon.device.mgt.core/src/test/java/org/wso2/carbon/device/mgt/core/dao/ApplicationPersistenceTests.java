/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;

import java.sql.SQLException;

public class ApplicationPersistenceTests extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(ApplicationPersistenceTests.class);
    private ApplicationDAO applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();

    @Test
    public void testAddApplication() {
        /* Adding dummy application to the application store */
        String testAppIdentifier = "test sample1";
        try {
            DeviceManagementDAOFactory.openConnection();
            applicationDAO.addApplication(TestDataHolder.generateApplicationDummyData(testAppIdentifier), -1234);
        } catch (DeviceManagementDAOException | SQLException e) {
            log.error("Error occurred while adding application test sample1", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        /* Retrieving the application by its name */
        Application target = null;
        try {
            target = this.getApplication(testAppIdentifier, -1234);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving application info";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

        Assert.assertEquals(target.getApplicationIdentifier(), testAppIdentifier, "Application added is not as same as " +
                "what's " +
                "retrieved");
    }

    private Application getApplication(String appIdentifier, int tenantId) throws DeviceManagementDAOException {
        Application application = null;
        try {
            DeviceManagementDAOFactory.openConnection();
            application = applicationDAO.getApplication(appIdentifier, tenantId);
        } catch (SQLException e) {
            log.error("Error occurred while metadata corresponding to the application '" + appIdentifier + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return application;
    }

    @BeforeClass
    @Override
    public void init() throws Exception {
        this.initDataSource();
    }

}
