
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
