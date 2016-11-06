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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PushNotificationProvider complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PushNotificationProvider">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FileBasedProperties" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="ConfigProperties" type="{}ConfigProperties"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PushNotificationProvider", propOrder = {
    "fileBasedProperties",
    "configProperties"
})
public class PushNotificationProvider {

    @XmlElement(name = "FileBasedProperties")
    protected boolean fileBasedProperties;
    @XmlElement(name = "ConfigProperties", required = true)
    protected ConfigProperties configProperties;
    @XmlAttribute(name = "type")
    protected String type;

    /**
     * Gets the value of the fileBasedProperties property.
     * 
     */
    public boolean isFileBasedProperties() {
        return fileBasedProperties;
    }

    /**
     * Sets the value of the fileBasedProperties property.
     * 
     */
    public void setFileBasedProperties(boolean value) {
        this.fileBasedProperties = value;
    }

    /**
     * Gets the value of the configProperties property.
     * 
     * @return
     *     possible object is
     *     {@link ConfigProperties }
     *     
     */
    public ConfigProperties getConfigProperties() {
        return configProperties;
    }

    /**
     * Sets the value of the configProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfigProperties }
     *     
     */
    public void setConfigProperties(ConfigProperties value) {
        this.configProperties = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
