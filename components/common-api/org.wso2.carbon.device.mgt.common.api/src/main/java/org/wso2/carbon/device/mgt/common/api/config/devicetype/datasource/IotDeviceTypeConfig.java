
package org.wso2.carbon.device.mgt.common.api.config.devicetype.datasource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IotDeviceTypeConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IotDeviceTypeConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DatasourceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ApiApplicationName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IotDeviceTypeConfig", propOrder = {
    "datasourceName",
    "apiApplicationName"
})
public class IotDeviceTypeConfig {

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
