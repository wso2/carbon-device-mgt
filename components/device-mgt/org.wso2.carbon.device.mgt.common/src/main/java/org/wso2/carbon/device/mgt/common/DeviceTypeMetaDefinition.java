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

package org.wso2.carbon.device.mgt.common;

import java.io.Serializable;
import java.util.List;

/**
 * Bean class for DeviceTypeMetaDefinition
 */
public class DeviceTypeMetaDefinition implements Serializable {

    private static final long serialVersionUID = -986985489431990410L;

    private List<String> properties;
    private List<Feature> features;
    private boolean claimable;
    private PushNotificationConfig pushNotificationConfig;
    //    private boolean policyMonitoringEnabled;
//    private InitialOperationConfig initialOperationConfig;
    private License license;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public boolean isClaimable() {
        return claimable;
    }

    public void setClaimable(boolean isClaimable) {
        this.claimable = isClaimable;
    }

    public PushNotificationConfig getPushNotificationConfig() {
        return pushNotificationConfig;
    }

    public void setPushNotificationConfig(
            PushNotificationConfig pushNotificationConfig) {
        this.pushNotificationConfig = pushNotificationConfig;
    }
//
//    public boolean isPolicyMonitoringEnabled() {
//        return policyMonitoringEnabled;
//    }
//
//    public void setPolicyMonitoringEnabled(boolean policyMonitoringEnabled) {
//        this.policyMonitoringEnabled = policyMonitoringEnabled;
//    }
//
//    public InitialOperationConfig getInitialOperationConfig() {
//        return initialOperationConfig;
//    }
//
//    public void setInitialOperationConfig(InitialOperationConfig initialOperationConfig) {
//        this.initialOperationConfig = initialOperationConfig;
//    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public DeviceTypeMetaDefinition getDeviceTypeMetaDefinition() {
        DeviceTypeMetaDefinition deviceTypeMetaDefinition = new DeviceTypeMetaDefinition();
        deviceTypeMetaDefinition.setProperties(properties);
        deviceTypeMetaDefinition.setFeatures(features);
        deviceTypeMetaDefinition.setClaimable(claimable);
        deviceTypeMetaDefinition.setPushNotificationConfig(pushNotificationConfig);
//        deviceTypeMetaDefinition.setPolicyMonitoringEnabled(policyMonitoringEnabled);
//        deviceTypeMetaDefinition.setInitialOperationConfig(initialOperationConfig);
        deviceTypeMetaDefinition.setLicense(license);
        deviceTypeMetaDefinition.setDescription(description);
        return deviceTypeMetaDefinition;
    }
}
