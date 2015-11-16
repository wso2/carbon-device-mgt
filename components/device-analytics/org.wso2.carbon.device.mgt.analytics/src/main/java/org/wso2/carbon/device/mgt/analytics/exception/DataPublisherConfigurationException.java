package org.wso2.carbon.device.mgt.analytics.exception;

public class DataPublisherConfigurationException extends Exception {
	public DataPublisherConfigurationException() {
		super();
	}

	public DataPublisherConfigurationException(String message) {
		super(message);
	}

	public DataPublisherConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataPublisherConfigurationException(Throwable cause) {
		super(cause);
	}

	protected DataPublisherConfigurationException(String message, Throwable cause,
												  boolean enableSuppression,
												  boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
