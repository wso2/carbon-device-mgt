package org.wso2.carbon.device.mgt.core.factories;

import org.wso2.carbon.device.mgt.core.DeviceApiService;
import org.wso2.carbon.device.mgt.core.impl.DeviceApiServiceImpl;

public class DeviceApiServiceFactory {
    private static final DeviceApiService service = new DeviceApiServiceImpl();

    public static DeviceApiService getDeviceApi() {
        return service;
    }
}
