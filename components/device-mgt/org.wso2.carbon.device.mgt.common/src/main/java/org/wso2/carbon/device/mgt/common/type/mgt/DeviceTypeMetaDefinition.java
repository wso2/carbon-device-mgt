package org.wso2.carbon.device.mgt.common.type.mgt;

import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.InitialOperationConfig;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;

import java.util.List;

public class DeviceTypeMetaDefinition {

    private List<String> properties;
    private List<Feature> features;
    private boolean claimable;
    private List<PushNotificationConfig> pushNotificationConfigs;
    private boolean policyMonitoringEnabled;
    private InitialOperationConfig initialOperationConfig;
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

    public List<PushNotificationConfig> getPushNotificationConfigs() {
        return pushNotificationConfigs;
    }

    public void setPushNotificationConfigs(
            List<PushNotificationConfig> pushNotificationConfigs) {
        this.pushNotificationConfigs = pushNotificationConfigs;
    }

    public boolean isPolicyMonitoringEnabled() {
        return policyMonitoringEnabled;
    }

    public void setPolicyMonitoringEnabled(boolean policyMonitoringEnabled) {
        this.policyMonitoringEnabled = policyMonitoringEnabled;
    }

    public InitialOperationConfig getInitialOperationConfig() {
        return initialOperationConfig;
    }

    public void setInitialOperationConfig(InitialOperationConfig initialOperationConfig) {
        this.initialOperationConfig = initialOperationConfig;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }
}
