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

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.core.authenticate.APITokenValidator;
import org.wso2.carbon.apimgt.core.gateway.APITokenAuthenticator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationException;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationFrameworkUtil;
import org.wso2.carbon.webapp.authenticator.framework.Constants;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticator;

import java.util.StringTokenizer;

public class OAuthAuthenticator implements WebappAuthenticator {

    private static final String OAUTH_AUTHENTICATOR = "OAuth";
    private static APITokenAuthenticator authenticator = new APITokenAuthenticator();

    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);

    @Override
    public boolean isAuthenticated(Request request) {
        return false;
    }

    @Override
    public Status authenticate(Request request, Response response) {
        String requestUri = request.getRequestURI();
        if (requestUri == null || "".equals(requestUri)) {
            return Status.CONTINUE;
        }

        StringTokenizer tokenizer = new StringTokenizer(requestUri, "/");
        String context = request.getContextPath();
        if (context == null || "".equals(context)) {
            context = tokenizer.nextToken();
            if (context == null || "".equals(context)) {
                return Status.CONTINUE;
            }
        }

//        boolean isContextCached = false;
//        if (APIUtil.getAPIContextCache().get(context) != null) {
//            isContextCached = Boolean.parseBoolean(APIUtil.getAPIContextCache().get(context).toString());
//        }
//        if (!isContextCached) {
//            return Status.CONTINUE;
//        }

        try {
            String apiVersion = tokenizer.nextToken();
            String domain = request.getHeader(APITokenValidator.getAPIManagerClientDomainHeader());
            String authLevel = authenticator.getResourceAuthenticationScheme(context, apiVersion,
                    request.getRequestURI(), request.getMethod());

            if (Constants.NO_MATCHING_AUTH_SCHEME.equals(authLevel)) {
                AuthenticationFrameworkUtil.handleNoMatchAuthScheme(request, response, request.getMethod(),
                        apiVersion, context);
                return Status.CONTINUE;
            } else {
                String bearerToken = this.getBearerToken(request);
                boolean isAuthenticated =
                        AuthenticationFrameworkUtil.doAuthenticate(context, apiVersion, bearerToken, authLevel, domain);
                return (isAuthenticated) ? Status.SUCCESS : Status.FAILURE;
            }
        } catch (APIManagementException e) {
            log.error("Error occurred while key validation", e);
            return Status.FAILURE;
        } catch (AuthenticationException e) {
            log.error("Failed to authenticate the incoming request", e);
            return Status.FAILURE;
        }
    }

    @Override
    public String getName() {
        return OAuthAuthenticator.OAUTH_AUTHENTICATOR;
    }

    private String getBearerToken(Request request) {
        MessageBytes authorization =
                request.getCoyoteRequest().getMimeHeaders().getValue(Constants.HTTPHeaders.HEADER_HTTP_AUTHORIZATION);
        String tokenValue = null;
        if (authorization != null) {
            authorization.toBytes();
            ByteChunk authBC = authorization.getByteChunk();
            if (authBC.startsWithIgnoreCase("bearer ", 0)) {
                String bearerToken = authBC.toString();
                tokenValue = bearerToken.substring(8, bearerToken.length() - 1);
            }
        }
        return tokenValue;
    }

}
