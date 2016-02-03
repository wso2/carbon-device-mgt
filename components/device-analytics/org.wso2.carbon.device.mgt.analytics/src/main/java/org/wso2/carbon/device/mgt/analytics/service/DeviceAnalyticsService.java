/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.analytics.service;

import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;

import java.util.List;

/**
 * This service can be used to publish and retreive data from the DAS.
 */
public interface DeviceAnalyticsService {

    /**
     * This is used to publish an event to DAS.
     * @param streamName is the name of the stream that the data needs to pushed
     * @param version is the version of the stream
     * @param metaDataArray - meta data that needs to pushed
     * @param correlationDataArray - correlation data that needs to be pushed
     * @param payloadDataArray - payload data that needs to be pushed
     * @return
     * @throws DataPublisherConfigurationException
     */
    boolean publishEvent(String streamName, String version, Object[] metaDataArray,
                         Object[] correlationDataArray, Object[] payloadDataArray)
            throws DataPublisherConfigurationException;

    /**
     * This service can be used to retrieve all the event for the query.
     * @param tableName is the name of the table that events need to be retrieved
     * @param query is query to be executed.
     * @return the record list
     * @throws AnalyticsException
     */
    List<Record> getAllEventsForDevice(String tableName, String query) throws
                                                                             AnalyticsException;
}
