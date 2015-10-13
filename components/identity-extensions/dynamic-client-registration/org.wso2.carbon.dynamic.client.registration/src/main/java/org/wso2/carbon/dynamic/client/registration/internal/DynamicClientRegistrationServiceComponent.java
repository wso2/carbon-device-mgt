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

package org.wso2.carbon.dynamic.client.registration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationService;
import org.wso2.carbon.dynamic.client.registration.impl.DynamicClientRegistrationServiceImpl;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

/**
 * @scr.component name="org.wso2.carbon.dynamic.client.registration" immediate="true"
 * @scr.reference name="identity.application.management.service"
 * interface="org.wso2.carbon.identity.application.mgt.ApplicationManagementService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setApplicationManagementService"
 * unbind="unsetApplicationManagementService"
 */
public class DynamicClientRegistrationServiceComponent {

    private static final Log log = LogFactory.getLog(DynamicClientRegistrationServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        if(log.isDebugEnabled()){
            log.debug("Starting DynamicClientRegistrationServiceComponent");
        }
        DynamicClientRegistrationService dynamicClientRegistrationService =
                new DynamicClientRegistrationServiceImpl();
        componentContext.getBundleContext().registerService(
                DynamicClientRegistrationService.class.getName(), dynamicClientRegistrationService, null);
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        if(log.isDebugEnabled()){
            log.debug("Stopping DynamicClientRegistrationServiceComponent");
        }
    }

    /**
     * Sets ApplicationManagement Service.
     *
     * @param applicationManagementService An instance of ApplicationManagementService
     */
    protected void setApplicationManagementService(ApplicationManagementService
                                                           applicationManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ApplicationManagement Service");
        }
        DynamicClientRegistrationDataHolder.getInstance().
                setApplicationManagementService(applicationManagementService);
    }

    /**
     * Unsets ApplicationManagement Service.
     *
     * @param applicationManagementService An instance of ApplicationManagementService
     */
    protected void unsetApplicationManagementService(ApplicationManagementService
                                                             applicationManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting ApplicationManagement Service");
        }
        DynamicClientRegistrationDataHolder.getInstance().setApplicationManagementService(null);
    }

}
