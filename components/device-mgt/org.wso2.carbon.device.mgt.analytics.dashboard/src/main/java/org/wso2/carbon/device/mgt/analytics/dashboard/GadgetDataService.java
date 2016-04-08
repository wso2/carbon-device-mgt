package org.wso2.carbon.device.mgt.analytics.dashboard;

/**
 * To be updated...
 */
public interface GadgetDataService {
    @SuppressWarnings("unused")
    int getTotalFilteredDeviceCount(String[] filters);

    @SuppressWarnings("unused")
    int getNonCompliantDeviceCount(String[] filters);

    @SuppressWarnings("unused")
    int getUnmonitoredDeviceCount(String[] filters);
}
