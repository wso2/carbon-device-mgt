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

package org.wso2.carbon.device.mgt.devicetype.api.mapper;

import org.wso2.carbon.device.mgt.devicetype.api.dto.DeviceType;
import org.wso2.carbon.device.mgt.devicetype.api.dto.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.devicetype.api.dto.Feature;
import org.wso2.carbon.device.mgt.devicetype.api.dto.MetadataEntry;
import org.wso2.carbon.device.mgt.devicetype.api.dto.PushNotificationConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Use this class for mapping model classes into JAX-RS beans.
 */
public class ModelMapper {

    public static DeviceType map(org.wso2.carbon.device.mgt.common.DeviceType deviceType) {
        DeviceType rv = new DeviceType();
        rv.setId(deviceType.getId());
        rv.setName(deviceType.getName());
        rv.setDeviceTypeMetaDefinition(map(deviceType.getDeviceTypeMetaDefinition()));
        return rv;
    }

    public static DeviceTypeMetaDefinition map(
            org.wso2.carbon.device.mgt.common.DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        DeviceTypeMetaDefinition rv = new DeviceTypeMetaDefinition();
        rv.setProperties(deviceTypeMetaDefinition.getProperties());
        List<Feature> features = new ArrayList<>();
        deviceTypeMetaDefinition.getFeatures().forEach(feature -> {
            features.add(map(feature));
        });
        rv.setFeatures(features);
        rv.setClaimable(deviceTypeMetaDefinition.isClaimable());
        rv.setPushNotificationConfig(
                map(deviceTypeMetaDefinition.getPushNotificationConfig())
        );
//        rv.setInitialOperationConfig(deviceTypeMetaDefinition.getInitialOperationConfig());
        return rv;
    }

    public static Feature map(org.wso2.carbon.device.mgt.common.Feature feature) {
        Feature rv = new Feature();
        rv.setId(feature.getId());
        rv.setCode(feature.getCode());
        rv.setDescription(feature.getDescription());
        rv.setDeviceType(feature.getDeviceType());
        List<MetadataEntry> metadataEntries = new ArrayList<>();
        feature.getMetadataEntries().forEach(metadataEntry -> {
            metadataEntries.add(map(metadataEntry));
        });
        rv.setMetadataEntries(metadataEntries);
        return rv;
    }

    public static MetadataEntry map(
            org.wso2.carbon.device.mgt.common.Feature.MetadataEntry metadataEntry) {
        MetadataEntry rv = new MetadataEntry();
        rv.setId(metadataEntry.getId());
        rv.setValue(metadataEntry.getValue());
        return rv;
    }

    private static PushNotificationConfig map(
            org.wso2.carbon.device.mgt.common.PushNotificationConfig pushNotificationConfig) {
        PushNotificationConfig rv = new PushNotificationConfig();
        rv.setType(pushNotificationConfig.getType());
        rv.setProperties(pushNotificationConfig.getProperties());
        rv.setIsScheduled(pushNotificationConfig.isScheduled());
        return rv;
    }

    public static org.wso2.carbon.device.mgt.common.DeviceType map(DeviceType deviceType) {
        org.wso2.carbon.device.mgt.common.DeviceType rv = new org.wso2.carbon.device.mgt.common.DeviceType();
        rv.setId(deviceType.getId());
        rv.setName(deviceType.getName());
        rv.setDeviceTypeMetaDefinition(map(deviceType.getDeviceTypeMetaDefinition()));
        return rv;
    }

    private static org.wso2.carbon.device.mgt.common.DeviceTypeMetaDefinition map(
            DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        org.wso2.carbon.device.mgt.common.DeviceTypeMetaDefinition rv =
                new org.wso2.carbon.device.mgt.common.DeviceTypeMetaDefinition();
        rv.setProperties(deviceTypeMetaDefinition.getProperties());
        List<org.wso2.carbon.device.mgt.common.Feature> features = new ArrayList<>();
        deviceTypeMetaDefinition.getFeatures().forEach(feature -> {
            features.add(map(feature));
        });
        rv.setFeatures(features);
        rv.setClaimable(deviceTypeMetaDefinition.getClaimable());
        rv.setPushNotificationConfig(
                map(deviceTypeMetaDefinition.getPushNotificationConfig())
        );
//        rv.setInitialOperationConfig(deviceTypeMetaDefinition.getInitialOperationConfig());
        return rv;
    }

    private static org.wso2.carbon.device.mgt.common.PushNotificationConfig map(
            PushNotificationConfig pushNotificationConfig) {
        return new org.wso2.carbon.device.mgt.common.PushNotificationConfig(pushNotificationConfig.getType(),
                                                                            pushNotificationConfig.getIsScheduled(),
                                                                            pushNotificationConfig.getProperties());
    }

    private static org.wso2.carbon.device.mgt.common.Feature map(Feature feature) {
        org.wso2.carbon.device.mgt.common.Feature rv = new org.wso2.carbon.device.mgt.common.Feature();
        rv.setId(feature.getId());
        rv.setCode(feature.getCode());
        rv.setDescription(feature.getDescription());
        rv.setDeviceType(feature.getDeviceType());
        List<org.wso2.carbon.device.mgt.common.Feature.MetadataEntry> metadataEntries = new ArrayList<>();
        feature.getMetadataEntries().forEach(metadataEntry -> {
            metadataEntries.add(map(metadataEntry));
        });
        rv.setMetadataEntries(metadataEntries);
        return rv;
    }

    private static org.wso2.carbon.device.mgt.common.Feature.MetadataEntry map(MetadataEntry metadataEntry) {
        org.wso2.carbon.device.mgt.common.Feature.MetadataEntry rv =
                new org.wso2.carbon.device.mgt.common.Feature.MetadataEntry();
        rv.setId(metadataEntry.getId());
        rv.setValue(metadataEntry.getValue());
        return rv;
    }


}
