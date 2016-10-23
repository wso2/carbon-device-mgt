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
 * <p>Java class for ManagementRepository complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ManagementRepository">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DataSourceConfiguration" type="{}DataSourceConfiguration"/>
 *         &lt;element name="DeviceDefinition" type="{}DeviceDefinition"/>
 *         &lt;element name="ProvisioningConfig" type="{}ProvisioningConfig"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ManagementRepository", propOrder = {
    "dataSourceConfiguration",
    "deviceDefinition",
    "provisioningConfig"
})
public class ManagementRepository {

    @XmlElement(name = "DataSourceConfiguration", required = true)
    protected DataSourceConfiguration dataSourceConfiguration;
    @XmlElement(name = "DeviceDefinition", required = true)
    protected DeviceDefinition deviceDefinition;
    @XmlElement(name = "ProvisioningConfig", required = true)
    protected ProvisioningConfig provisioningConfig;

    /**
     * Gets the value of the dataSourceConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link DataSourceConfiguration }
     *     
     */
    public DataSourceConfiguration getDataSourceConfiguration() {
        return dataSourceConfiguration;
    }

    /**
     * Sets the value of the dataSourceConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataSourceConfiguration }
     *     
     */
    public void setDataSourceConfiguration(DataSourceConfiguration value) {
        this.dataSourceConfiguration = value;
    }

    /**
     * Gets the value of the deviceDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceDefinition }
     *     
     */
    public DeviceDefinition getDeviceDefinition() {
        return deviceDefinition;
    }

    /**
     * Sets the value of the deviceDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceDefinition }
     *     
     */
    public void setDeviceDefinition(DeviceDefinition value) {
        this.deviceDefinition = value;
    }

    /**
     * Gets the value of the provisioningConfig property.
     * 
     * @return
     *     possible object is
     *     {@link ProvisioningConfig }
     *     
     */
    public ProvisioningConfig getProvisioningConfig() {
        return provisioningConfig;
    }

    /**
     * Sets the value of the provisioningConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProvisioningConfig }
     *     
     */
    public void setProvisioningConfig(ProvisioningConfig value) {
        this.provisioningConfig = value;
    }

}
