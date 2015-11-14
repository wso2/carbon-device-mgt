package org.wso2.carbon.device.mgt.analytics.service;

import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;

public interface DeviceAnalyticsService {

	public boolean publishEvent(String streamName, String version, Object[] metaDataArray,
				 Object[] correlationDataArray, Object[] payloadDataArray) throws DataPublisherConfigurationException;
}
