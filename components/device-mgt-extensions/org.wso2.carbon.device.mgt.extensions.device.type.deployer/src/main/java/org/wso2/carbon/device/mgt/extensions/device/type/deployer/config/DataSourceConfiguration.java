
package org.wso2.carbon.device.mgt.extensions.device.type.deployer.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DataSourceConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataSourceConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="JndiLookupDefinition" type="{}JndiLookupDefinition"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSourceConfiguration", propOrder = {
    "jndiLookupDefinition"
})
public class DataSourceConfiguration {

    @XmlElement(name = "JndiLookupDefinition", required = true)
    protected JndiLookupDefinition jndiLookupDefinition;

    /**
     * Gets the value of the jndiLookupDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link JndiLookupDefinition }
     *     
     */
    public JndiLookupDefinition getJndiLookupDefinition() {
        return jndiLookupDefinition;
    }

    /**
     * Sets the value of the jndiLookupDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link JndiLookupDefinition }
     *     
     */
    public void setJndiLookupDefinition(JndiLookupDefinition value) {
        this.jndiLookupDefinition = value;
    }

}
