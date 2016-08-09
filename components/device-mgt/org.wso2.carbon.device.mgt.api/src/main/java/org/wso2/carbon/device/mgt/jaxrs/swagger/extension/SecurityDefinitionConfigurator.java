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
package org.wso2.carbon.device.mgt.jaxrs.swagger.extension;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;

import java.util.HashMap;
import java.util.Map;

@SwaggerDefinition(
        basePath = "/api-device-mgt-v1.0",
        host = "localhost:9443"
)
public class SecurityDefinitionConfigurator implements ReaderListener {

    public static final String TOKEN_AUTH_SCHEME = "tokenAuthScheme";

    @Override
    public void beforeScan(Reader reader, Swagger swagger) {

    }

    @Override
    public void afterScan(Reader reader, Swagger swagger) {
        OAuth2Definition tokenScheme = new OAuth2Definition();
        tokenScheme.setType("oauth2");
        tokenScheme.setFlow("password");
        tokenScheme.setTokenUrl("https://" + swagger.getHost() + "/oauth/token");
        tokenScheme.setAuthorizationUrl("https://" + swagger.getHost() + "/oauth/authorize");

        Map<String, SecuritySchemeDefinition> schemes = new HashMap<>();
        schemes.put(TOKEN_AUTH_SCHEME, tokenScheme);

        swagger.setSecurityDefinitions(schemes);
    }

}
