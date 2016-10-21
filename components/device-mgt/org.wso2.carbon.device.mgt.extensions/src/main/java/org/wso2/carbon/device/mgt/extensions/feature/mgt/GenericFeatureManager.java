/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.extensions.feature.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceTypeIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This stores the features for device types that are mentioned through the annotations.
 */
public class GenericFeatureManager {

    private static final Log log = LogFactory.getLog(GenericFeatureManager.class);
    private static Map<DeviceTypeIdentifier, List<Feature>> featureSet = new HashMap<>();
    private static GenericFeatureManager instance = new GenericFeatureManager();

    private GenericFeatureManager() {
    }

    public static GenericFeatureManager getInstance() {
        return instance;
    }

    /**
     * @param deviceTypeFeatures feature list for each device type.
     */
    public void addFeatures(Map<DeviceTypeIdentifier, List<Feature>> deviceTypeFeatures) {
        this.featureSet.putAll(deviceTypeFeatures);
    }

    /**
     * @param deviceType
     * @param featureName
     * @return the extracted feature for the which matches the feature name and device type.
     */
    public Feature getFeature(DeviceTypeIdentifier deviceType, String featureName) {
        Feature extractedFeature = null;
        List<Feature> deviceFeatureList = featureSet.get(deviceType);
        for (Feature feature : deviceFeatureList) {
            if (feature.getName().equalsIgnoreCase(featureName)) {
                extractedFeature = feature;
            }
        }
        return extractedFeature;
    }

    /**
     * @param deviceType returns the features for the device type.
     * @return
     */
    public List<Feature> getFeatures(DeviceTypeIdentifier deviceType) {
        return featureSet.get(deviceType);
    }

}