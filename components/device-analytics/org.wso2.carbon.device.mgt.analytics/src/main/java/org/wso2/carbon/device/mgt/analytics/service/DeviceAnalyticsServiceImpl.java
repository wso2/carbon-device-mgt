package org.wso2.carbon.device.mgt.analytics.service;

import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.DeviceDataPublisher;

public class DeviceAnalyticsServiceImpl implements DeviceAnalyticsService {


	@Override
	public boolean publishEvent(String streamName, String version, Object[] metaDataArray,
			Object[] correlationDataArray, Object[] payloadDataArray) throws DataPublisherConfigurationException {
		return DeviceDataPublisher.getInstance().publishEvent(streamName, version, metaDataArray,
															  correlationDataArray, payloadDataArray);
	}




}
