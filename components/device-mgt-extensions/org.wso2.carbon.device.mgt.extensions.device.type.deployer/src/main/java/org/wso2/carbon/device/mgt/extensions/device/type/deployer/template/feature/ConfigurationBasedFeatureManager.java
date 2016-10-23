/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.feature;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This implementation retreives the features that are configured through the deployer.
 */
public class ConfigurationBasedFeatureManager implements FeatureManager {
    private List<Feature> features = new ArrayList<>();

    public ConfigurationBasedFeatureManager(
            List<org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Feature> features) {
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
