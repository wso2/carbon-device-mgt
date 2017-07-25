package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.common.spi.DeviceTypeGeneratorService;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;

public class DeviceTypeGeneratorServiceImpl implements DeviceTypeGeneratorService {

    @Override
    public DeviceManagementService populateDeviceManagementService(String deviceTypeName
            , DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        return new HTTPDeviceTypeManagerService(deviceTypeName, deviceTypeMetaDefinition);
    }
}
