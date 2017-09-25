package org.wso2.carbon.device.mgt.core.archival;


public class ArchivalException extends Exception {

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ArchivalException(String message) {
        super(message);
        setErrorMessage(message);
    }

    public ArchivalException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    protected ArchivalException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        setErrorMessage(message);
    }
}
