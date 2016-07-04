package org.wso2.carbon.webapp.authenticator.framework.internal;

//import org.wso2.carbon.device.mgt.core.scep.SCEPManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPManager;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticationValve;
import org.wso2.carbon.webapp.authenticator.framework.WebappAuthenticatorRepository;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import org.wso2.carbon.webapp.authenticator.framework.config.AuthenticatorConfig;
import org.wso2.carbon.webapp.authenticator.framework.config.AuthenticatorConfigService;
import org.wso2.carbon.webapp.authenticator.framework.config.WebappAuthenticatorConfig;
import org.wso2.carbon.webapp.authenticator.framework.config.impl.AuthenticatorConfigServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @scr.component name="org.wso2.carbon.webapp.authenticator" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.certificate.mgt"
 * interface="org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService"
 * policy="dynamic"
 * cardinality="1..n"
 * bind="setCertificateManagementService"
 * unbind="unsetCertificateManagementService"
 * @scr.reference name="org.wso2.carbon.certificate.mgt.core.scep"
 * interface="org.wso2.carbon.certificate.mgt.core.scep.SCEPManager"
 * policy="dynamic"
 * cardinality="1..n"
 * bind="setSCEPManagementService"
 * unbind="unsetSCEPManagementService"
 * @scr.reference name="identity.oauth2.validation.service"
 * interface="org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setOAuth2ValidationService"
 * unbind="unsetOAuth2ValidationService"
 * @scr.reference name="tenant.indexloader"
 * interface="org.wso2.carbon.registry.indexing.service.TenantIndexingLoader"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setTenantIndexLoader"
 * unbind="unsetTenantIndexLoader"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic"
 * bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 */
public class WebappAuthenticatorFrameworkServiceComponent {
    private static final Log log = LogFactory.getLog(WebappAuthenticatorFrameworkServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting Web Application Authenticator Framework Bundle");
        }
        try {
            WebappAuthenticatorConfig.init();
            WebappAuthenticatorRepository repository = new WebappAuthenticatorRepository();
            for (AuthenticatorConfig config : WebappAuthenticatorConfig.getInstance().getAuthenticators()) {
                WebappAuthenticator authenticator =
                        (WebappAuthenticator) Class.forName(config.getClassName()).newInstance();

                if ((config.getParams() != null) && (!config.getParams().isEmpty())) {
                    Properties properties = new Properties();
                    for (AuthenticatorConfig.Parameter param : config.getParams()) {
                        properties.setProperty(param.getName(), param.getValue());
                    }
                    authenticator.setProperties(properties);
                }
                authenticator.init();
                repository.addAuthenticator(authenticator);
            }

            //Register AuthenticatorConfigService to expose webapp-authenticator configs.
            BundleContext bundleContext = componentContext.getBundleContext();
            AuthenticatorConfigService authenticatorConfigService = new AuthenticatorConfigServiceImpl();
            bundleContext.registerService(AuthenticatorConfigService.class.getName(), authenticatorConfigService, null);

            AuthenticatorFrameworkDataHolder.getInstance().setWebappAuthenticatorRepository(repository);

            List<CarbonTomcatValve> valves = new ArrayList<CarbonTomcatValve>();
            valves.add(new WebappAuthenticationValve());
            TomcatValveContainer.addValves(valves);

            if (log.isDebugEnabled()) {
                log.debug("Web Application Authenticator Framework Bundle has been started successfully");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing the bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService acquired");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        AuthenticatorFrameworkDataHolder.getInstance().setRealmService(null);
    }

    protected void setCertificateManagementService(CertificateManagementService certificateManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting certificate management service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setCertificateManagementService(certificateManagementService);
    }

    protected void unsetCertificateManagementService(CertificateManagementService certificateManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing certificate management service");
        }

        AuthenticatorFrameworkDataHolder.getInstance().setCertificateManagementService(null);
    }

    protected void setSCEPManagementService(SCEPManager scepManager) {
        if (log.isDebugEnabled()) {
            log.debug("Setting SCEP management service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setScepManager(scepManager);
    }

    protected void unsetSCEPManagementService(SCEPManager scepManager) {
        if (log.isDebugEnabled()) {
            log.debug("Removing SCEP management service");
        }

        AuthenticatorFrameworkDataHolder.getInstance().setScepManager(null);
    }

    /**
     * Sets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    protected void setOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting OAuth2TokenValidationService Service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setOAuth2TokenValidationService(tokenValidationService);
    }

    /**
     * Unsets OAuth2TokenValidation Service.
     *
     * @param tokenValidationService An instance of OAuth2TokenValidationService
     */
    protected void unsetOAuth2ValidationService(OAuth2TokenValidationService tokenValidationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting OAuth2TokenValidationService Service");
        }
        AuthenticatorFrameworkDataHolder.getInstance().setOAuth2TokenValidationService(null);
    }

    protected void setTenantIndexLoader(TenantIndexingLoader tenantIndexLoader) {
        AuthenticatorFrameworkDataHolder.getInstance().setTenantIndexingLoader(tenantIndexLoader);
    }

    protected void unsetTenantIndexLoader(TenantIndexingLoader tenantIndexLoader) {
        AuthenticatorFrameworkDataHolder.getInstance().setTenantIndexingLoader(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        AuthenticatorFrameworkDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        AuthenticatorFrameworkDataHolder.getInstance().setTenantRegistryLoader(null);
    }
}
