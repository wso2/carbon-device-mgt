/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.certificate.mgt.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Error Response")
public class ErrorResponse {

    private Long code = null;
    private String message = null;
    private String description = null;
    private String moreInfo = null;
    private List<ErrorListItem> errorItems = new ArrayList<>();

    private ErrorResponse() {
    }

    @JsonProperty(value = "code")
    @ApiModelProperty(required = true, value = "")
    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    @JsonProperty(value = "message")
    @ApiModelProperty(required = true, value = "ErrorResponse message.")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty(value = "description")
    @ApiModelProperty(value = "A detail description about the error message.")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty(value = "moreInfo")
    @ApiModelProperty(value = "Preferably an url with more details about the error.")
    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public void addErrorListItem(ErrorListItem item) {
        this.errorItems.add(item);
    }

    /**
     * If there are more than one error list them out. \nFor example, list out validation errors by each field.
     */
    @JsonProperty(value = "errorItems")
    @ApiModelProperty(value = "If there are more than one error list them out. \n" +
            "For example, list out validation errors by each field.")
    public List<ErrorListItem> getErrorItems() {
        return errorItems;
    }

    public void setErrorItems(List<ErrorListItem> error) {
        this.errorItems = error;
    }

    @Override
    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("{");
//        boolean cont = false;
//        if (code != null) {
//            cont = true;
//            sb.append("  \"code\": ").append(code);
//        }
//        if (message != null) {
//            if (cont) {
//                sb.append(",");
//            }
//            cont = true;
//            sb.append("  \"message\": \"").append(message).append("\"");
//        }
//        if (description != null) {
//            if (cont) {
//                sb.append(",");
//            }
//            cont = true;
//            sb.append("  \"description\": ").append(description).append("\"");
//        }
//        if (moreInfo != null) {
//            if (cont) {
//                sb.append(",");
//            }
//            cont = true;
//            sb.append("  \"moreInfo\": \"").append(moreInfo).append("\"");
//        }
//        if (error != null && error.size() > 0) {
//            if (cont) {
//                sb.append(",");
//            }
//            sb.append("  \"errorItems\": ").append(error);
//        }
//        sb.append("}");
//        return sb.toString();
        return null;
    }

    public static class ErrorResponseBuilder {

        private Long code = null;
        private String message = null;
        private String description = null;
        private String moreInfo = null;
        private List<ErrorListItem> error;


        public ErrorResponseBuilder() {
            this.error = new ArrayList<>();
        }

        public ErrorResponseBuilder setCode(long code) {
            this.code = code;
            return this;
        }

        public ErrorResponseBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public ErrorResponseBuilder setMoreInfo(String moreInfo) {
            this.moreInfo = moreInfo;
            return this;
        }

        public ErrorResponseBuilder addErrorItem(String code, String msg) {
            ErrorListItem item = new ErrorListItem();
            item.setCode(code);
            item.setMessage(msg);
            this.error.add(item);
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(code);
            errorResponse.setMessage(message);
            errorResponse.setErrorItems(error);
            errorResponse.setDescription(description);
            errorResponse.setMoreInfo(moreInfo);
            return errorResponse;
        }
    }

}


