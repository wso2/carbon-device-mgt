package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.feature;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationBasedFeatureManager implements FeatureManager {
    private List<Feature> features = new ArrayList<>();

    public ConfigurationBasedFeatureManager(List<org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Feature> features) {
        for (org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Feature feature : features) {
            Feature deviceFeature = new Feature();
            deviceFeature.setCode(feature.getCode());
            deviceFeature.setName(feature.getName());
            deviceFeature.setDescription(feature.getDescription());
            this.features.add(deviceFeature);
        }
    }
    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean addFeatures(List<Feature> features) throws DeviceManagementException {
        return false;
    }

    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
        Feature extractedFeature = null;
        for (Feature feature : features) {
            if (feature.getName().equalsIgnoreCase(name)) {
                extractedFeature = feature;
            }
        }
        return extractedFeature;
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
        return features;
    }

    @Override
    public boolean removeFeature(String name) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean addSupportedFeaturesToDB() throws DeviceManagementException {
        return false;
    }
}
