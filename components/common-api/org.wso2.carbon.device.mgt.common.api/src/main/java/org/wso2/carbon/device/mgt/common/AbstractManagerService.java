package org.wso2.carbon.device.mgt.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * This class provides util methods for manager services
 */
public abstract class AbstractManagerService {
    private static final Log log = LogFactory.getLog(AbstractManagerService.class);

    private PrivilegedCarbonContext ctx;

    /**
     * Returns current username. this method assumes WebappAuthenticationValve is setting username,
     * tenant_domain, tenant_id upon successful authentication.
     * Add context param doAuthentication to "true" on web.xml.
     *
     * @return current username
     */
    protected String getCurrentUserName(){
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    /**
     * Returns OSGi service. Should invoke endTenantFlow() to end the tenant flow once osgi service
     * is consumed.
     * @param <T> OSGi service class
     * @return OSGi service
     */
    @SuppressWarnings("unchecked")
    protected <T> T getServiceProvider(Class<T> osgiServiceClass) throws DeviceManagementException{
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        PrivilegedCarbonContext.startTenantFlow();
        ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(tenantDomain, true);
        if (log.isDebugEnabled()) {
            log.debug("Getting thread local carbon context for tenant domain: " + tenantDomain);
        }
        T clazz = (T) ctx.getOSGiService(osgiServiceClass, null);
        if (clazz == null){
            throw new DeviceManagementException("Requested OSGi service '" + osgiServiceClass.getName() + "' is not initialized!");
        }
        return clazz;
    }

    /**
     * Ends tenant flow.
     */
    protected void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
        ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Tenant flow ended");
        }
    }
}
