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
package org.wso2.carbon.certificate.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementServiceImpl;

/**
 * @scr.component name="org.wso2.carbon.certificate.mgt" immediate="true"
 */
public class CertificateManagementServiceComponent {

    private static Log log = LogFactory.getLog(CertificateManagementServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing certificate management core bundle");
            }

            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(CertificateManagementService.class.getName(),
                    CertificateManagementServiceImpl.getInstance(), null);

            if (log.isDebugEnabled()) {
                log.debug("Certificate management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing certificate management core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

}
