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

package org.wso2.carbon.device.mgt.etc.config.devicetype.datasource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeviceTypeConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * <complexType name="DeviceTypeConfig">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="DatasourceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         <element name="ApiApplicationName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceTypeConfig", propOrder = {
    "datasourceName",
    "apiApplicationName"
})
public class DeviceTypeConfig {

    @XmlElement(name = "DatasourceName", required = true)
    protected String datasourceName;
    @XmlElement(name = "ApiApplicationName")
    protected String apiApplicationName;
    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * Gets the value of the datasourceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatasourceName() {
        return datasourceName;
    }

    /**
     * Sets the value of the datasourceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatasourceName(String value) {
        this.datasourceName = value;
    }

    /**
     * Gets the value of the apiApplicationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApiApplicationName() {
        return apiApplicationName;
    }

    /**
     * Sets the value of the apiApplicationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApiApplicationName(String value) {
        this.apiApplicationName = value;
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
