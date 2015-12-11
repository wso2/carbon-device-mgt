/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.dynamic.client.web.proxy.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.ws.rs.core.Response;

/**
 * Created by harshan on 12/10/15.
 */
public class DCRProxyUtils {

    public static ConfigurationContextService getConfigurationContextService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        return  (ConfigurationContextService) ctx.getOSGiService(ConfigurationContextService.class, null);
    }

    public static Response.Status getResponseStatus(int statusCode) {
        switch (statusCode) {
            case 200 :
                return Response.Status.OK;
            case 201 :
                return Response.Status.CREATED;
            case 400 :
                return Response.Status.BAD_REQUEST;
            case 500 :
                return Response.Status.INTERNAL_SERVER_ERROR;
        }
        return Response.Status.ACCEPTED;
    }

    public static String getKeyManagerHost()
            throws IllegalArgumentException {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration.
                                                       getAuthenticatorConfig(
                                                               Constants.ConfigurationProperties.AUTHENTICATOR_NAME);
        if (authenticatorConfig != null && authenticatorConfig.getParameters() != null) {
            return getHostName(authenticatorConfig.getParameters().get(Constants.ConfigurationProperties.
                                                                               AUTHENTICATOR_CONFIG_HOST_URL));

        }else{
            throw new IllegalArgumentException("Configuration parameters need to be defined in Authenticators.xml.");
        }
    }

    private static String getHostName(String host) {
        if (host != null && !host.isEmpty()) {
            if (host.contains("https://")) {
                return host.replace("https://","");
            }
        } else {
            throw new IllegalArgumentException("Remote Host parameter must defined in Authenticators.xml.");
        }
        return null;
    }
}
