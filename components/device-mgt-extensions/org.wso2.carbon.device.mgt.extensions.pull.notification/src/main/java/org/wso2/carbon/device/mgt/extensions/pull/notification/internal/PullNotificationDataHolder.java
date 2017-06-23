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
package org.wso2.carbon.device.mgt.extensions.pull.notification.internal;

import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

public class PullNotificationDataHolder {

    private DeviceManagementProviderService deviceManagementProviderService;
    private PolicyManagerService policyManagerService;

    private static PullNotificationDataHolder thisInstance = new PullNotificationDataHolder();

    public static PullNotificationDataHolder getInstance() {
        return thisInstance;
    }

    public DeviceManagementProviderService getDeviceManagementProviderService() {
        return deviceManagementProviderService;
    }

    public void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        this.deviceManagementProviderService = deviceManagementProviderService;
    }

    public PolicyManagerService getPolicyManagerService() {
        return policyManagerService;
    }

    public void setPolicyManagerService(PolicyManagerService policyManagerService) {
        this.policyManagerService = policyManagerService;
    }
}
