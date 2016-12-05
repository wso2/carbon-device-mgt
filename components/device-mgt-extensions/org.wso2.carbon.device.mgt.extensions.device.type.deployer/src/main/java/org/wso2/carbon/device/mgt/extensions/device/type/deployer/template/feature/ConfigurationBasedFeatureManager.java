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
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Operation;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This implementation retreives the features that are configured through the deployer.
 */
public class ConfigurationBasedFeatureManager implements FeatureManager {
    private List<Feature> features = new ArrayList<>();
    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String CONTENT_TYPE = "contentType";
    private static final String PATH_PARAMS = "pathParams";
    private static final String QUERY_PARAMS = "queryParams";
    private static final String FORM_PARAMS = "formParams";
    private static final Pattern PATH_PARAM_REGEX = Pattern.compile("\\{(.*?)\\}");

    public ConfigurationBasedFeatureManager(
            List<org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Feature> features) {
        for (org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Feature feature : features) {
            Feature deviceFeature = new Feature();
            deviceFeature.setCode(feature.getCode());
            deviceFeature.setName(feature.getName());
            deviceFeature.setDescription(feature.getDescription());
            Operation operation = feature.getOperation();
            if (operation != null) {
                Map<String, Object> apiParams = new HashMap<>();
                apiParams.put(METHOD, operation.getMethod().toUpperCase());
                apiParams.put(URI, operation.getContext());
                apiParams.put(CONTENT_TYPE, operation.getType());
                List<String> pathParams = new ArrayList<>();
                List<String> queryParams = new ArrayList<>();
                List<String> formParams = new ArrayList<>();
                setPathParams(operation.getContext(), pathParams);
                apiParams.put(PATH_PARAMS, pathParams);
                if (operation.getQueryParameters() != null) {
                    queryParams = operation.getQueryParameters().getParameter();
                }
                apiParams.put(QUERY_PARAMS, queryParams);
                if (operation.getFormParameters() != null) {
                    formParams = operation.getFormParameters().getParameter();
                }
                apiParams.put(FORM_PARAMS, formParams);
                List<Feature.MetadataEntry> metadataEntries = new ArrayList<>();
                Feature.MetadataEntry metadataEntry = new Feature.MetadataEntry();
                metadataEntry.setId(-1);
                metadataEntry.setValue(apiParams);
                metadataEntries.add(metadataEntry);
                deviceFeature.setMetadataEntries(metadataEntries);
            }
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

    private void setPathParams(String context, List<String> pathParams) {
        Matcher regexMatcher = PATH_PARAM_REGEX.matcher(context);
        while (regexMatcher.find()) {
            pathParams.add(regexMatcher.group(1));
        }
    }
}
