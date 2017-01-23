/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.policy.mgt.core.internal;

import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyInformationPoint;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.HashMap;
import java.util.Map;

public class PolicyManagementDataHolder {

    private RealmService realmService;
    private TenantManager tenantManager;
    private PolicyEvaluationPoint policyEvaluationPoint;
    private Map<String, PolicyEvaluationPoint> policyEvaluationPoints = new HashMap<>();
    private PolicyInformationPoint policyInformationPoint;
    private DeviceManagementProviderService deviceManagementService;
    private MonitoringManager monitoringManager;
    private PolicyManager policyManager;
    private TaskService taskService;

    private static PolicyManagementDataHolder thisInstance = new PolicyManagementDataHolder();

    private PolicyManagementDataHolder() {}

    public static PolicyManagementDataHolder getInstance() {
        return thisInstance;
    }

    public PolicyManager getPolicyManager() {
        return policyManager;
    }

    public void setPolicyManager(PolicyManager policyManager) {
        this.policyManager = policyManager;
    }

    public MonitoringManager getMonitoringManager() {
        return monitoringManager;
    }

    public void setMonitoringManager(MonitoringManager monitoringManager) {
        this.monitoringManager = monitoringManager;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
        this.setTenantManager(realmService);
    }

    private void setTenantManager(RealmService realmService) {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        this.tenantManager = realmService.getTenantManager();
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    public PolicyEvaluationPoint getPolicyEvaluationPoint() {
        PolicyConfiguration policyConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getPolicyConfiguration();
        String policyEvaluationPointName = policyConfiguration.getPolicyEvaluationPointName();
        return policyEvaluationPoints.get(policyEvaluationPointName);
    }

    public void setPolicyEvaluationPoint(String name, PolicyEvaluationPoint policyEvaluationPoint) {
        policyEvaluationPoints.put(name,policyEvaluationPoint);
    }

    public void removePolicyEvaluationPoint(PolicyEvaluationPoint policyEvaluationPoint) {
        policyEvaluationPoints.remove(policyEvaluationPoint.getName());
    }


    public PolicyInformationPoint getPolicyInformationPoint() {
        return policyInformationPoint;
    }

    public void setPolicyInformationPoint(PolicyInformationPoint policyInformationPoint) {
        this.policyInformationPoint = policyInformationPoint;
    }

    public DeviceManagementProviderService getDeviceManagementService() {
        return deviceManagementService;
    }

    public void setDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        this.deviceManagementService = deviceManagementService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}
