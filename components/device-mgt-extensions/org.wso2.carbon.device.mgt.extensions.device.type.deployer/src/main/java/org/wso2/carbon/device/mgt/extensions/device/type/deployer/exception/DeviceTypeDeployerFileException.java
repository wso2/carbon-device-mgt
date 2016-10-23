/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception;

/**
 * This exception will be thrown if there are any issues with the content of the deployer.
 */
public class DeviceTypeDeployerFileException extends RuntimeException {

    public DeviceTypeDeployerFileException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public DeviceTypeDeployerFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceTypeDeployerFileException(String msg) {
        super(msg);
    }

    public DeviceTypeDeployerFileException() {
        super();
    }

    public DeviceTypeDeployerFileException(Throwable cause) {
        super(cause);
    }

}
