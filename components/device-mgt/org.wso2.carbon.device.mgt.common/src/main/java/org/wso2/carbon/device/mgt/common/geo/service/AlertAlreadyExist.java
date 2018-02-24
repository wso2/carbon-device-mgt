package org.wso2.carbon.device.mgt.common.geo.service;

public class AlertAlreadyExist extends Exception {

    private static final long serialVersionUID = 4709355511911265093L;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

     public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

     public AlertAlreadyExist(String msg) {
        super(msg);
        setErrorMessage(msg);
    }
}