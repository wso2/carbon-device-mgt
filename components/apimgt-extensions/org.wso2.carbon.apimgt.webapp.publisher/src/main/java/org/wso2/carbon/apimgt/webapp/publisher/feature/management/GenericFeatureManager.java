/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.webapp.publisher.feature.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;

import java.util.ArrayList;
import java.util.List;

public class GenericFeatureManager implements FeatureManager {

    private static final Log log = LogFactory.getLog(GenericFeatureManager.class);
    private static List<Feature> featureSet = null;

    public GenericFeatureManager(List<Feature> features) {
        synchronized (this) {
            featureSet = features;
        }
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        throw new DeviceManagementException("Adding of features is not supported");
    }

    @Override
    public boolean addFeatures(List<Feature> features) throws DeviceManagementException {
        throw new DeviceManagementException("Features added at the time of instantiation");
    }

    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
        Feature extractedFeature = null;
        for(Feature feature : featureSet){
            if(feature.getCode().equalsIgnoreCase(name)){
                extractedFeature = feature;
            }
        }
        return extractedFeature;
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
        return featureSet;
    }

    @Override
    public boolean removeFeature(String code) throws DeviceManagementException {
        throw new DeviceManagementException("Removing of features is not supported");
    }

    @Override
    public boolean addSupportedFeaturesToDB() throws DeviceManagementException {
        throw new DeviceManagementException("Features added at the time of instantiation");
    }

}