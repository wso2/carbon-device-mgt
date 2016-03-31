package org.wso2.carbon.apimgt.webapp.publisher.config;

public class APIResourceManagementException extends Exception{
	private static final long serialVersionUID = -3151279311929070297L;

	private String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public APIResourceManagementException(String msg, Exception nestedEx) {
		super(msg, nestedEx);
		setErrorMessage(msg);
	}

	public APIResourceManagementException(String message, Throwable cause) {
		super(message, cause);
		setErrorMessage(message);
	}

	public APIResourceManagementException(String msg) {
		super(msg);
		setErrorMessage(msg);
	}

	public APIResourceManagementException() {
		super();
	}

	public APIResourceManagementException(Throwable cause) {
		super(cause);
	}
}
