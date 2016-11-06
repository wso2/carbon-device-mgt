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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.wso2.carbon package. 
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

    private final static QName _DeviceTypeConfiguration_QNAME = new QName("", "DeviceTypeConfiguration");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.wso2.carbon
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeviceTypeConfiguration }
     * 
     */
    public DeviceTypeConfiguration createDeviceTypeConfiguration() {
        return new DeviceTypeConfiguration();
    }

    /**
     * Create an instance of {@link Operation }
     * 
     */
    public Operation createOperation() {
        return new Operation();
    }

    /**
     * Create an instance of {@link Attributes }
     * 
     */
    public Attributes createAttributes() {
        return new Attributes();
    }

    /**
     * Create an instance of {@link ProvisioningConfig }
     * 
     */
    public ProvisioningConfig createProvisioningConfig() {
        return new ProvisioningConfig();
    }

    /**
     * Create an instance of {@link TableConfig }
     * 
     */
    public TableConfig createTableConfig() {
        return new TableConfig();
    }

    /**
     * Create an instance of {@link Table }
     * 
     */
    public Table createTable() {
        return new Table();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link JndiConfig }
     * 
     */
    public JndiConfig createJndiConfig() {
        return new JndiConfig();
    }

    /**
     * Create an instance of {@link FormParameters }
     * 
     */
    public FormParameters createFormParameters() {
        return new FormParameters();
    }

    /**
     * Create an instance of {@link Features }
     * 
     */
    public Features createFeatures() {
        return new Features();
    }

    /**
     * Create an instance of {@link Feature }
     * 
     */
    public Feature createFeature() {
        return new Feature();
    }

    /**
     * Create an instance of {@link PushNotificationProvider }
     * 
     */
    public PushNotificationProvider createPushNotificationProvider() {
        return new PushNotificationProvider();
    }

    /**
     * Create an instance of {@link DataSource }
     * 
     */
    public DataSource createDataSource() {
        return new DataSource();
    }

    /**
     * Create an instance of {@link ConfigProperties }
     * 
     */
    public ConfigProperties createConfigProperties() {
        return new ConfigProperties();
    }

    /**
     * Create an instance of {@link License }
     * 
     */
    public License createLicense() {
        return new License();
    }

    /**
     * Create an instance of {@link DeviceDetails }
     * 
     */
    public DeviceDetails createDeviceDetails() {
        return new DeviceDetails();
    }

    /**
     * Create an instance of {@link QueryParameters }
     * 
     */
    public QueryParameters createQueryParameters() {
        return new QueryParameters();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeviceTypeConfiguration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "DeviceTypeConfiguration")
    public JAXBElement<DeviceTypeConfiguration> createDeviceTypeConfiguration(DeviceTypeConfiguration value) {
        return new JAXBElement<DeviceTypeConfiguration>(_DeviceTypeConfiguration_QNAME, DeviceTypeConfiguration.class, null, value);
    }

}
