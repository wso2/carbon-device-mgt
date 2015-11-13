package org.wso2.carbon.device.mgt.common.api.exception;


public class AccessTokenException extends Exception {


	public AccessTokenException(String message) {
		super(message);
	}

	public AccessTokenException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccessTokenException(Throwable cause) {
		super(cause);
	}

	public AccessTokenException(String message, Throwable cause, boolean enableSuppression,
								 boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
