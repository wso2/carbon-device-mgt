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


package org.wso2.carbon.policy.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyDeviceWrapper;

import java.util.List;
import java.util.Map;

public interface MonitoringDAO {

    int addComplianceDetails(int deviceId, int policyId) throws MonitoringDAOException;

    /**
     * This is getting a list of values with device id and applied policy
     * @param devicePolicyMap <Device Id, Policy Id>
     * @throws MonitoringDAOException
     */

    @Deprecated
    void addComplianceDetails(Map<Integer, Integer> devicePolicyMap) throws MonitoringDAOException;

    void addComplianceDetails(List<PolicyDeviceWrapper> policyDeviceWrappers) throws MonitoringDAOException;

    void setDeviceAsNoneCompliance(int deviceId, int enrolmentId, int policyId) throws MonitoringDAOException;

    void setDeviceAsCompliance(int deviceId, int enrolmentId, int policyId) throws MonitoringDAOException;

    void addNonComplianceFeatures(int policyComplianceStatusId, int deviceId, List<ComplianceFeature>
            complianceFeatures)
            throws MonitoringDAOException;

    NonComplianceData getCompliance(int deviceId, int enrolmentId) throws MonitoringDAOException;

    List<NonComplianceData> getCompliance(List<Integer> deviceIds) throws MonitoringDAOException;

    List<NonComplianceData> getCompliance() throws MonitoringDAOException;

    List<ComplianceFeature> getNoneComplianceFeatures(int policyComplianceStatusId) throws MonitoringDAOException;

    void deleteNoneComplianceData(int policyComplianceStatusId) throws MonitoringDAOException;

    boolean updateAttempts(int deviceId, boolean reset) throws MonitoringDAOException;

    void updateAttempts(List<Integer> deviceId, boolean reset) throws MonitoringDAOException;

}