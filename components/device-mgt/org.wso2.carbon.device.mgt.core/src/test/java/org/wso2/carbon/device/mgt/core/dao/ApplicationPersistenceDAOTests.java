/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;

import java.util.Properties;

public class ApplicationPersistenceDAOTests extends BaseDeviceManagementDAOTest {

    private static final Log log = LogFactory.getLog(ApplicationPersistenceDAOTests.class);
    private ApplicationDAO applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();

    @Test
    public void testAddApplication() {
        /* Initializing source application bean to be tested */
        Properties properties = new Properties();
        Application source = new Application();
        source.setName("SimpleCalculator");
        source.setCategory("TestCategory");
        source.setApplicationIdentifier("com.simple.calculator");
        source.setType("TestType");
        source.setVersion("1.0.0");
        source.setImageUrl("http://test.org/image/");
        source.setLocationUrl("http://test.org/location/");

        /* Adding dummy application to the application store */
        try {
            DeviceManagementDAOFactory.openConnection();
            applicationDAO.addApplication(source, -1234);
        } catch (DeviceManagementDAOException e) {
            log.error("Error occurred while adding application '" + source.getName() + "'", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        /* Retrieving the application by its name */
        Application target = null;
        try {
            target = this.getApplication(source.getApplicationIdentifier(), -1234);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving application info";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

        Assert.assertEquals(target.getApplicationIdentifier(), source.getApplicationIdentifier(), "Application added is not as same as " +
                "what's " +
                "retrieved");
    }

    private Application getApplication(String packageName, int tenantId) throws DeviceManagementDAOException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return applicationDAO.getApplication(packageName, tenantId);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing connection", e);
            }
        }
    }

    @BeforeClass
    @Override
    public void init() throws Exception {
        this.initDatSource();
    }
}
