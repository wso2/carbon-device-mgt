package org.wso2.carbon.device.mgt.analytics.dashboard.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOFactory;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.ndatasource.core.DataSourceService;

@SuppressWarnings("unused")
/**
 * @scr.component name="org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService" immediate="true"
 * @scr.reference name="org.wso2.carbon.ndatasource"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 */
public class GadgetDataServiceComponent {
    private static final Log log = LogFactory.getLog(GadgetDataServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting Device Management Dashboard Analytics Bundle...");
        }
        try {
            DeviceConfigurationManager.getInstance().initConfig();
            DeviceManagementConfig config =
                    DeviceConfigurationManager.getInstance().getDeviceManagementConfig();

            DataSourceConfig dsConfig = config.getDeviceManagementConfigRepository().getDataSourceConfig();
            GadgetDataServiceDAOFactory.init(dsConfig);
            //Register GadgetDataService to expose corresponding data to external parties.
            componentContext.getBundleContext().
                    registerService(GadgetDataService.class.getName(), new GadgetDataServiceImpl(), null);
            if (log.isDebugEnabled()) {
                log.debug("Device Management Dashboard Analytics Bundle has been started successfully");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing the bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Device Management Dashboard Analytics Bundle...");
        }
        //do nothing
    }

    public void setDataSourceService(DataSourceService dataSourceService){

    }

    public void unsetDataSourceService(DataSourceService dataSourceService){

    }
}
