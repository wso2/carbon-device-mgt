
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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.wso2.carbon.device.mgt.common.api.config.server.configs package.
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

    private final static QName _DeviceCloudConfiguration_QNAME = new QName("", "DeviceCloudConfiguration");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.wso2.carbon.device.mgt.iot.common.config.server.configs
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeviceCloudConfig }
     * 
     */
    public DeviceCloudConfig createDeviceCloudConfig() {
        return new DeviceCloudConfig();
    }

    /**
     * Create an instance of {@link ApiManagerConfig }
     * 
     */
    public ApiManagerConfig createApiManagerConfig() {
        return new ApiManagerConfig();
    }

    /**
     * Create an instance of {@link ControlQueuesConfig }
     * 
     */
    public ControlQueuesConfig createControlQueuesConfig() {
        return new ControlQueuesConfig();
    }

    /**
     * Create an instance of {@link SecurityConfig }
     * 
     */
    public SecurityConfig createSecurityConfig() {
        return new SecurityConfig();
    }

    /**
     * Create an instance of {@link ControlQueue }
     * 
     */
    public ControlQueue createControlQueue() {
        return new ControlQueue();
    }

    /**
     * Create an instance of {@link DeviceUserValidatorConfig }
     * 
     */
    public DeviceUserValidatorConfig createDeviceUserValidatorConfig() {
        return new DeviceUserValidatorConfig();
    }

    /**
     * Create an instance of {@link DataStore }
     * 
     */
    public DataStore createDataStore() {
        return new DataStore();
    }

    /**
     * Create an instance of {@link DataStoresConfig }
     * 
     */
    public DataStoresConfig createDataStoresConfig() {
        return new DataStoresConfig();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeviceCloudConfig }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "DeviceCloudConfiguration")
    public JAXBElement<DeviceCloudConfig> createDeviceCloudConfiguration(DeviceCloudConfig value) {
        return new JAXBElement<DeviceCloudConfig>(_DeviceCloudConfiguration_QNAME, DeviceCloudConfig.class, null, value);
    }

}
