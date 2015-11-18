
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

package org.wso2.carbon.device.mgt.common.api.config.devicetype.datasource;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.wso2.carbon.device.mgt.iot.common.config.server.configs package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DeviceTypeConfigManager_QNAME = new QName("", "DeviceTypeConfigManager");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.wso2.carbon.device.mgt.iot.common.config.server.configs
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeviceTypeConfigManager }
     * 
     */
    public DeviceTypeConfigManager createDeviceTypeConfigManager() {
        return new DeviceTypeConfigManager();
    }

    /**
     * Create an instance of {@link DeviceTypeConfig }
     * 
     */
    public DeviceTypeConfig createDeviceTypeConfig() {
        return new DeviceTypeConfig();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeviceTypeConfigManager }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "DeviceTypeConfigManager")
    public JAXBElement<DeviceTypeConfigManager> createDeviceTypeConfigManager(
            DeviceTypeConfigManager value) {
        return new JAXBElement<DeviceTypeConfigManager>(_DeviceTypeConfigManager_QNAME, DeviceTypeConfigManager.class, null, value);
    }

}
