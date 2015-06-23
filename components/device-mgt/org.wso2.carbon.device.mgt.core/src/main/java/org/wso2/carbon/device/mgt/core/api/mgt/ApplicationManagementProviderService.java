package org.wso2.carbon.device.mgt.core.api.mgt;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;

import java.util.List;

public interface ApplicationManagementProviderService extends ApplicationManager {

    void updateApplicationListInstallInDevice(DeviceIdentifier deviceIdentifier, List<Application> applications)
            throws ApplicationManagementException;
}
