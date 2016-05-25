/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.configuration.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Represents the tenant configuration for a device platform.
 */
@XmlRootElement(name = "PlatformConfiguration")
@XmlAccessorType(XmlAccessType.NONE)

@ApiModel(value = "PlatformConfiguration",
        description = "This class carries all information related to a Tenant configuration")
public class PlatformConfiguration implements Serializable {

    @XmlElement(name = "type")
    @ApiModelProperty(name = "type", value = "type of device", required = true)
    private String type;

    @ApiModelProperty(name = "configuration", value = "List of Configuration Entries", required = true)
    @XmlElement(name = "configuration")
    private List<ConfigurationEntry> configuration;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ConfigurationEntry> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<ConfigurationEntry> configuration) {
        this.configuration = configuration;
    }

}
