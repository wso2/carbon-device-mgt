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

import org.apache.http.Header;

/**
 * RESTResponse class holds the data retrieved from the HTTP invoke response.
 */
public class RESTResponse {
    private String contentType;
    private String content;
    private Header[] headers;
    private int httpStatus;

    /**
     * Constructor
     *
     * @param contentType from the REST invoke response
     * @param content     from the REST invoke response
     * @param headers     from the REST invoke response
     * @param httpStatus  from the REST invoke response
     */
    public RESTResponse(String contentType, String content, Header[] headers, int httpStatus) {
        this.contentType = contentType;
        this.content = content;
        this.headers = headers;
        this.httpStatus = httpStatus;
    }

    /**
     * Get the content type of the EST invoke response
     *
     * @return String content type of the response
     */
    public String getContentType() {
        return contentType;
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
     * Get headers of the REST invoke response
     *
     * @return headers of the REST invoke response
     */
    public Header[] getHeaders() {
        return headers;
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
