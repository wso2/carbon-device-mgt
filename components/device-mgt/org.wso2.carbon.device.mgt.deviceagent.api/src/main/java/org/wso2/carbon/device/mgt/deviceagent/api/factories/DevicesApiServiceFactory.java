package org.wso2.carbon.device.mgt.deviceagent.api.factories;

import org.wso2.carbon.device.mgt.deviceagent.api.DevicesApiService;
import org.wso2.carbon.device.mgt.deviceagent.api.impl.DevicesApiServiceImpl;

public class DevicesApiServiceFactory {
    private static final DevicesApiService service = new DevicesApiServiceImpl();

    public static DevicesApiService getDevicesApi() {
        return service;
    }
}
