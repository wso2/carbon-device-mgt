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
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;

import java.util.Date;

public class EnrolmentPersistenceDAOTests extends BaseDeviceManagementDAOTest {

    private static final Log log = LogFactory.getLog(EnrolmentPersistenceDAOTests.class);
    private EnrolmentDAO enrolmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();

    @Test
    public void testAddEnrolment() {
        int deviceId = 1234;
        String owner = "admin";

        /* Initializing source enrolment configuration bean to be tested */
        EnrolmentInfo source =
                new EnrolmentInfo(null, owner, EnrolmentInfo.OwnerShip.BYOD,
                        EnrolmentInfo.Status.CREATED);

        /* Adding dummy enrolment configuration to the device management metadata store */
        try {
            DeviceManagementDAOFactory.openConnection();
            enrolmentDAO.addEnrollment(deviceId, source, -1234);
        } catch (DeviceManagementDAOException e) {
            log.error("Error occurred while adding enrollment", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        /* Retrieving the enrolment associated with the given deviceId and owner */
        EnrolmentInfo target = null;
        try {
            target = this.getEnrolmentConfig(deviceId, owner, -1234);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving application info";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

        Assert.assertEquals(target, source, "Enrolment configuration added is not as same as what's retrieved");
    }

    private EnrolmentInfo getEnrolmentConfig(int deviceId, String currentOwner,
                                             int tenantId) throws DeviceManagementDAOException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return enrolmentDAO.getEnrolment(deviceId, currentOwner, tenantId);
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
