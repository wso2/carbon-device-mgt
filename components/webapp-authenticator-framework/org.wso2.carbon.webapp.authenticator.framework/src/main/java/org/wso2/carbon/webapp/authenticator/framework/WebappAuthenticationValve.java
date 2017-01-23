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
package org.wso2.carbon.webapp.authenticator.framework;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.encoder.Encode;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import org.wso2.carbon.webapp.authenticator.framework.authorizer.WebappTenantAuthorizer;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.StringTokenizer;

public class WebappAuthenticationValve extends CarbonTomcatValve {

    private static final Log log = LogFactory.getLog(WebappAuthenticationValve.class);
    private static HashMap<String, String> nonSecuredEndpoints = new HashMap<>();

    @Override
    public void invoke(Request request, Response response, CompositeValve compositeValve) {

        if (this.isContextSkipped(request) ||  this.skipAuthentication(request)) {
            this.getNext().invoke(request, response, compositeValve);
            return;
        }

        WebappAuthenticator authenticator = WebappAuthenticatorFactory.getAuthenticator(request);
        if (authenticator == null) {
            String msg = "Failed to load an appropriate authenticator to authenticate the request";
            AuthenticationFrameworkUtil.handleResponse(request, response, HttpServletResponse.SC_UNAUTHORIZED, msg);
            return;
        }
        AuthenticationInfo authenticationInfo = authenticator.authenticate(request, response);
        if (isManagedAPI(request) && (authenticationInfo.getStatus() == WebappAuthenticator.Status.CONTINUE ||
                authenticationInfo.getStatus() == WebappAuthenticator.Status.SUCCESS)) {
            WebappAuthenticator.Status status = WebappTenantAuthorizer.authorize(request, authenticationInfo);
            authenticationInfo.setStatus(status);
        }
        if (authenticationInfo.getTenantId() != -1) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantId(authenticationInfo.getTenantId());
                privilegedCarbonContext.setTenantDomain(authenticationInfo.getTenantDomain());
                privilegedCarbonContext.setUsername(authenticationInfo.getUsername());
                this.processRequest(request, response, compositeValve, authenticationInfo);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            this.processRequest(request, response, compositeValve, authenticationInfo);
        }
    }

    private boolean skipAuthentication(Request request) {
        String param = request.getContext().findParameter("doAuthentication");
        return (param == null || !Boolean.parseBoolean(param) || isNonSecuredEndPoint(request));
    }

    private boolean isManagedAPI(Request request) {
        String param = request.getContext().findParameter("managed-api-enabled");
        return (param != null && Boolean.parseBoolean(param));
    }

    private boolean isContextSkipped(Request request) {
        String ctx = request.getContext().getPath();
        if (ctx == null || "".equals(ctx)) {
            ctx = request.getContextPath();
            if (ctx == null || "".equals(ctx)) {
                String requestUri = request.getRequestURI();
                if ("/".equals(requestUri)) {
                    return true;
                }
                StringTokenizer tokenizer = new StringTokenizer(request.getRequestURI(), "/");
                if (!tokenizer.hasMoreTokens()) {
                    return false;
                }
                ctx = tokenizer.nextToken();
            }
        }
        return ("carbon".equalsIgnoreCase(ctx) || "services".equalsIgnoreCase(ctx));
    }

    private boolean isNonSecuredEndPoint(Request request) {
        String uri = request.getRequestURI();
        if(!uri.endsWith("/")) {
            uri = uri + "/";
        }
        String contextPath = request.getContextPath();
        //Check the contextPath in nonSecuredEndpoints. If so it means cache is not populated for this web-app.
        if (!nonSecuredEndpoints.containsKey(contextPath)) {
            String param = request.getContext().findParameter("nonSecuredEndPoints");
            String skippedEndPoint;
            if (param != null && !param.isEmpty()) {
                //Add the nonSecured end-points to cache
                StringTokenizer tokenizer = new StringTokenizer(param, ",");
                nonSecuredEndpoints.put(contextPath, "true");
                while (tokenizer.hasMoreTokens()) {
                    skippedEndPoint = tokenizer.nextToken();
                    skippedEndPoint = skippedEndPoint.replace("\n", "").replace("\r", "").trim();
                    if(!skippedEndPoint.endsWith("/")) {
                        skippedEndPoint = skippedEndPoint + "/";
                    }
                    nonSecuredEndpoints.put(skippedEndPoint, "true");
                }
            }
        }
        return nonSecuredEndpoints.containsKey(uri);
    }

    private void processRequest(Request request, Response response, CompositeValve compositeValve,
                                AuthenticationInfo authenticationInfo) {
        switch (authenticationInfo.getStatus()) {
            case SUCCESS:
            case CONTINUE:
                this.getNext().invoke(request, response, compositeValve);
                break;
            case FAILURE:
                String msg = "Failed to authorize incoming request";
                if (authenticationInfo.getMessage() != null && !authenticationInfo.getMessage().isEmpty()) {
                    msg = authenticationInfo.getMessage();
                    response.setHeader("WWW-Authenticate", msg);
                }
                if (log.isDebugEnabled()) {
                    log.debug(msg + " , API : " + Encode.forUriComponent(request.getRequestURI()));
                }
                AuthenticationFrameworkUtil.
                        handleResponse(request, response, HttpServletResponse.SC_UNAUTHORIZED, msg);
                break;
        }
    }
}