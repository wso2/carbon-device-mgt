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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.sensor.mgt;

import java.io.Serializable;
import java.util.Map;

/**
 * Class definition for Sensor type which is a template for actual sensors attached to various devices. This template
 * defines the common attributes and properties of all sensors falling under the family of a single SensorType.
 * Ex: A Camera in an AndroidPhone, iPhone, WindowsPhone or a RaspberryPi device all falls under a single SensorType
 * "CAMERA" but may in itself have separate properties.
 */
public class SensorType implements Serializable {
    private static final long serialVersionUID = -3151279311229073230L;

    private String typeID;
    private String typeName;
    private String typeTAG;
    private String description;
    private Map<String, Object> typeProperties;

    private String streamDefinitionVersion;
    private Map<String, String> metaData;
    private Map<String, String> correlationData;
    private Map<String, String> payloadData;
    private String streamDefinition;

    public String getTypeID() {
        return typeID;
    }

    public void setTypeID(String typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeTAG() {
        return typeTAG;
    }

    public void setTypeTAG(String typeTAG) {
        this.typeTAG = typeTAG;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getTypeProperties() {
        return typeProperties;
    }

    public void setTypeProperties(Map<String, Object> typeProperties) {
        this.typeProperties = typeProperties;
    }

    public String getStreamDefinitionVersion() {
        return streamDefinitionVersion;
    }

    public void setStreamDefinitionVersion(String streamDefinitionVersion) {
        this.streamDefinitionVersion = streamDefinitionVersion;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public Map<String, String> getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(Map<String, String> correlationData) {
        this.correlationData = correlationData;
    }

    public Map<String, String> getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(Map<String, String> payloadData) {
        this.payloadData = payloadData;
    }

    public String getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(String streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    /**
     * Method to construct and set the stream definition specific to this SensorType based on the meta, correlation,
     * payload properties added to this SensorType definition.
     */
    public void buildStreamDefinition() {
        String metaNameTypePairs = "";
        String correlationNameTypePairs = "";
        String payloadNameTypePairs = "";

        String metaDataDefinition = "";
        String correlationDataDefinition = "";
        String payloadDataDefinition = "";

        if (metaData != null && metaData.size() > 0) {
            for (String metaInfoName : metaData.keySet()) {
                String metaInfoType = metaData.get(metaInfoName);
                String nameTypePair = String.format(
                        SensorStreamDefinitionConstants.STREAM_DEF_NAME_TYPE_PAIR, metaInfoName, metaInfoType);
                metaNameTypePairs = nameTypePair + ",";
            }
            metaNameTypePairs = metaNameTypePairs.substring(0, metaNameTypePairs.length() - 1);
            metaDataDefinition = String.format(SensorStreamDefinitionConstants.STREAM_DEF_META_TAG, metaNameTypePairs);
        }

        if (correlationData != null && correlationData.size() > 0) {
            for (String correlationInfoName : correlationData.keySet()) {
                String correlationInfoType = correlationData.get(correlationInfoName);
                String nameTypePair = String.format(
                        SensorStreamDefinitionConstants.STREAM_DEF_NAME_TYPE_PAIR, correlationInfoName,
                        correlationInfoType);
                correlationNameTypePairs = nameTypePair + ",";
            }
            correlationNameTypePairs = correlationNameTypePairs.substring(0, correlationNameTypePairs.length() - 1);
            correlationDataDefinition = String.format(
                    SensorStreamDefinitionConstants.STREAM_DEF_CORRELATION_TAG, correlationNameTypePairs);
        }

        if (payloadData != null && payloadData.size() > 0) {
            for (String payloadInfoName : payloadData.keySet()) {
                String payloadInfoType = payloadData.get(payloadInfoName);
                String nameTypePair = String.format(
                        SensorStreamDefinitionConstants.STREAM_DEF_NAME_TYPE_PAIR, payloadInfoName, payloadInfoType);
                payloadNameTypePairs = nameTypePair + ",";
            }
            payloadNameTypePairs = payloadNameTypePairs.substring(0, payloadNameTypePairs.length() - 1);
            payloadDataDefinition = String.format(
                    SensorStreamDefinitionConstants.STREAM_DEF_PAYLOAD_TAG, payloadNameTypePairs);
        }

        String streamDefinitionName =
                SensorStreamDefinitionConstants.DEFAULT_STREAM_DEF_NAME_PREFIX + typeName.toLowerCase();
        String streamDefinitionNickname = typeName + "-" + typeTAG;

        streamDefinition = String.format(SensorStreamDefinitionConstants.STREAM_DEF_NAME_TAG, streamDefinitionName) +
                String.format(SensorStreamDefinitionConstants.STREAM_DEF_VERSION_TAG, streamDefinitionVersion) +
                String.format(SensorStreamDefinitionConstants.STREAM_DEF_NICKNAME_TAG, streamDefinitionNickname) +
                String.format(SensorStreamDefinitionConstants.STREAM_DEF_DESCRIPTION_TAG, description);

        if (!metaDataDefinition.equals("")) {
            streamDefinition += metaDataDefinition;
        }

        if (!correlationDataDefinition.equals("")) {
            streamDefinition += correlationDataDefinition;
        }

        if (!payloadDataDefinition.equals("")) {
            streamDefinition += payloadDataDefinition;
        }

        if (streamDefinition.charAt(streamDefinition.length() - 1) == ',') {
            streamDefinition = streamDefinition.substring(0, streamDefinition.length() - 1);
        }
    }

    /**
     * Class holding a list of common sensor types in use. These enums can be used to set the "typeTAG" attribute of
     * the SensorType object.
     */
    public enum CommonSensorTypes {
        TEMPERATURE("Temperature"),
        DISTANCE("Distance"),
        GPS("GPS"),
        LIGHT("Light"),
        SOUND("Sound"),
        GAS("Gas"),
        ALTITUDE("Altitude"),
        DEPTH("Depth"),
        TILT("Tilt"),
        FLOW("Flow"),
        CAMERA("Camera");

        private String value;

        CommonSensorTypes(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
