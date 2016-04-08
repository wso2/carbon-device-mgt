package org.wso2.carbon.device.mgt.analytics.dashboard.dao;

@SuppressWarnings("unused")
/**
 * Custom exception class for data access related exceptions.
 */
public class GadgetDataServiceDAOException extends Exception {
    private String errorMessage;
    private static final long serialVersionUID = 2021891706072918864L;

    /**
     * Constructs a new exception with the specific error message and nested exception.
     *
     * @param errorMessage specific error message.
     * @param nestedException Nested exception.
     */
    public GadgetDataServiceDAOException(String errorMessage, Exception nestedException) {
        super(errorMessage, nestedException);
        setErrorMessage(errorMessage);
    }

    /**
     * Constructs a new exception with the specific error message and cause.
     *
     * @param errorMessage Specific error message.
     * @param cause Cause of this exception.
     */
    public GadgetDataServiceDAOException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        setErrorMessage(errorMessage);
    }

    /**
     * Constructs a new exception with the specific error message.
     *
     * @param errorMessage Specific error message.
     */
    public GadgetDataServiceDAOException(String errorMessage) {
        super(errorMessage);
        setErrorMessage(errorMessage);
    }

    /**
     * Constructs a new exception with the specific error message and cause.
     *
     * @param cause Cause of this exception.
     */
    public GadgetDataServiceDAOException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
