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
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.JSONUtil;

import java.sql.*;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractApplicationDAOImpl extends AbstractDAOImpl implements ApplicationDAO {

    private static final Log log = LogFactory.getLog(AbstractApplicationDAOImpl.class);

    public Application createApplication(Application application) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to create an application");
            log.debug("Application Details : ");
            log.debug("UUID : " + application.getUuid() + " Name : " + application.getName() + " User name : "
                    + application.getUser().getUserName());
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";
        boolean isBatchExecutionSupported = ConnectionManagerUtil.isBatchQuerySupported();

        try {
            conn = this.getDBConnection();
            sql += "INSERT INTO APPM_APPLICATION (UUID, IDENTIFIER, NAME, SHORT_DESCRIPTION, DESCRIPTION, ICON_NAME, "
                    + "BANNER_NAME, VIDEO_NAME, SCREENSHOTS, CREATED_BY, CREATED_AT, MODIFIED_AT, "
                    + "APPLICATION_CATEGORY_ID, PLATFORM_ID, TENANT_ID, LIFECYCLE_STATE_ID, "
                    + "LIFECYCLE_STATE_MODIFIED_AT, LIFECYCLE_STATE_MODIFIED_BY) VALUES "
                    + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, application.getUuid());
            stmt.setString(2, application.getIdentifier());
            stmt.setString(3, application.getName());
            stmt.setString(4, application.getShortDescription());
            stmt.setString(5, application.getDescription());
            stmt.setString(6, application.getIconName());
            stmt.setString(7, application.getBannerName());
            stmt.setString(8, application.getVideoName());
            stmt.setString(9, JSONUtil.listToJsonArrayString(application.getScreenshots()));
            stmt.setString(10, application.getUser().getUserName());
            stmt.setDate(11, new Date(application.getCreatedAt().getTime()));
            stmt.setDate(12, new Date(application.getModifiedAt().getTime()));
            stmt.setInt(13, application.getCategory().getId());
            stmt.setInt(14, application.getPlatform().getId());
            stmt.setInt(15, application.getUser().getTenantId());
            stmt.setInt(16, application.getCurrentLifecycle().getLifecycleState().getId());
            stmt.setDate(17, new Date(
                    application.getCurrentLifecycle().getLifecycleStateModifiedAt().getTime()));
            stmt.setString(18, application.getCurrentLifecycle().getGetLifecycleStateModifiedBy());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                application.setId(rs.getInt(1));
            }

            if (application.getTags() != null && application.getTags().size() > 0) {
                sql = "INSERT INTO APPM_APPLICATION_TAG (NAME, APPLICATION_ID) VALUES (?, ?); ";
                stmt = conn.prepareStatement(sql);
                for (String tag : application.getTags()) {
                    stmt.setString(1, tag);
                    stmt.setInt(2, application.getId());

                    if (isBatchExecutionSupported) {
                        stmt.addBatch();
                    } else {
                        stmt.execute();
                    }
                }
                if (isBatchExecutionSupported) {
                    stmt.executeBatch();
                }
            }

            if (application.getProperties() != null && application.getProperties().size() > 0) {
                sql = "INSERT INTO APPM_APPLICATION_PROPERTY (PROP_KEY, PROP_VAL, APPLICATION_ID) VALUES (?, ?, ?); ";
                stmt = conn.prepareStatement(sql);
                Iterator it = application.getProperties().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> property = (Map.Entry) it.next();
                    stmt.setString(1, property.getKey());
                    stmt.setString(2, property.getValue());
                    stmt.setInt(3, application.getId());
                    if (isBatchExecutionSupported) {
                        stmt.addBatch();
                    } else {
                        stmt.execute();
                    }
                }
                if (isBatchExecutionSupported) {
                    stmt.executeBatch();
                }
            }

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding the application", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }

        return application;
    }

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
