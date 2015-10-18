/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.authenticator.framework.authorizer;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationFrameworkUtil;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;

import javax.servlet.http.HttpServletResponse;

public class PermissionAuthorizationValve extends CarbonTomcatValve {

    private static final Log log = LogFactory.getLog(PermissionAuthorizationValve.class);
    private static final String AUTHORIZATION_ENABLED = "authorization-enabled";


    @Override
    public void invoke(Request request, Response response, CompositeValve compositeValve) {

        String permissionStatus = request.getContext().findParameter(AUTHORIZATION_ENABLED);
        if (permissionStatus == null || permissionStatus.isEmpty()) {
            this.processResponse(request, response, compositeValve, WebappAuthenticator.Status.CONTINUE);
            return;
        }
        // check whether the permission checking function is enabled in web.xml
        boolean isEnabled = Boolean.valueOf(permissionStatus);
        if (!isEnabled) {
            this.processResponse(request, response, compositeValve, WebappAuthenticator.Status.CONTINUE);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Checking permission of request: " + request.getRequestURI());
        }
        PermissionAuthorizer permissionAuthorizer = new PermissionAuthorizer();
        WebappAuthenticator.Status status = permissionAuthorizer.authorize(request, response);
        this.processResponse(request, response, compositeValve, status);
    }

    private void processResponse(Request request, Response response, CompositeValve compositeValve,
                                 WebappAuthenticator.Status status) {
        switch (status) {
            case SUCCESS:
            case CONTINUE:
                this.getNext().invoke(request, response, compositeValve);
                break;
            case FAILURE:
                String msg = "Failed to authorize incoming request";
                log.error(msg);
                AuthenticationFrameworkUtil.handleResponse(request, response, HttpServletResponse.SC_UNAUTHORIZED, msg);
                break;
        }
    }
}
