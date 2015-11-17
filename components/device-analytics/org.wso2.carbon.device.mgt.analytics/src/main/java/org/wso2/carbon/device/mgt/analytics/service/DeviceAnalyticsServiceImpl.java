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
import org.wso2.carbon.device.mgt.analytics.DeviceDataPublisher;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.internal.DeviceAnalyticsDataHolder;

import java.util.ArrayList;
import java.util.List;

public class DeviceAnalyticsServiceImpl implements DeviceAnalyticsService {
    private static Log log = LogFactory.getLog(DeviceAnalyticsServiceImpl.class);

    @Override
    public boolean publishEvent(String streamName, String version, Object[] metaDataArray,
                                Object[] correlationDataArray, Object[] payloadDataArray)
            throws DataPublisherConfigurationException {
        return DeviceDataPublisher.getInstance().publishEvent(streamName, version, metaDataArray,
                                                              correlationDataArray, payloadDataArray);
    }

    @Override
    public List<Record> getAllSensorEventsForDevice(String tableName, String query)
            throws AnalyticsException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        AnalyticsDataAPI analyticsDataAPI = DeviceAnalyticsDataHolder.getInstance().getAnalyticsDataAPI();
        int eventCount = analyticsDataAPI.searchCount(tenantId, tableName, query);
        if (eventCount == 0) {
            return new ArrayList<>();
        }
        AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
        drillDownRequest.setScoreFunction("1 - (time / 10000000000)");
        drillDownRequest.setQuery(query);
        drillDownRequest.setTableName(tableName);
        drillDownRequest.setRecordCount(eventCount);
        List<SearchResultEntry> resultEntries = analyticsDataAPI.drillDownSearch(tenantId, drillDownRequest);
        List<String> recordIds = getRecordIds(resultEntries);
        AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, null, recordIds);
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