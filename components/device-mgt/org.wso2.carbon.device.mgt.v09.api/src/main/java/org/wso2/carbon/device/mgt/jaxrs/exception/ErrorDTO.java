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

import java.util.ArrayList;
import java.util.List;

public class ErrorDTO {

    private Long code = null;
    private String message = null;
    private String description = null;

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setError(List<ErrorDTO> error) {
        this.error = error;
    }

    private String moreInfo = null;

    public String getMessage() {
        return message;
    }

    public Long getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public List<ErrorDTO> getError() {
        return error;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("class ErrorDTO {\n");
        stringBuilder.append("  code: ").append(code).append("\n");
        stringBuilder.append("  message: ").append(message).append("\n");
        stringBuilder.append("  description: ").append(description).append("\n");
        stringBuilder.append("  moreInfo: ").append(moreInfo).append("\n");
        stringBuilder.append("  error: ").append(error).append("\n");
        stringBuilder.append("}\n");
        return stringBuilder.toString();
    }

    private List<ErrorDTO> error = new ArrayList<>();

}
