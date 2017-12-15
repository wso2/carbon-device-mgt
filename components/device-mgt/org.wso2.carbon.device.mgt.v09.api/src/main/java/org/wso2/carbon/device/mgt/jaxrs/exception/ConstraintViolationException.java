/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.exception;

import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;

import javax.validation.ConstraintViolation;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Set;

public class ConstraintViolationException extends WebApplicationException {
    private String message;

    public <T> ConstraintViolationException(Set<ConstraintViolation<T>> violations) {
        super(Response.status(Response.Status.BAD_REQUEST)
                .entity(DeviceMgtUtil.getConstraintViolationErrorDTO(violations))
                .header(Constants.DeviceConstants.HEADER_CONTENT_TYPE, Constants.DeviceConstants.APPLICATION_JSON)
                .build());

        //Set the error message
        StringBuilder stringBuilder = new StringBuilder();
        for (ConstraintViolation violation : violations) {
            stringBuilder.append(violation.getRootBeanClass().getSimpleName());
            stringBuilder.append(".");
            stringBuilder.append(violation.getPropertyPath());
            stringBuilder.append(": ");
            stringBuilder.append(violation.getMessage());
            stringBuilder.append(", ");
        }
        message = stringBuilder.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
