package org.wso2.carbon.certificate.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPManager;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPManagerImpl;

/**
 * @scr.component name="org.wso2.carbon.certificate.mgt.core.scep" immediate="true"
 */
public class SCEPManagerServiceComponent {

    private static final Log log = LogFactory.getLog(SCEPManagerServiceComponent.class);

    protected void activate(ComponentContext componentContext) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing SCEP core bundle");
            }

            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(SCEPManager.class.getName(),
                    new SCEPManagerImpl(), null);

            if (log.isDebugEnabled()) {
                log.debug("SCEP core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            String msg = "Error occurred while initializing SCEP core bundle";
            log.error(msg, e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating SCEP core bundle");
        }
    }

}
