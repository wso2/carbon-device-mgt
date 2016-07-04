package org.wso2.carbon.device.mgt.core.internal;

///**
// * @scr.component name="org.wso2.carbon.certificate.mgt.core.scep" immediate="true"
// * @scr.reference name="app.mgt.service"
// * interface="org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService"
// * cardinality="1..1"
// * policy="dynamic"
// * bind="setApplicationManagementProviderService"
// * unbind="unsetApplicationManagementProviderService"
// */
//public class SCEPManagerServiceComponent {
//
//    private static final Log log = LogFactory.getLog(SCEPManagerServiceComponent.class);
//
//    protected void activate(ComponentContext componentContext) {
//
//        try {
//            if (log.isDebugEnabled()) {
//                log.debug("Initializing SCEP core bundle");
//            }
//
//            BundleContext bundleContext = componentContext.getBundleContext();
//            bundleContext.registerService(SCEPManager.class.getName(),
//                    new SCEPManagerImpl(), null);
//
//            if (log.isDebugEnabled()) {
//                log.debug("SCEP core bundle has been successfully initialized");
//            }
//        } catch (Throwable e) {
//            String msg = "Error occurred while initializing SCEP core bundle";
//            log.error(msg, e);
//        }
//    }
//
//    protected void deactivate(ComponentContext ctx) {
//        if (log.isDebugEnabled()) {
//            log.debug("Deactivating SCEP core bundle");
//        }
//    }
//
//    protected void unsetApplicationManagementProviderService(ApplicationManagementProviderService
//                                                                     applicationManagementProviderService) {
//        //do nothing
//    }
//
//    protected void setApplicationManagementProviderService(ApplicationManagementProviderService
//                                                                   applicationManagementProviderService) {
//        //do nothing
//    }
//
//}
