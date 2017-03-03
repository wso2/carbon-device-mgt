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

package org.wso2.carbon.apimgt.application.extension.api.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO class to be used when registering an ApiM application.
 */
@XmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationProfile {
    @XmlElement(required = true)
    private String applicationName;
    @XmlElement(required = true)
    private String tags[];
    @XmlElement(required = true)
    private boolean isAllowedToAllDomains;
    @XmlElement(required = false)
    private String validityPeriod;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApiApplicationName(String apiApplicationName) {
        this.applicationName = apiApplicationName;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public boolean isAllowedToAllDomains() {
        return isAllowedToAllDomains;
    }

    public void setIsAllowedToAllDomains(boolean isAllowedToAllDomains) {
        this.isAllowedToAllDomains = isAllowedToAllDomains;
    }

    public String getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(String validityPeriod) {
        this.validityPeriod = validityPeriod;
    }
}
