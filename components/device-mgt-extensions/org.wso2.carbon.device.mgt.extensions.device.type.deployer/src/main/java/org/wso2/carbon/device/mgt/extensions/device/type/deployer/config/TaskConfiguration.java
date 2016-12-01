package org.wso2.carbon.device.mgt.extensions.device.type.deployer.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 *
 */
@XmlRootElement(name = "TaskConfiguration")
public class TaskConfiguration {


    private boolean enabled;
    private int frequency;
    private String taskClazz;
    private List<Operation> operations;

    @XmlElement(name = "Enable", required = true)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @XmlElement(name = "Frequency", required = true)
    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @XmlElement(name = "TaskClass", required = true)
    public String getTaskClazz() {
        return taskClazz;
    }

    public void setTaskClazz(String taskClazz) {
        this.taskClazz = taskClazz;
    }

    @XmlElementWrapper(name="Operations")
    @XmlElement(name = "Operation", required = true)
    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    @XmlRootElement(name = "Operation")
    public static class Operation {

        private String operationName;
        private int recurrency;

        @XmlElement(name = "Name", required = true)
        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        @XmlElement(name = "RecurrentTimes", required = true)
        public int getRecurrency() {
            return recurrency;
        }

        public void setRecurrency(int recurrency) {
            this.recurrency = recurrency;
        }

    }
}

