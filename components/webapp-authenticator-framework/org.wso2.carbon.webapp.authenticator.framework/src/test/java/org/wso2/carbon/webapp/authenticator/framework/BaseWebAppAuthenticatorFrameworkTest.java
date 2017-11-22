/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.webapp.authenticator.framework;

import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.util.TestTenantIndexingLoader;
import org.wso2.carbon.webapp.authenticator.framework.util.TestTenantRegistryLoader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static org.wso2.carbon.security.SecurityConstants.ADMIN_USER;
import static org.wso2.carbon.utils.ServerConstants.ADMIN_ROLE;

/**
 * This is the base class for starting up the relevant services
 */
public class BaseWebAppAuthenticatorFrameworkTest {
    public final static String AUTHORIZATION_HEADER = "Authorization";

    @BeforeSuite
    public void init() throws RegistryException, UserStoreException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("carbon-home");
        if (resourceUrl != null) {
            File carbonHome = new File(resourceUrl.getFile());
            System.setProperty("carbon.home", carbonHome.getAbsolutePath());
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantId(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
        CarbonCoreDataHolder.getInstance().setRegistryService(getRegistryService());
        AuthenticatorFrameworkDataHolder.getInstance().setTenantRegistryLoader(new TestTenantRegistryLoader());
        AuthenticatorFrameworkDataHolder.getInstance().setTenantIndexingLoader(new TestTenantIndexingLoader());
    }

    /**
     * To get the registry service.
     * @return RegistryService
     * @throws RegistryException Registry Exception
     */
    private  RegistryService getRegistryService() throws RegistryException, UserStoreException {
        RealmService realmService = new InMemoryRealmService();
        AuthenticatorFrameworkDataHolder.getInstance().setRealmService(realmService);
        UserStoreManager userStoreManager = AuthenticatorFrameworkDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getUserStoreManager();
        Permission adminPermission = new Permission(PermissionUtils.ADMIN_PERMISSION_REGISTRY_PATH,
                CarbonConstants.UI_PERMISSION_ACTION);
        userStoreManager.addRole(ADMIN_ROLE + "t", new String[] { ADMIN_USER }, new Permission[] { adminPermission });
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = BaseWebAppAuthenticatorFrameworkTest.class.getClassLoader()
                .getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }
}
