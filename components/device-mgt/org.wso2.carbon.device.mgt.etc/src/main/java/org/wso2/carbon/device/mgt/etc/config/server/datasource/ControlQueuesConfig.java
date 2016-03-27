
package org.wso2.carbon.device.mgt.etc.config.server.datasource;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ControlQueuesConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * <complexType name="ControlQueuesConfig">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="ControlQueue" type="{}ControlQueue" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ControlQueuesConfig", propOrder = {
    "controlQueue"
})
public class ControlQueuesConfig {

    @XmlElement(name = "ControlQueue")
    protected List<ControlQueue> controlQueue;

    /**
     * Gets the value of the controlQueue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the controlQueue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getControlQueue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ControlQueue }
     * 
     * 
     */
    public List<ControlQueue> getControlQueue() {
        if (controlQueue == null) {
            controlQueue = new ArrayList<ControlQueue>();
        }
        return this.controlQueue;
    }

}
