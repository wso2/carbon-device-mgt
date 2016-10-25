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

package org.wso2.carbon.apimgt.application.extension.api.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the information related to permission.
 */
@XmlRootElement (name = "Permission")
public class Permission {

    private String path; // permission string
    private String url; // url of the resource
    private String method; // http method

    public String getPath() {
        return path;
    }

    @XmlElement (name = "path", required = true)
    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    @XmlElement (name = "url", required = true)
    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    @XmlElement (name = "method", required = true)
    public void setMethod(String method) {
        this.method = method;
    }
}
