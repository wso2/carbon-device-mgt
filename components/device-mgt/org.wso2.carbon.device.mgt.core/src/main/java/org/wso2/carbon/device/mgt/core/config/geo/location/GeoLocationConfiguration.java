/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.geo.location;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the information related to Geo Location configuration.
 */
@XmlRootElement(name = "GeoLocationConfiguration")
public class GeoLocationConfiguration {

    private boolean publishLocationOperationResponse;
    private boolean isEnabled;

    public boolean getPublishLocationOperationResponse() {
        return publishLocationOperationResponse;
    }

    @XmlElement(name = "PublishLocationOperationResponse", required = true)
    public void setPublishLocationOperationResponse(boolean publishLocationOperationResponse) {
        this.publishLocationOperationResponse = publishLocationOperationResponse;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    @XmlElement(name = "isEnabled", required = true)
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
