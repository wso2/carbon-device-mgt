package org.wso2.carbon.device.mgt.common;

import java.util.List;

/**
 *
 */
public class OperationMonitoringTaskConfig {

    private boolean isEnabled;
    private int frequency;
    private List<MonitoringOperation> monitoringOperation;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public List<MonitoringOperation> getMonitoringOperation() {
        return monitoringOperation;
    }

    public void setMonitoringOperation(List<MonitoringOperation> monitoringOperation) {
        this.monitoringOperation = monitoringOperation;
    }



}
