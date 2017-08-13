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
import org.json.JSONException;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleStateTransition;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.JSONUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    @Override
    public Application getApplication(String uuid) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the UUID(" + uuid + ") from the database");
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";
        Application application = null;
        try {

            conn = this.getDBConnection();
            sql += "SELECT APP.*, APL.NAME AS APL_NAME, APL.IDENTIFIER AS APL_IDENTIFIER, "
                    + "CAT.ID AS CAT_ID, CAT.NAME AS CAT_NAME FROM APPM_APPLICATION AS APP INNER JOIN APPM_PLATFORM AS "
                    + "APL ON APP.PLATFORM_ID = APL.ID INNER JOIN APPM_APPLICATION_CATEGORY AS CAT ON "
                    + "APP.APPLICATION_CATEGORY_ID = CAT.ID WHERE UUID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application with the UUID " + uuid);
            }

            if (rs.next()) {
                application = new Application();
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

                application = Util.loadApplication(rs, rsProperties, rsTags);
                Util.cleanupResources(null, rsProperties);
                Util.cleanupResources(null, rsTags);
            }
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return application;
    }

    @Override
    public void changeLifecycle(String applicationUUID, String lifecycleIdentifier, String userName) throws
            ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Change Life cycle status change " + lifecycleIdentifier + "request received to the DAO "
                    + "level for the application with " + "the UUID '" + applicationUUID + "' from the user "
                    + userName);
        }
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getDBConnection();
            String sql = "UPDATE APPM_APPLICATION SET "
                    + "LIFECYCLE_STATE_ID = (SELECT ID FROM APPM_LIFECYCLE_STATE WHERE IDENTIFIER = ?), "
                    + "LIFECYCLE_STATE_MODIFIED_BY = ?, LIFECYCLE_STATE_MODIFIED_AT = ? WHERE UUID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, lifecycleIdentifier);
            stmt.setString(2, userName);
            stmt.setDate(3, new Date(System.currentTimeMillis()));
            stmt.setString(4, applicationUUID);
            stmt.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while changing lifecycle of application: " + applicationUUID + " to: "
                            + lifecycleIdentifier + " state.", e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<LifecycleStateTransition> getNextLifeCycleStates(String applicationUUID, int tenantId)
            throws ApplicationManagementDAOException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String sql = "SELECT STATE.NAME, TRANSITION.DESCRIPTION, TRANSITION.PERMISSION FROM ( SELECT * FROM "
                + "APPM_LIFECYCLE_STATE ) STATE RIGHT JOIN (SELECT * FROM APPM_LIFECYCLE_STATE_TRANSITION WHERE "
                + "INITIAL_STATE = (SELECT LIFECYCLE_STATE_ID FROM APPM_APPLICATION WHERE UUID = ?)) "
                + "TRANSITION  ON TRANSITION.NEXT_STATE = STATE.ID";

        try {
            connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, applicationUUID);
            resultSet = preparedStatement.executeQuery();

            List<LifecycleStateTransition> lifecycleStateTransitions = new ArrayList<>();

            while(resultSet.next()) {
                LifecycleStateTransition lifecycleStateTransition = new LifecycleStateTransition();
                lifecycleStateTransition.setDescription(resultSet.getString(2));
                lifecycleStateTransition.setNextState(resultSet.getString(1));
                lifecycleStateTransition.setPermission(resultSet.getString(3));
                lifecycleStateTransitions.add(lifecycleStateTransition);
            }
            return lifecycleStateTransitions;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error while getting the DBConnection for getting the life "
                    + "cycle states for the application with the UUID : " + applicationUUID, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("SQL exception while executing the query '" + sql + "'.", e);
        } finally {
            Util.cleanupResources(preparedStatement, resultSet);
        }
    }
}
