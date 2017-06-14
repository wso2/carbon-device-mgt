/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.application.mgt.core.dao.impl.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.Pagination;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.JSONUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class holds the generic implementation of ApplicationDAO which can be used to support ANSI db syntax.
 */
public class MySQLApplicationDAOImpl extends AbstractApplicationDAOImpl {

    private static final Log log = LogFactory.getLog(MySQLApplicationDAOImpl.class);

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Getting application data from the database");
            log.debug(String.format("Filter: limit=%s, offset=%", filter.getLimit(), filter.getOffset()));
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";
        ApplicationList applicationList = new ApplicationList();
        List<Application> applications = new ArrayList<>();
        Pagination pagination = new Pagination();

        if (filter == null) {
            throw new ApplicationManagementDAOException("Filter need to be instantiated");
        } else {
            pagination.setLimit(filter.getLimit());
            pagination.setOffset(filter.getOffset());
        }

        try {

            conn = this.getConnection();

            sql += "SELECT SQL_CALC_FOUND_ROWS APP.*, APL.NAME AS APL_NAME, APL.IDENTIFIER AS APL_IDENTIFIER," +
                    " CAT.NAME AS CAT_NAME ";
            sql += "FROM APPM_APPLICATION AS APP ";
            sql += "INNER JOIN APPM_PLATFORM AS APL ON APP.PLATFORM_ID = APL.ID ";
            sql += "INNER JOIN APPM_APPLICATION_CATEGORY AS CAT ON APP.APPLICATION_CATEGORY_ID = CAT.ID ";

            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                sql += "WHERE APP.NAME LIKE ? ";
            }
            sql += "LIMIT ?,?;";

            stmt = conn.prepareStatement(sql);
            int index = 0;
            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                stmt.setString(++index, "%" + filter.getSearchQuery() + "%");
            }
            stmt.setInt(++index, filter.getOffset());
            stmt.setInt(++index, filter.getLimit());

            rs = stmt.executeQuery();

            int length = 0;
            sql = "SELECT FOUND_ROWS() AS COUNT;"; //TODO: from which tables????
            stmt = conn.prepareStatement(sql);
            ResultSet rsCount = stmt.executeQuery();
            if (rsCount.next()) {
                pagination.setCount(rsCount.getInt("COUNT"));
            }

            while (rs.next()) {

                //Getting properties
                sql = "SELECT * FROM APPM_APPLICATION_PROPERTY WHERE APPLICATION_ID=?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, rs.getInt("ID"));
                ResultSet rsProperties = stmt.executeQuery();

                //Getting tags
                sql = "SELECT * FROM APPM_APPLICATION_TAG WHERE APPLICATION_ID=?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, rs.getInt("ID"));
                ResultSet rsTags = stmt.executeQuery();

                applications.add(Util.loadApplication(rs, rsProperties, rsTags));
                Util.cleanupResources(null, rsProperties);
                Util.cleanupResources(null, rsTags);
                length++;
            }

            pagination.setSize(length);

            applicationList.setApplications(applications);
            applicationList.setPagination(pagination);

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return applicationList;
    }

    @Override
    public Application editApplication(Application application) throws ApplicationManagementDAOException {
        return null;
    }


    @Override
    public Application createApplication(Application application) throws ApplicationManagementDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";


        try {
            conn = this.getConnection();
            sql += "INSERT INTO APPM_APPLICATION (UUID, NAME, SHORT_DESCRIPTION, DESCRIPTION, ICON_NAME, BANNER_NAME, " +
                    "VIDEO_NAME, SCREENSHOTS, CREATED_BY, CREATED_AT, MODIFIED_AT, APPLICATION_CATEGORY_ID, " + "" +
                    "PLATFORM_ID, TENANT_ID, LIFECYCLE_STATE_ID, LIFECYCLE_STATE_MODIFIED_AT, " +
                    "LIFECYCLE_STATE_MODIFIED_BY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, application.getUuid());
            stmt.setString(2, application.getName());
            stmt.setString(3, application.getShortDescription());
            stmt.setString(4, application.getDescription());
            stmt.setString(5, application.getIconName());
            stmt.setString(6, application.getBannerName());
            stmt.setString(7, application.getVideoName());
            stmt.setString(8, JSONUtil.listToJsonArrayString(application.getScreenshots()));
            stmt.setString(9, application.getUser().getUserName());
            stmt.setDate(10, new Date(application.getCreatedAt().getTime()));
            stmt.setDate(11, new Date(application.getModifiedAt().getTime()));
            stmt.setInt(12, application.getCategory().getId());
            stmt.setInt(13, application.getPlatform().getId());
            stmt.setInt(14, application.getUser().getTenantId());
            stmt.setInt(15, application.getCurrentLifecycle().getLifecycleState().getId());
            stmt.setDate(16, new Date(application.getCurrentLifecycle().getLifecycleStateModifiedAt().getTime()));
            stmt.setString(17, application.getCurrentLifecycle().getGetLifecycleStateModifiedBy());
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
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            if (application.getProperties() != null && application.getProperties().size() > 0) {
                sql = "INSERT INTO APPM_APPLICATION_PROPERTY (PROP_KEY, PROP_VAL, APPLICATION_ID) VALUES (?, ?, ?); ";
                stmt = conn.prepareStatement(sql);
                Iterator it = application.getProperties().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> property = (Map.Entry)it.next();
                    stmt.setString(1, property.getKey());
                    stmt.setString(2, property.getValue());
                    stmt.setInt(3, application.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding the application", e);
        }

        return application;
    }

}
