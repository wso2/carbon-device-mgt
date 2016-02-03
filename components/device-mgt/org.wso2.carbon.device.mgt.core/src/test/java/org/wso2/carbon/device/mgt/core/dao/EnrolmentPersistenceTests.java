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
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;

import java.sql.SQLException;

public class EnrolmentPersistenceTests extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(EnrolmentPersistenceTests.class);
    private EnrollmentDAO enrollmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();

    @Test
    public void testAddEnrolment() {
        int deviceId = TestDataHolder.initialTestDevice.getId();
        String owner = "admin";

        /* Initializing source enrolment configuration bean to be tested */
        EnrolmentInfo source =
                new EnrolmentInfo(null, owner, EnrolmentInfo.OwnerShip.BYOD,
                        EnrolmentInfo.Status.CREATED);

        /* Adding dummy enrolment configuration to the device management metadata store */
        try {
            DeviceManagementDAOFactory.openConnection();
            enrollmentDAO.addEnrollment(deviceId, source, TestDataHolder.SUPER_TENANT_ID);
        } catch (DeviceManagementDAOException | SQLException e) {
            log.error("Error occurred while adding enrollment", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        /* Retrieving the enrolment associated with the given deviceId and owner */
        EnrolmentInfo target = null;
        try {
            target = this.getEnrolmentConfig(deviceId, owner, TestDataHolder.SUPER_TENANT_ID);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving application info";
            log.error(msg, e);
            Assert.fail(msg, e);
        }

        Assert.assertEquals(target, source, "Enrolment configuration added is not as same as what's retrieved");
    }

    private EnrolmentInfo getEnrolmentConfig(int deviceId, String currentOwner,
                                             int tenantId) throws DeviceManagementDAOException {
        EnrolmentInfo enrolmentInfo = null;
        try {
            DeviceManagementDAOFactory.openConnection();
            enrolmentInfo = enrollmentDAO.getEnrollment(deviceId, currentOwner, tenantId);
        } catch (SQLException e) {
            log.error("Error occurred while retrieving enrolment corresponding to device id '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return enrolmentInfo;
    }

    @BeforeClass
    @Override
    public void init() throws Exception {
        this.initDataSource();
    }

}
