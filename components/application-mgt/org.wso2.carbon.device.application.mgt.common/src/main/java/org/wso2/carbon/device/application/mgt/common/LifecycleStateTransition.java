package org.wso2.carbon.device.application.mgt.common;

/**
 * This represents the LifeCycleStateTransition from one state to next state.
 */
public class LifecycleStateTransition {
    private String nextState;
    private String permission;
    private String description;

    public String getNextState() {
        return nextState;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public void setNextState(String nextState) {
        this.nextState = nextState;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
