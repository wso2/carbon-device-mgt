package org.wso2.carbon.webapp.authenticator.framework;

import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;

import java.io.File;
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
        RealmService realmService = new InMemoryRealmService();
        AuthenticatorFrameworkDataHolder.getInstance().setRealmService(realmService);
        UserStoreManager userStoreManager = AuthenticatorFrameworkDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getUserStoreManager();
        Permission adminPermission = new Permission(PermissionUtils.ADMIN_PERMISSION_REGISTRY_PATH,
                CarbonConstants.UI_PERMISSION_ACTION);
       userStoreManager.addRole(ADMIN_ROLE + "t", new String[] { ADMIN_USER }, new Permission[] { adminPermission });

    }
}
