/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Java class for PushNotificationProviders complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PushNotificationProviders">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PushNotificationProvider" type="{}PushNotificationProvider"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PushNotificationProviders", propOrder = {
        "pushNotificationProviders"
})
public class PushNotificationProviders {

    @XmlElement(name = "PushNotificationProviderConfig")
    protected List<PushNotificationProvider> pushNotificationProviders;

    /**
     * Gets the value of the pushNotificationProviders property.
     *
     * @return
     *     possible object is
     *     {@link PushNotificationProvider }
     *
     */
    public List<PushNotificationProvider> getPushNotificationProviders() {
        if (pushNotificationProviders == null) {
            pushNotificationProviders = new ArrayList<PushNotificationProvider>();
        }
        return this.pushNotificationProviders;
    }

    public void addPushNotificationProviders(List<PushNotificationProvider> pushNotificationProviders) {
        this.pushNotificationProviders = pushNotificationProviders;
    }

}