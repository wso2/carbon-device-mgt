package org.wso2.carbon.device.mgt.common.api.exception;

/**
 * Created by smean-MAC on 5/29/15.
 */
public class DeviceControllerException extends Exception{

    public DeviceControllerException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DeviceControllerException(Throwable cause) {
        super(cause);
    }

    public DeviceControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceControllerException(String message) {
        super(message);
    }

    public DeviceControllerException() {
    }
}
