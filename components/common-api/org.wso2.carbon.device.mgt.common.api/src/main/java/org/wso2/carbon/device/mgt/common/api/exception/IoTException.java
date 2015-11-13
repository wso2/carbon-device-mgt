package org.wso2.carbon.device.mgt.common.api.exception;

public class IoTException extends Exception {
	public IoTException() {
		super();
	}

	public IoTException(String message) {
		super(message);
	}

	public IoTException(String message, Throwable cause) {
		super(message, cause);
	}

	public IoTException(Throwable cause) {
		super(cause);
	}

	protected IoTException(String message, Throwable cause, boolean enableSuppression,
						   boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
