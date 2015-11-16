package org.wso2.carbon.device.mgt.analytics.internal;

import org.wso2.carbon.analytics.api.AnalyticsDataAPI;

public class DeviceAnalyticsDataHolder {
	private static DeviceAnalyticsDataHolder thisInstance = new DeviceAnalyticsDataHolder();
	private AnalyticsDataAPI analyticsDataAPI;
	private DeviceAnalyticsDataHolder() {
	}

	public static DeviceAnalyticsDataHolder getInstance() {
		return thisInstance;
	}

	public AnalyticsDataAPI getAnalyticsDataAPI() {
		return analyticsDataAPI;
	}

	public void setAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
		this.analyticsDataAPI = analyticsDataAPI;
	}
}
