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


package org.wso2.carbon.device.mgt.common.policy.mgt.monitor;

import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;

import java.sql.Timestamp;
import java.util.List;

public class NonComplianceData {

    private int id;
    private int deviceId;
    private int enrolmentId;
    private int policyId;
    List<ComplianceFeature> complianceFeatures;
    private boolean status;
    private Timestamp lastRequestedTime;
    private Timestamp lastSucceededTime;
    private Timestamp lastFailedTime;
    private int attempts;
    private String message;

    /**
     * This parameter is to inform the policy core, weather related device type plugins does need the full policy or
     * the  part which is none compliance.
     */
    private boolean completePolicy;
    private Policy policy;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEnrolmentId() {
        return enrolmentId;
    }

    public void setEnrolmentId(int enrolmentId) {
        this.enrolmentId = enrolmentId;
    }

    public Timestamp getLastRequestedTime() {
        return lastRequestedTime;
    }

    public void setLastRequestedTime(Timestamp lastRequestedTime) {
        this.lastRequestedTime = lastRequestedTime;
    }

    public Timestamp getLastSucceededTime() {
        return lastSucceededTime;
    }

    public void setLastSucceededTime(Timestamp lastSucceededTime) {
        this.lastSucceededTime = lastSucceededTime;
    }

    public Timestamp getLastFailedTime() {
        return lastFailedTime;
    }

    public void setLastFailedTime(Timestamp lastFailedTime) {
        this.lastFailedTime = lastFailedTime;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public List<ComplianceFeature> getComplianceFeatures() {
        return complianceFeatures;
    }

    public void setComplianceFeatures(List<ComplianceFeature> complianceFeatures) {
        this.complianceFeatures = complianceFeatures;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCompletePolicy() {
        return completePolicy;
    }

    public void setCompletePolicy(boolean completePolicy) {
        this.completePolicy = completePolicy;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }
}
