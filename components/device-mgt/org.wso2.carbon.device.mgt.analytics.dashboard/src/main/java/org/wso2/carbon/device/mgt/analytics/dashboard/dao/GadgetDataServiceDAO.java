package org.wso2.carbon.device.mgt.analytics.dashboard.dao;

public interface GadgetDataServiceDAO {

    // Total devices related analytics

    /**
     * Method to get total filtered device count from a particular tenant.
     *
     * @param filters List of filters to be applied in getting
     * total filtered device count.
     *
     * @return Total filtered device count.
     */
    @SuppressWarnings("unused")
    int getTotalFilteredDeviceCount(String[] filters) throws GadgetDataServiceDAOException;

    // Security-concerns related analytics

    /**
     * Method to get non-compliant device count.
     *
     * @param filters List of filters to be applied in getting
     * non-compliant device count.
     *
     * @return Non-compliant device count.
     */
    int getNonCompliantDeviceCount(String[] filters);

    /**
     * Method to get unmonitored device count.
     *
     * @param filters List of filters to be applied in getting
     * unmonitored device count.
     *
     * @return Unmonitored device count.
     */
    int getUnmonitoredDeviceCount(String[] filters);

    // Device Groupings related analytics

    @SuppressWarnings("unused")
    int getAndroidDeviceCount(String[] filters);
    @SuppressWarnings("unused")
    int getBYODDeviceCount(String[] filters);
    @SuppressWarnings("unused")
    int getCOPEDeviceCount(String[] filters);
}
