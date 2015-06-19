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
package org.wso2.carbon.device.mgt.user.core.internal;


import org.osgi.service.component.ComponentContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.device.mgt.user.core.UserManager;
import org.wso2.carbon.device.mgt.user.core.UserManagerImpl;
import org.wso2.carbon.device.mgt.user.core.service.UserManagementService;
import org.wso2.carbon.user.core.service.RealmService;


/**
 * @scr.component name="org.wso2.carbon.device.usermanager" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class DeviceMgtUserServiceComponent {

    private static Log log = LogFactory.getLog(DeviceMgtUserServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing user management core bundle");
            }

            UserManager userMgr = new UserManagerImpl();
            DeviceMgtUserDataHolder.getInstance().setUserManager(userMgr);

            if (log.isDebugEnabled()) {
                log.debug("Registering OSGi service User Management Service");
            }
            /* Registering User Management service */
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(org.wso2.carbon.device.mgt.user.core.UserManager.class.getName(),
                    new org.wso2.carbon.device.mgt.user.core.service.UserManagementService(), null);
            if (log.isDebugEnabled()) {
                log.debug("User management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            String msg = "Error occurred while initializing user management core bundle";
            log.error(msg, e);
        }
    }
    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void setRealmService(RealmService realmService) {
        DeviceMgtUserDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Un sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        DeviceMgtUserDataHolder.getInstance().setRealmService(null);
    }

}
