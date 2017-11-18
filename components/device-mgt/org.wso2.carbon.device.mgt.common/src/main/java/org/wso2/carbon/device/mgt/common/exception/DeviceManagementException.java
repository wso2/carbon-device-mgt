/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.common.exception;

/**
 * This exception is thrown whenever a device management exceptoin occurs
 */
public class DeviceManagementException extends Exception {

    private static final long serialVersionUID = -3151279311929070297L;
    private static final int INTERNAL_SERVER_ERROR = 500;

    private int status = INTERNAL_SERVER_ERROR;

    public DeviceManagementException(String msg, Exception nestedEx) {
        super(msg, nestedEx);

    }

    public DeviceManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceManagementException(String message, Throwable cause, int status) {
        super(message, cause);
        this.status = status;
    }

    public DeviceManagementException(String msg) {
        super(msg);
    }

    public DeviceManagementException(String msg, int status) {
        super(msg);
        this.status = status;
    }

    public DeviceManagementException() {
        super();
    }

    public DeviceManagementException(Throwable cause) {
        super(cause);
    }

    public int getStatus() {
        return status;
    }
}
