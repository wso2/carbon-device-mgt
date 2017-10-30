/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.analytics.dashboard.exception;

/**
 * Custom exception class for catching invalid parameter values,
 * relevant to Gadget Data Service DAO layer.
 */
public class InvalidResultCountValueException extends Exception {

    private String errorMessage;
    private static final long serialVersionUID = 2021891706072918864L;

    /**
     * Constructs a new exception with the specific error message and nested exception.
     * @param errorMessage specific error message.
     * @param nestedException Nested exception.
     */
    @SuppressWarnings("unused")
    public InvalidResultCountValueException(String errorMessage, Exception nestedException) {
        super(errorMessage, nestedException);
        setErrorMessage(errorMessage);
    }

    /**
     * Constructs a new exception with the specific error message and cause.
     * @param errorMessage Specific error message.
     * @param cause Cause of this exception.
     */
    @SuppressWarnings("unused")
    public InvalidResultCountValueException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        setErrorMessage(errorMessage);
    }

    /**
     * Constructs a new exception with the specific error message.
     * @param errorMessage Specific error message.
     */
    public InvalidResultCountValueException(String errorMessage) {
        super(errorMessage);
        setErrorMessage(errorMessage);
    }

    /**
     * Constructs a new exception with the specific error message and cause.
     * @param cause Cause of this exception.
     */
    @SuppressWarnings("unused")
    public InvalidResultCountValueException(Throwable cause) {
        super(cause);
    }

    @SuppressWarnings("unused")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}

