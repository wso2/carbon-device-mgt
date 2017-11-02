package org.wso2.carbon.device.mgt.core.factories;

import org.wso2.carbon.device.mgt.core.DevicesApiService;
import org.wso2.carbon.device.mgt.core.impl.DevicesApiServiceImpl;

public class DevicesApiServiceFactory {
    private static final DevicesApiService service = new DevicesApiServiceImpl();

    public static DevicesApiService getDevicesApi() {
        return service;
    }
}
