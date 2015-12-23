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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericFeatureManager {

    private static final Log log = LogFactory.getLog(GenericFeatureManager.class);
    private static Map<String,List<Feature>> featureSet = null;

    private static GenericFeatureManager instance = null;

    private GenericFeatureManager() {
        synchronized (this) {
            featureSet = new HashMap<String,List<Feature>>();
        }
    }

    public static GenericFeatureManager getInstance(){
        if(instance==null){
            instance = new GenericFeatureManager();
        }
        return instance;
    }

    public boolean addFeature(Feature feature) throws DeviceManagementException {
        throw new DeviceManagementException("Adding of individual features is not supported");
    }

    public boolean addFeatures(Map<String,List<Feature>> freshFeatures) throws DeviceManagementException {
        this.featureSet.putAll(freshFeatures);
        return true;
    }

    public Feature getFeature(String deviceType, String featureCode) throws DeviceManagementException {
        Feature extractedFeature = null;
        List<Feature> deviceFeatureList = featureSet.get(deviceType);
        for(Feature feature : deviceFeatureList){
            if(feature.getCode().equalsIgnoreCase(featureCode)){
                extractedFeature = feature;
            }
        }
        return extractedFeature;
    }

    public List<Feature> getFeatures(String deviceType) throws DeviceManagementException {
        return featureSet.get(deviceType);
    }

}