package org.wso2.carbon.device.mgt.analytics.dashboard.internal;

import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOException;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOFactory;

/**
 * To be updated...
 */
class GadgetDataServiceImpl implements GadgetDataService {
    @Override
    public int getTotalFilteredDeviceCount(String[] filters) {
        // default
        int totalFilteredDeviceCount = -1;
        try {
            totalFilteredDeviceCount = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getTotalFilteredDeviceCount(filters);
        } catch (GadgetDataServiceDAOException e) {
            return totalFilteredDeviceCount;
        }
        return totalFilteredDeviceCount;
    }

    @Override
    public int getNonCompliantDeviceCount(String[] filters) {
        return GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getNonCompliantDeviceCount(filters);
    }

    @Override
    public int getUnmonitoredDeviceCount(String[] filters) {
        return GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getUnmonitoredDeviceCount(filters);
    }
}
