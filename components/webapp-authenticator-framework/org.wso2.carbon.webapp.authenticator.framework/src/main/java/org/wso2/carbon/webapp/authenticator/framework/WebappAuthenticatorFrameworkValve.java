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

import org.apache.axis2.context.MessageContext;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;

import javax.servlet.ServletException;
import java.io.IOException;

public class WebappAuthenticatorFrameworkValve extends CarbonTomcatValve {

    @Override
    public void invoke(Request request, Response response, CompositeValve compositeValve) {
        WebappAuthenticator authenticator = WebappAuthenticatorFactory.getAuthenticator(request);
        WebappAuthenticator.Status status = authenticator.authenticate(request);
        switch (status) {
            case SUCCESS:
            case CONTINUE:
                getNext().invoke(request, response, compositeValve);
            case FAILURE:
                //do something
        }
    }
}
