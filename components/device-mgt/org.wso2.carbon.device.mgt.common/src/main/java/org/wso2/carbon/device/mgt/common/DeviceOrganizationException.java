package org.wso2.carbon.device.mgt.common;

public class DeviceOrganizationException extends Exception {

    public DeviceOrganizationException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public DeviceOrganizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceOrganizationException(String msg) {
        super(msg);
    }

    public DeviceOrganizationException() {
        super();
    }

    public DeviceOrganizationException(Throwable cause) {
        super(cause);
    }
}
