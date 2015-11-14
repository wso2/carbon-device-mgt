package org.wso2.carbon.device.mgt.analytics.exception;

public class DataPublisherAlreadyExistsException extends  Exception {
	public DataPublisherAlreadyExistsException() {
		super();
	}

	public DataPublisherAlreadyExistsException(String message) {
		super(message);
	}

	public DataPublisherAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataPublisherAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	protected DataPublisherAlreadyExistsException(String message, Throwable cause,
												  boolean enableSuppression,
												  boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
