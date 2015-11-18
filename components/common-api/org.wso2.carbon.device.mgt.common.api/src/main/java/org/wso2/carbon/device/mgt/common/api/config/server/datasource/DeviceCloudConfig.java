/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.api.config.server.datasource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeviceCloudConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * <complexType name="DeviceCloudConfig">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="DataStores" type="{}DataStoresConfig"/>
 *         <element name="ControlQueues" type="{}ControlQueuesConfig"/>
 *         <element name="Security" type="{}SecurityConfig"/>
 *         <element name="ApiManager" type="{}ApiManagerConfig"/>
 *         <element name="DeviceUserValidator" type="{}DeviceUserValidatorConfig"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceCloudConfig", propOrder = {
    "dataStores",
    "controlQueues",
    "security",
    "apiManager",
    "deviceUserValidator"
})
@XmlRootElement(name = "DeviceCloudConfiguration")
public class DeviceCloudConfig {

    @XmlElement(name = "DataStores", required = true)
    protected DataStoresConfig dataStores;
    @XmlElement(name = "ControlQueues", required = true)
    protected ControlQueuesConfig controlQueues;
    @XmlElement(name = "Security", required = true)
    protected SecurityConfig security;
    @XmlElement(name = "ApiManager", required = true)
    protected ApiManagerConfig apiManager;
    @XmlElement(name = "DeviceUserValidator", required = true)
    protected DeviceUserValidatorConfig deviceUserValidator;

    /**
     * Gets the value of the dataStores property.
     * 
     * @return
     *     possible object is
     *     {@link DataStoresConfig }
     *     
     */
    public DataStoresConfig getDataStores() {
        return dataStores;
    }

    /**
     * Sets the value of the dataStores property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataStoresConfig }
     *     
     */
    public void setDataStores(DataStoresConfig value) {
        this.dataStores = value;
    }

    /**
     * Gets the value of the controlQueues property.
     * 
     * @return
     *     possible object is
     *     {@link ControlQueuesConfig }
     *     
     */
    public ControlQueuesConfig getControlQueues() {
        return controlQueues;
    }

    /**
     * Sets the value of the controlQueues property.
     * 
     * @param value
     *     allowed object is
     *     {@link ControlQueuesConfig }
     *     
     */
    public void setControlQueues(ControlQueuesConfig value) {
        this.controlQueues = value;
    }

    /**
     * Gets the value of the security property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityConfig }
     *     
     */
    public SecurityConfig getSecurity() {
        return security;
    }

    /**
     * Sets the value of the security property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityConfig }
     *     
     */
    public void setSecurity(SecurityConfig value) {
        this.security = value;
    }

    /**
     * Gets the value of the apiManager property.
     * 
     * @return
     *     possible object is
     *     {@link ApiManagerConfig }
     *     
     */
    public ApiManagerConfig getApiManager() {
        return apiManager;
    }

    /**
     * Sets the value of the apiManager property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApiManagerConfig }
     *     
     */
    public void setApiManager(ApiManagerConfig value) {
        this.apiManager = value;
    }

    /**
     * Gets the value of the deviceUserValidator property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceUserValidatorConfig }
     *     
     */
    public DeviceUserValidatorConfig getDeviceUserValidator() {
        return deviceUserValidator;
    }

    /**
     * Sets the value of the deviceUserValidator property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceUserValidatorConfig }
     *     
     */
    public void setDeviceUserValidator(DeviceUserValidatorConfig value) {
        this.deviceUserValidator = value;
    }

}
