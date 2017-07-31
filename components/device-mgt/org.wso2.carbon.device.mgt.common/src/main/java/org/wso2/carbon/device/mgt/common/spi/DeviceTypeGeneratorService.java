package org.wso2.carbon.device.mgt.common.spi;

import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;

/**
 * This implementation populates device management service.
 */
public interface DeviceTypeGeneratorService {

    DeviceManagementService populateDeviceManagementService(String deviceTypeName
            , DeviceTypeMetaDefinition deviceTypeMetaDefinition);

}
