package org.wso2.carbon.device.application.mgt.common.exception;

import org.wso2.carbon.device.application.mgt.common.Comment;

public class CommentManagementException extends Exception{
    private String message;

    public CommentManagementException(String message, Throwable throwable) {
        super(message, throwable);
        setMessage(message);
    }

    public CommentManagementException(String message) {
        super(message);
        setMessage(message);
    }

    public CommentManagementException() {

    }
    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
