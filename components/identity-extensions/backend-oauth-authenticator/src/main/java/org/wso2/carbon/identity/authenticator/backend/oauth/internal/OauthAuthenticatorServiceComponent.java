/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.identity.authenticator.backend.oauth.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.identity.authenticator.backend.oauth.OauthAuthenticator;



public class OauthAuthenticatorServiceComponent  implements BundleActivator {

    private ServiceRegistration pipServiceRegRef;
    private static final Log log = LogFactory.getLog(OauthAuthenticatorServiceComponent
            .class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        log.info("Initiating");
        try {
            OauthAuthenticator oauthAuthenticator = new OauthAuthenticator();
            pipServiceRegRef =  bundleContext.registerService(CarbonServerAuthenticator.class.getName(),
                                          oauthAuthenticator, null);
            if (log.isDebugEnabled()) {
                log.debug("OAuth Authenticator bundle is activated");
            }
        } catch (Throwable e) {
            log.fatal(" Error while activating OAuth authenticator ", e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("OAuth Authenticator bundle is deactivated");
        }
        pipServiceRegRef.unregister();
    }

}
