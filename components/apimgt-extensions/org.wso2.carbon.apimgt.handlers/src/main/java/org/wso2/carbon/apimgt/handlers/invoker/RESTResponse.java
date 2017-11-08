/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.handlers.invoker;


/**
 * RESTResponse class holds the data retrieved from the HTTP invoke response.
 */
public class RESTResponse {
    private String content;
    private int httpStatus;

    /**
     * Constructor
     *
     * @param content     from the REST invoke response
     * @param httpStatus  from the REST invoke response
     */
    RESTResponse(String content, int httpStatus) {
        this.content = content;
        this.httpStatus = httpStatus;
    }


    /**
     * Get contents of the REST invoke response
     *
     * @return contents of the REST invoke response
     */
    public String getContent() {
        return content;
    }

    /**
     * Get the HTTP Status code from REST invoke response
     *
     * @return int HTTP status code
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}
