package org.wso2.carbon.device.mgt.common.api.exception;

public class DeviceMgtCommonsException extends Exception {
	public DeviceMgtCommonsException() {
		super();
	}

	public DeviceMgtCommonsException(String message) {
		super(message);
	}

	public DeviceMgtCommonsException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeviceMgtCommonsException(Throwable cause) {
		super(cause);
	}

	protected DeviceMgtCommonsException(String message, Throwable cause, boolean enableSuppression,
	                                    boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
