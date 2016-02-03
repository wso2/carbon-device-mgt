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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.device.mgt.analytics.datapublisher.DeviceDataPublisher;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.internal.DeviceAnalyticsDataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of Osgi Service which can be used to publish and retireved
 * event/records.
 */
public class DeviceAnalyticsServiceImpl implements DeviceAnalyticsService {
	private static Log log = LogFactory.getLog(DeviceAnalyticsServiceImpl.class);
	private static final String SORT_SCORE_VALUE = "1 - (time / 10000000000)";

	/**
	 * @param streamName           is the name of the stream that the data needs to pushed
	 * @param version              is the version of the stream
	 * @param metaDataArray        - meta data that needs to pushed
	 * @param correlationDataArray - correlation data that needs to be pushed
	 * @param payloadDataArray     - payload data that needs to be pushed
	 * @return
	 * @throws DataPublisherConfigurationException
	 */
	@Override
	public boolean publishEvent(String streamName, String version, Object[] metaDataArray,
								Object[] correlationDataArray, Object[] payloadDataArray)
			throws DataPublisherConfigurationException {
		DataPublisher dataPublisher = DeviceDataPublisher.getInstance().getDataPublisher();
		if (dataPublisher != null) {
			String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, version);
			return dataPublisher.tryPublish(streamId, System.currentTimeMillis(),metaDataArray, correlationDataArray,
									 payloadDataArray);

		} else {
			return false;
		}
	}

	/**
	 * @param tableName is the name of the table that events need to be retrieved
	 * @param query     is query to be executed.
	 * @return
	 * @throws AnalyticsException
	 */
	@Override
	public List<Record> getAllEventsForDevice(String tableName, String query)
			throws AnalyticsException {

		int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
		AnalyticsDataAPI analyticsDataAPI =
				DeviceAnalyticsDataHolder.getInstance().getAnalyticsDataAPI();
		int eventCount = analyticsDataAPI.searchCount(tenantId, tableName, query);
		if (eventCount == 0) {
			return new ArrayList<>();
		}
		AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
		drillDownRequest.setScoreFunction(SORT_SCORE_VALUE);
		drillDownRequest.setQuery(query);
		drillDownRequest.setTableName(tableName);
		drillDownRequest.setRecordCount(eventCount);
		List<SearchResultEntry> resultEntries = analyticsDataAPI.drillDownSearch(tenantId,
																				 drillDownRequest);
		List<String> recordIds = getRecordIds(resultEntries);
		AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, null,
															  recordIds);
		return AnalyticsDataServiceUtils.listRecords(analyticsDataAPI, response);
	}

	private List<String> getRecordIds(List<SearchResultEntry> searchResults) {
		List<String> ids = new ArrayList<>();
		for (SearchResultEntry searchResult : searchResults) {
			ids.add(searchResult.getId());
		}
		return ids;
	}

}