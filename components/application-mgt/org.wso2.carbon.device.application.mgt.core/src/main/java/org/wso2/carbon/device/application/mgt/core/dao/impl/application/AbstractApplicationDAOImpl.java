/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.core.dao.impl.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractApplicationDAOImpl extends AbstractDAOImpl implements ApplicationDAO {

    private static final Log log = LogFactory.getLog(AbstractApplicationDAOImpl.class);

    @Override
    public int getApplicationCount(Filter filter) throws ApplicationManagementDAOException {
        if(log.isDebugEnabled()){
            log.debug("Getting application count from the database");
            log.debug(String.format("Filter: limit=%s, offset=%", filter.getLimit(), filter.getOffset()));
        }

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";
        int count = 0;

        if (filter == null) {
            throw new ApplicationManagementDAOException("Filter need to be instantiated");
        }

        try {
            conn = this.getConnection();
            sql += "SELECT COUNT(APP.ID) AS APP_COUNT ";
            sql += "FROM APPM_APPLICATION AS APP ";
            sql += "INNER JOIN APPM_PLATFORM AS APL ON APP.PLATFORM_ID = APL.ID ";
            sql += "INNER JOIN APPM_APPLICATION_CATEGORY AS CAT ON APP.APPLICATION_CATEGORY_ID = CAT.ID ";

            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                sql += "WHERE APP.NAME LIKE ? ";
            }
            sql += ";";

            stmt = conn.prepareStatement(sql);
            int index = 0;
            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                stmt.setString(++index, "%" + filter.getSearchQuery() + "%");
            }
            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("APP_COUNT");
            }
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return count;
    }
}
