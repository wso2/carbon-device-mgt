
package org.wso2.carbon.device.mgt.extensions.device.type.deployer.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeviceDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeviceDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TableName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PrimaryKey" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Attributes" type="{}Attributes"/>
 *         &lt;element name="License" type="{}License"/>
 *         &lt;element name="Features" type="{}Features"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceDefinition", propOrder = {
    "tableName",
    "primaryKey",
    "attributes",
    "license",
    "features"
})
public class DeviceDefinition {

    @XmlElement(name = "TableName", required = true)
    protected String tableName;
    @XmlElement(name = "PrimaryKey", required = true)
    protected String primaryKey;
    @XmlElement(name = "Attributes", required = true)
    protected Attributes attributes;
    @XmlElement(name = "License", required = true)
    protected License license;
    @XmlElement(name = "Features", required = true)
    protected Features features;

    /**
     * Gets the value of the tableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the value of the tableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTableName(String value) {
        this.tableName = value;
    }

    /**
     * Gets the value of the primaryKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Sets the value of the primaryKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrimaryKey(String value) {
        this.primaryKey = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link Attributes }
     *     
     */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Attributes }
     *     
     */
    public void setAttributes(Attributes value) {
        this.attributes = value;
    }

    /**
     * Gets the value of the license property.
     * 
     * @return
     *     possible object is
     *     {@link License }
     *     
     */
    public License getLicense() {
        return license;
    }

    /**
     * Sets the value of the license property.
     * 
     * @param value
     *     allowed object is
     *     {@link License }
     *     
     */
    public void setLicense(License value) {
        this.license = value;
    }

    /**
     * Gets the value of the features property.
     * 
     * @return
     *     possible object is
     *     {@link Features }
     *     
     */
    public Features getFeatures() {
        return features;
    }

    /**
     * Sets the value of the features property.
     * 
     * @param value
     *     allowed object is
     *     {@link Features }
     *     
     */
    public void setFeatures(Features value) {
        this.features = value;
    }

}
