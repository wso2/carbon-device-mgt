package org.wso2.carbon.device.mgt.extensions.device.type.deployer.config;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * <p>Java class for InitialOperationConfig complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="InitialOperationConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Operations" type="{http://www.w3.org/2001/XMLSchema}list"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlRootElement(name = "InitialOperationConfig")
public class InitialOperationConfig {

    private List<String> operations;

    @XmlElementWrapper(name = "Operations", required = true)
    @XmlElement(name = "Operation", required = true)
    public List<String> getOperations() {
        return operations;
    }

    public void setOperationsll(List<String> operations) {
        this.operations = operations;
    }
}
