/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.analytics.dashboard.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOException;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOFactory;

import java.sql.SQLException;
import java.util.Map;

/**
 * To be updated...
 */
class GadgetDataServiceImpl implements GadgetDataService {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(GadgetDataServiceImpl.class);

    @Override
    public int getTotalDeviceCount(Map<String, Object> filters) {
        int totalDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            totalDeviceCount = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getTotalDeviceCount(filters);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            totalDeviceCount = -1;
            return totalDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return totalDeviceCount;
    }

    @Override
    public int getActiveDeviceCount() {
        int activeDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            activeDeviceCount = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getActiveDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            activeDeviceCount = -1;
            return activeDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return activeDeviceCount;
    }

    @Override
    public int getInactiveDeviceCount() {
        int inactiveDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            inactiveDeviceCount = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getInactiveDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            inactiveDeviceCount = -1;
            return inactiveDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return inactiveDeviceCount;
    }

    @Override
    public int getRemovedDeviceCount() {
        int removedDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            removedDeviceCount = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getRemovedDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            removedDeviceCount = -1;
            return removedDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return removedDeviceCount;
    }

    @Override
    public int getNonCompliantDeviceCount() {
        int nonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            nonCompliantDeviceCount = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getNonCompliantDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            nonCompliantDeviceCount = -1;
            return nonCompliantDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return nonCompliantDeviceCount;
    }

    @Override
    public int getUnmonitoredDeviceCount() {
        int unmonitoredDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            unmonitoredDeviceCount = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getUnmonitoredDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            unmonitoredDeviceCount = -1;
            return unmonitoredDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return unmonitoredDeviceCount;
    }

}
