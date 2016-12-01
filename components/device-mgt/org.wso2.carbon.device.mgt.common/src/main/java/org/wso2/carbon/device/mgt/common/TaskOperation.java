package org.wso2.carbon.device.mgt.common;

/**
 *
 */
public class TaskOperation {

    private String taskName;
    private int recurrentTimes;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getRecurrentTimes() {
        return recurrentTimes;
    }

    public void setRecurrentTimes(int recurrentTimes) {
        this.recurrentTimes = recurrentTimes;
    }

}

