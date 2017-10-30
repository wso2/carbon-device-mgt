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
 * <p>Java class for DataSource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataSource">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="JndiConfig" type="{}JndiConfig"/>
 *         &lt;element name="TableConfig" type="{}TableConfig"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSource", propOrder = {
    "jndiConfig",
    "tableConfig"
})
public class DataSource {

    @XmlElement(name = "JndiConfig", required = true)
    protected JndiConfig jndiConfig;

    @XmlElement(name = "TableConfig", required = true)
    protected TableConfig tableConfig;

    /**
     * Gets the value of the jndiConfig property.
     * 
     * @return
     *     possible object is
     *     {@link JndiConfig }
     *     
     */
    public JndiConfig getJndiConfig() {
        return jndiConfig;
    }

    /**
     * Sets the value of the jndiConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link JndiConfig }
     *     
     */
    public void setJndiConfig(JndiConfig value) {
        this.jndiConfig = value;
    }

    /**
     * Gets the value of the tableConfig property.
     * 
     * @return
     *     possible object is
     *     {@link TableConfig }
     *     
     */
    public TableConfig getTableConfig() {
        return tableConfig;
    }

    /**
     * Sets the value of the tableConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableConfig }
     *     
     */
    public void setTableConfig(TableConfig value) {
        this.tableConfig = value;
    }

}
