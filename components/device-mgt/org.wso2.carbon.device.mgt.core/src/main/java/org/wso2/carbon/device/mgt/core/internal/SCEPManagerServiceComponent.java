package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.ios.core.service.IOSEnrollmentService;

/**
 * @scr.component name="org.wso2.carbon.device.ios.enrollment" immediate="true"
 * @scr.reference name="org.wso2.carbon.device.manager"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * policy="dynamic"
 * cardinality="1..n"
 * bind="setDeviceManagementService"
 * unbind="unsetDeviceManagementService"
 * @scr.reference name="org.wso2.carbon.certificate.mgt"
 * interface="org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService"
 * policy="dynamic"
 * cardinality="1..n"
 * bind="setCertificateManagementService"
 * unbind="unsetCertificateManagementService"
 */
public class SCEPManagerServiceComponent {

    private static final Log log = LogFactory.getLog(IOSEnrollmentServiceComponent.class);

    protected void activate(ComponentContext componentContext) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing iOS device management core bundle");
            }

            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(IOSEnrollmentService.class.getName(),
                    IOSEnrollmentService.getInstance(), null);

            if (log.isDebugEnabled()) {
                log.debug("iOS device management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            String msg = "Error occurred while initializing ios device management core bundle";
            log.error(msg, e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating iOS device management core bundle");
        }
    }

    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting device management service provider");
        }
        IOSEnrollmentServiceHolder.getInstance().setDeviceManagementService(deviceManagementService);
    }

    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing device management service provider");
        }

        IOSEnrollmentServiceHolder.getInstance().setDeviceManagementService(null);
    }

    protected void setCertificateManagementService(CertificateManagementService certificateManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting certificate management service");
        }
        IOSEnrollmentServiceHolder.getInstance().setCertificateManagementService(certificateManagementService);
    }

    protected void unsetCertificateManagementService(CertificateManagementService certificateManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing certificate management service");
        }

        IOSEnrollmentServiceHolder.getInstance().setCertificateManagementService(null);
    }

}
