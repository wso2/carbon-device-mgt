/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.config;

import org.wso2.carbon.device.mgt.core.config.identity.IdentityConfigurations;
import org.wso2.carbon.device.mgt.core.config.pagination.PaginationConfiguration;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.device.mgt.core.config.push.notification.PushNotificationConfiguration;
import org.wso2.carbon.device.mgt.core.config.task.TaskConfiguration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Represents Device Mgt configuration.
 */
@XmlRootElement(name = "DeviceMgtConfiguration")
@SuppressWarnings("unused")
public final class DeviceManagementConfig {

    private DeviceManagementConfigRepository deviceManagementConfigRepository;
    private TaskConfiguration taskConfiguration;
    private IdentityConfigurations identityConfigurations;
    private PolicyConfiguration policyConfiguration;
    private PaginationConfiguration paginationConfiguration;
    private PushNotificationConfiguration pushNotificationConfiguration;


    @XmlElement(name = "ManagementRepository", required = true)
    public DeviceManagementConfigRepository getDeviceManagementConfigRepository() {
        return deviceManagementConfigRepository;
    }

    public void setDeviceManagementConfigRepository(DeviceManagementConfigRepository deviceManagementConfigRepository) {
        this.deviceManagementConfigRepository = deviceManagementConfigRepository;
    }

    @XmlElement(name = "IdentityConfiguration", required = true)
    public IdentityConfigurations getIdentityConfigurations() {
        return identityConfigurations;
    }


    public void setIdentityConfigurations(IdentityConfigurations identityConfigurations) {
        this.identityConfigurations = identityConfigurations;
    }

    @XmlElement(name = "PolicyConfiguration", required = true)
    public PolicyConfiguration getPolicyConfiguration() {
        return policyConfiguration;
    }

    public void setPolicyConfiguration(PolicyConfiguration policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    @XmlElement(name = "TaskConfiguration", required = true)
    public TaskConfiguration getTaskConfiguration() {
        return taskConfiguration;
    }

    public void setTaskConfiguration(TaskConfiguration taskConfiguration) {
        this.taskConfiguration = taskConfiguration;
    }

    @XmlElement(name = "PaginationConfiguration", required = true)
    public PaginationConfiguration getPaginationConfiguration() {
        return paginationConfiguration;
    }

    public void setPaginationConfiguration(PaginationConfiguration paginationConfiguration) {
        this.paginationConfiguration = paginationConfiguration;
    }

    @XmlElement(name = "PushNotificationConfiguration", required = true)
    public PushNotificationConfiguration getPushNotificationConfiguration() {
        return pushNotificationConfiguration;
    }

    public void setPushNotificationConfiguration(PushNotificationConfiguration pushNotificationConfiguration) {
        this.pushNotificationConfiguration = pushNotificationConfiguration;
    }
}

