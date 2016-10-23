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
package org.wso2.carbon.device.mgt.extensions.device.type.deployer.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PushNotificationConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PushNotificationConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PushNotificationProvider" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FileBasedProperties" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Properties" type="{}Properties"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PushNotificationConfiguration", propOrder = {
    "pushNotificationProvider",
    "fileBasedProperties",
    "properties"
})
public class PushNotificationConfiguration {

    @XmlElement(name = "PushNotificationProvider", required = true)
    protected String pushNotificationProvider;
    @XmlElement(name = "FileBasedProperties", required = true)
    protected boolean fileBasedProperties;
    @XmlElement(name = "Properties", required = true)
    protected Properties properties;

    /**
     * Gets the value of the pushNotificationProvider property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPushNotificationProvider() {
        return pushNotificationProvider;
    }

    /**
     * Sets the value of the pushNotificationProvider property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPushNotificationProvider(String value) {
        this.pushNotificationProvider = value;
    }

    /**
     * Gets the value of the fileBasedProperties property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean isFileBasedProperties() {
        return fileBasedProperties;
    }

    /**
     * Sets the value of the fileBasedProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileBasedProperties(boolean value) {
        this.fileBasedProperties = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link Properties }
     *     
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties }
     *     
     */
    public void setProperties(Properties value) {
        this.properties = value;
    }

}
