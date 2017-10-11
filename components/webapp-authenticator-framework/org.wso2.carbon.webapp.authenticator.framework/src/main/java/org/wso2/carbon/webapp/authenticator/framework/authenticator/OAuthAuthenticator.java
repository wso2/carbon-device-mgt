/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationException;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.Utils.Utils;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuth2TokenValidator;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthTokenValidationException;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthValidationResponse;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAuthAuthenticator implements WebappAuthenticator {
    private static final Pattern PATTERN = Pattern.compile("[B|b]earer\\s");
    private Properties properties;
    private OAuth2TokenValidator tokenValidator;
    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);

    public void init() {
        this.tokenValidator = Utils.initAuthenticators(this.properties);
    }

    public boolean canHandle(org.apache.catalina.connector.Request request) {
        MessageBytes authorization = request.getCoyoteRequest().getMimeHeaders().getValue("Authorization");
        if (authorization != null) {
            authorization.toBytes();
            ByteChunk authBC = authorization.getByteChunk();
            String tokenValue = authBC.toString();
            Matcher matcher = PATTERN.matcher(tokenValue);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    public AuthenticationInfo authenticate(org.apache.catalina.connector.Request request, Response response) {
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        if ((requestUri == null) || ("".equals(requestUri))) {
            authenticationInfo.setStatus(WebappAuthenticator.Status.CONTINUE);
            return authenticationInfo;
        }
        StringTokenizer tokenizer = new StringTokenizer(requestUri, "/");
        String context = tokenizer.nextToken();
        if ((context == null) || (context.isEmpty())) {
            authenticationInfo.setStatus(WebappAuthenticator.Status.CONTINUE);
        }
        try {
            String bearerToken = getBearerToken(request);
            String resource = requestUri + ":" + requestMethod;
            OAuthValidationResponse oAuthValidationResponse = this.tokenValidator.validateToken(bearerToken, resource);
            authenticationInfo = Utils.setAuthenticationInfo(oAuthValidationResponse, authenticationInfo);
        } catch (AuthenticationException e) {
            log.error("Failed to authenticate the incoming request", e);
        } catch (OAuthTokenValidationException e) {
            log.error("Failed to authenticate the incoming request due to oauth token validation error.", e);
        }
        return authenticationInfo;
    }

    public String getName() {
        return "OAuth";
    }

    public String getProperty(String name) {
        if (this.properties == null) {
            return null;
        }
        return this.properties.getProperty(name);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private String getBearerToken(org.apache.catalina.connector.Request request) {
        MessageBytes authorization = request.getCoyoteRequest().getMimeHeaders().getValue("Authorization");

        String tokenValue = null;
        if (authorization != null) {
            authorization.toBytes();
            ByteChunk authBC = authorization.getByteChunk();
            tokenValue = authBC.toString();
            Matcher matcher = PATTERN.matcher(tokenValue);
            if (matcher.find()) {
                tokenValue = tokenValue.substring(matcher.end());
            }
        }
        return tokenValue;
    }

}
