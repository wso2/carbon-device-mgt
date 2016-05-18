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
package org.wso2.carbon.device.mgt.common.push.notification;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement(name = "PushNotificationProviderConfiguration")
public class PushNotificationConfig {

    private String type;
    Map<String, String> properties;

    public PushNotificationConfig(String type, Map<String, String> properties) {
        this.type = type;
        this.properties = properties;
    }

    @XmlElement(name = "Type", required = true)
    public String getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

}
