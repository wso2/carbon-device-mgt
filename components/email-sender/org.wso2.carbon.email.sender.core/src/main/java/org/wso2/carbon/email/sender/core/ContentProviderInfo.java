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
package org.wso2.carbon.email.sender.core;

import java.util.Map;

public class ContentProviderInfo {

    private String template;
    private Map<String, TypedValue<Class<?>, Object>> params;

    public ContentProviderInfo(final String template, final Map<String, TypedValue<Class<?>, Object>> params) {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        this.template = template;
        if (params == null) {
            throw new IllegalArgumentException("Place-holder parameter map cannot be null");
        }
        this.params = params;
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, TypedValue<Class<?>, Object>> getParams() {
        return params;
    }

    public void addParam(String name, TypedValue<Class<?>, Object> param) {
        params.put(name, param);
    }

    public TypedValue<Class<?>, Object> getParam(String name) {
        return params.get(name);
    }

}
