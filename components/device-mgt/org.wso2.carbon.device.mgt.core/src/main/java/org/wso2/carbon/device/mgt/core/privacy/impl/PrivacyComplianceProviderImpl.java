/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.core.privacy.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.PrivacyComplianceException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.privacy.PrivacyComplianceProvider;
import org.wso2.carbon.device.mgt.core.privacy.dao.PrivacyComplianceDAO;
import org.wso2.carbon.device.mgt.core.privacy.dao.PrivacyComplianceDAOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivacyComplianceProviderImpl implements PrivacyComplianceProvider {

    private static final Log log = LogFactory.getLog(PrivacyComplianceProviderImpl.class);

    PrivacyComplianceDAO complianceDAO;

    public PrivacyComplianceProviderImpl() {
        complianceDAO = DeviceManagementDAOFactory.getPrivacyComplianceDAO();
    }

    @Override
    public void deleteDevicesOfUser(String username) throws PrivacyComplianceException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting the requested users.");
        }
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            List<DeviceEnrollmentMapping> enrollmentMappings = complianceDAO.getDevicesOfUser(username, tenantId);
            if(enrollmentMappings == null || enrollmentMappings.isEmpty()){
                log.info("No enrolments found with the user..!");
                return;
            }
            Map<Integer, List<Integer>> deviceMap = new HashMap<>();
            int x = -1;
            for (DeviceEnrollmentMapping m : enrollmentMappings) {
                if (m.getDeviceId() != x) {
                    x = m.getDeviceId();
                    List<Integer> enrolments = new ArrayList<>();
                    enrolments.add(m.getEnrolmentId());
                    deviceMap.put(m.getDeviceId(), enrolments);
                } else {
                    deviceMap.get(m.getDeviceId()).add(m.getEnrolmentId());
                }
            }
            for (int deviceId : deviceMap.keySet()) {
                List<Integer> enrollmentIds = deviceMap.get(deviceId);
                for (Integer enrolmentId : enrollmentIds) {
                    complianceDAO.deleteDeviceOperationDetails(enrolmentId);
                    complianceDAO.deleteOperationEnrolmentMappings(enrolmentId);
                    complianceDAO.deleteDeviceApplications(deviceId, enrolmentId, tenantId);
                    complianceDAO.deleteDeviceDetails(deviceId, enrolmentId);
                    complianceDAO.deleteDeviceProperties(deviceId, enrolmentId, tenantId);
                    complianceDAO.deleteDeviceLocation(deviceId, enrolmentId);
                    complianceDAO.deleteDeviceEnrollments(deviceId, tenantId);
                }
                complianceDAO.deleteDevice(deviceId, tenantId);
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (PrivacyComplianceDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting the devices and details of the given user";
            log.error(msg, e);
            throw new PrivacyComplianceException(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Database error occurred while deleting the devices and details of the given user";
            log.error(msg, e);
            throw new PrivacyComplianceException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        log.info("Requested users device has been successfully removed..!");
    }

    @Override
    public void deleteDeviceDetails(DeviceIdentifier deviceIdentifier) throws PrivacyComplianceException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting the requested device details.");
        }
        try {
            Device device = this.getDevice(deviceIdentifier);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            complianceDAO.deleteDeviceOperationDetails(device.getEnrolmentInfo().getId());
            complianceDAO.deleteOperationEnrolmentMappings(device.getEnrolmentInfo().getId());
            complianceDAO.deleteDeviceApplications(device.getId(), device.getEnrolmentInfo().getId(), tenantId);
            complianceDAO.deleteDeviceDetails(device.getId(), device.getEnrolmentInfo().getId());
            complianceDAO.deleteDeviceProperties(device.getId(), device.getEnrolmentInfo().getId(), tenantId);
            complianceDAO.deleteDeviceLocation(device.getId(), device.getEnrolmentInfo().getId());
            complianceDAO.deleteDeviceEnrollments(device.getId(), tenantId);
            complianceDAO.deleteDevice(device.getId(), tenantId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Database error occurred while deleting the device details.";
            log.error(msg, e);
            throw new PrivacyComplianceException(msg, e);
        } catch (PrivacyComplianceDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting the device details.";
            log.error(msg, e);
            throw new PrivacyComplianceException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }


    }

    private Device getDevice(DeviceIdentifier deviceId) throws PrivacyComplianceException {
        try {
            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId, false);
        } catch (DeviceManagementException e) {
            throw new PrivacyComplianceException(
                    "Error occurred while retrieving device info.", e);
        }
    }
}

