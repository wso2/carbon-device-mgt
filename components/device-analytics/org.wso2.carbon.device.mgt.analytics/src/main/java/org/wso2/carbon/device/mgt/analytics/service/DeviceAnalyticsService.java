package org.wso2.carbon.device.mgt.analytics.service;

import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;

import java.util.List;

public interface DeviceAnalyticsService {

    boolean publishEvent(String streamName, String version, Object[] metaDataArray,
                         Object[] correlationDataArray, Object[] payloadDataArray)
            throws DataPublisherConfigurationException;

    List<Record> getAllSensorEventsForDevice(String tableName, String query) throws
                                                                             AnalyticsException;
}
