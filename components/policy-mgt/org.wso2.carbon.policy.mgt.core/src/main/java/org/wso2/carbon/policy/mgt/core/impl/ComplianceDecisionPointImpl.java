/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.policy.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.EnrolmentDAO;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceDecisionPoint;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;

public class ComplianceDecisionPointImpl implements ComplianceDecisionPoint {

    private static final Log log = LogFactory.getLog(ComplianceDecisionPointImpl.class);

    private EnrolmentDAO enrolmentDAO;
    private DeviceDAO deviceDAO;

    private PolicyManager policyManager;

    public ComplianceDecisionPointImpl() {
        enrolmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        policyManager = new PolicyManagerImpl();
    }

    @Override
    public String getNoneComplianceRule(Policy policy) throws PolicyComplianceException {
        return policy.getCompliance();
    }

    @Override
    public void setDeviceAsUnreachable(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

    }

    @Override
    public void setDeviceAsReachable(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

    }

    @Override
    public void reEnforcePolicy(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

    }

    @Override
    public void markDeviceAsNoneCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

    }

    @Override
    public void markDeviceAsCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

    }

    @Override
    public void deactivateDevice(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

    }

    @Override
    public void activateDevice(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {

    }

    @Override
    public void validateDevicePolicyCompliance(DeviceIdentifier deviceIdentifier, Policy policy) throws
            PolicyComplianceException {

    }
}
