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
package org.wso2.carbon.device.mgt.common;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;
import io.swagger.annotations.*;

@ApiModel(value = "Feature", description = "This class carries all information related to a devices enrollment status.")
public class Feature implements Serializable {

    @ApiModelProperty(name = "id", value = "Feature Id.", required = true )
    private int id;
    @ApiModelProperty(name = "code", value = "The code of the feature. For example the code to lock a device" +
                                             " is DEVICE_LOCK.", required = true )
    private String code;
    @ApiModelProperty(name = "name", value = "A name that describes a feature.", required = true )
    private String name;
    @ApiModelProperty(name = "description", value = "Provides a description of the features..", required = true )
    private String description;
    @ApiModelProperty(name = "deviceType", value = "Provide the device type for the respective feature. " +
                                                   "Features allow you to perform operations on any device type, " +
                                                   "such as android, iOS or windows..", required = true )
    private String deviceType;
    
    @ApiModelProperty(name = "metadataEntries", value = "Properties related to features.", required = true )
    private List<MetadataEntry> metadataEntries;

    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MetadataEntry> getMetadataEntries() {
        return metadataEntries;
    }

    public void setMetadataEntries(List<MetadataEntry> metadataEntries) {
        this.metadataEntries = metadataEntries;
    }

    @XmlElement
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class MetadataEntry implements Serializable {

        private int id;
        private Object value;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
