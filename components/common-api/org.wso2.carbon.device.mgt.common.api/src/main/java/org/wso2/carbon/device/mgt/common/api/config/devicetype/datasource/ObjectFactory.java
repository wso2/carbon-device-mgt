
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

    private final static QName _IoTDeviceTypeConfigManager_QNAME = new QName("", "IoTDeviceTypeConfigManager");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.wso2.carbon.device.mgt.iot.common.config.server.configs
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link IoTDeviceTypeConfigManager }
     * 
     */
    public IoTDeviceTypeConfigManager createIoTDeviceTypeConfigManager() {
        return new IoTDeviceTypeConfigManager();
    }

    /**
     * Create an instance of {@link IotDeviceTypeConfig }
     * 
     */
    public IotDeviceTypeConfig createIotDeviceTypeConfig() {
        return new IotDeviceTypeConfig();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IoTDeviceTypeConfigManager }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "IoTDeviceTypeConfigManager")
    public JAXBElement<IoTDeviceTypeConfigManager> createIoTDeviceTypeConfigManager(IoTDeviceTypeConfigManager value) {
        return new JAXBElement<IoTDeviceTypeConfigManager>(_IoTDeviceTypeConfigManager_QNAME, IoTDeviceTypeConfigManager.class, null, value);
    }

}
