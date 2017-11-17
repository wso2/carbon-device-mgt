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
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This handles ApplicationDAO related operations.
 */
public class GenericApplicationDAOImpl extends AbstractDAOImpl implements ApplicationDAO {

    private static final Log log = LogFactory.getLog(GenericApplicationDAOImpl.class);

    @Override
    public int createApplication(Application application, int deviceId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to create an application");
            log.debug("Application Details : ");
            log.debug("App Name : " + application.getName() + " App Type : "
                    + application.getType() + " User Name : " + application.getUser().getUserName());
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int index = 0;
        int applicationId = -1;
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement("INSERT INTO AP_APP (NAME, TYPE, APP_CATEGORY, "
                    + "IS_FREE, PAYMENT_CURRENCY, RESTRICTED, TENANT_ID) VALUES "
                    + "(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(++index, application.getName());
            stmt.setString(++index, application.getType());
            stmt.setString(++index, application.getAppCategory());
            stmt.setInt(++index, application.getIsFree());
            stmt.setString(++index, application.getPaymentCurrency());
            stmt.setInt(++index, application.getIsRestricted());
            stmt.setInt(++index, application.getUser().getTenantId());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                applicationId = rs.getInt(1);
            }
            return applicationId;

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding the application", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void addTags(List<Tag> tags, int applicationId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add tags");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int index = 0;
        String sql = "INSERT INTO AP_APP_TAG (TAG, TENANT_ID, AP_APP_ID) "
                + "VALUES (?, ?, ?)";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            for (Tag tag : tags) {
                stmt.setString(++index, tag.getTagName());
                stmt.setInt(++index, tenantId);
                stmt.setInt(++index, applicationId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        }catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        }catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding tags", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void addUnrestrictedRoles(List<UnrestrictedRole> unrestrictedRoles, int applicationId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add unrestricted roles");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int index = 0;
        String sql = "INSERT INTO AP_UNRESTRICTED_ROLES (ROLE, TENANT_ID, AP_APP_ID) "
                + "VALUES (?, ?, ?)";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            for (UnrestrictedRole role : unrestrictedRoles) {
                stmt.setString(++index, role.getRole());
                stmt.setInt(++index, tenantId);
                stmt.setInt(++index, applicationId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        }catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        }catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding unrestricted roles", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }

    }

    @Override
    public int isExistApplication(String appName, String type, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to verify whether the registering app is registered or not");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int isExist = 0;
        int index = 0;
        String sql = "SELECT * FROM AP_APP WHERE NAME = ? AND TYPE = ? TENANT_ID = ?";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setString(++index , appName);
            stmt.setString(++index , type);
            stmt.setInt(++index, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                isExist = 1;
           }

           return isExist;

        }catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        }catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding unrestricted roles", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }

    }

    @Override
    public ApplicationList getApplications(Filter filter, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application data from the database");
            log.debug(String.format("Filter: limit=%s, offset=%s", filter.getLimit(), filter.getOffset()));
        }

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ApplicationList applicationList = new ApplicationList();
        Pagination pagination = new Pagination();
        int index = 0;
        String sql = "SELECT AP_APP.ID AS APP_ID, AP_APP.NAME AS APP_NAME, AP_APP.TYPE AS APP_TYPE, AP_APP.APP_CATEGORY"
                + " AS APP_CATEGORY, AP_APP.IS_FREE, AP_APP.RESTRICTED, AP_APP_TAG.TAG AS APP_TAG, AP_UNRESTRICTED_ROLES.ROLE "
                + "AS APP_UNRESTRICTED_ROLES FROM ((AP_APP LEFT JOIN AP_APP_TAG ON AP_APP.ID = AP_APP_TAG.AP_APP_ID) "
                + "LEFT JOIN AP_UNRESTRICTED_ROLES ON AP_APP.ID = AP_UNRESTRICTED_ROLES.AP_APP_ID) "
                + "WHERE AP_APP.TENANT_ID =  ?";


        if (filter == null) {
            throw new ApplicationManagementDAOException("Filter need to be instantiated");
        }

        if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
            sql += " AND LOWER (AP_APP.NAME) ";
            if (filter.isFullMatch()) {
                sql += "= ?";
            } else {
                sql += "LIKE ?";
            }
        }

        sql += " LIMIT ? OFFSET ?";

        pagination.setLimit(filter.getLimit());
        pagination.setOffset(filter.getOffset());

        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(++index, tenantId);

            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                if (filter.isFullMatch()) {
                    stmt.setString(++index, filter.getSearchQuery().toLowerCase());
                } else {
                    stmt.setString(++index, "%" + filter.getSearchQuery().toLowerCase() + "%");
                }
            }

            stmt.setInt(++index, filter.getLimit());
            stmt.setInt(++index, filter.getOffset());
            rs = stmt.executeQuery();
            applicationList.setApplications(Util.loadApplications(rs));
            pagination.setSize(filter.getOffset());
            pagination.setCount(this.getApplicationCount(filter));
            applicationList.setPagination(pagination);

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application list for the tenant"
                    + " " + tenantId + ". While executing " + sql, e);
        }
        catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection while "
                    + "getting application list for the tenant " + tenantId, e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON ", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return applicationList;
    }


    @Override
    public int getApplicationCount(Filter filter) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
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
            conn = this.getDBConnection();
            sql += "SELECT count(APP.ID) AS APP_COUNT FROM AP_APP AS APP WHERE TENANT_ID = ?";

            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                sql += " AND LOWER (APP.NAME) LIKE ? ";
            }
            sql += ";";

            stmt = conn.prepareStatement(sql);
            int index = 0;
            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                stmt.setString(++index, "%" + filter.getSearchQuery().toLowerCase() + "%");
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
    public Application getApplication(String appName, String appType, int tenantId) throws
            ApplicationManagementDAOException {
        if (log.isDebugEnabled()){
            log.debug("Getting application with the type(" + appType + " and Name " + appName +
                    " ) from the database");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT AP_APP.ID AS APP_ID, AP_APP.NAME AS APP_NAME, AP_APP.TYPE AS APP_TYPE, AP_APP.APP_CATEGORY "
                    + "AS APP_CATEGORY, AP_APP.IS_FREE, AP_APP_TAG.TAG, AP_UNRESTRICTED_ROLES.ROLE AS RELESE_ID FROM "
                    + "AP_APP, AP_APP_TAG, AP_UNRESTRICTED_ROLES WHERE AP_APP.NAME=? AND AP_APP.TYPE= ? "
                    + "AND AP_APP.TENANT_ID=?;";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, appName);
            stmt.setString(2, appType);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application with the type "
                        + appType +"and app name "+ appName);
            }

            return Util.loadApplication(rs);

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application details with app name " + appName + " While executing query "
                            + sql, e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void addLifecycle(Lifecycle lifecycle, int tenantId, int appReleaseId, int appId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Life cycle is created by" + lifecycle.getCreatedBy() + " at "
                    + lifecycle.getCreatedAt());
        }
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getDBConnection();
            String sql = "INSERT INTO AP_APP_LIFECYCLE (CREATED_BY, CREATED_TIMESTAMP, TENANT_ID, AP_APP_RELEASE_ID, "
                    + "AP_APP_ID) VALUES (?, ?, ?, ? ,?);";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, lifecycle.getCreatedBy());
            stmt.setTimestamp(2, (Timestamp) lifecycle.getCreatedAt());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, appReleaseId);
            stmt.setInt(5, appId);
            stmt.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding the lifecycle of application", e);
        } finally {
            Util.cleanupResources(stmt, null);
        }

    }

    @Override
    public void changeLifecycle(String applicationUUID, String lifecycleIdentifier, String userName, int tenantId)
            throws ApplicationManagementDAOException {
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
                    + "LIFECYCLE_STATE_MODIFIED_BY = ?, LIFECYCLE_STATE_MODIFIED_AT = ? WHERE UUID = ? AND TENANT_ID "
                    + "= ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, lifecycleIdentifier);
            stmt.setString(2, userName);
            stmt.setDate(3, new Date(System.currentTimeMillis()));
            stmt.setString(4, applicationUUID);
            stmt.setInt(5, tenantId);
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
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String sql = "SELECT STATE.NAME, TRANSITION.DESCRIPTION, TRANSITION.PERMISSION FROM ( SELECT * FROM "
                + "APPM_LIFECYCLE_STATE ) STATE RIGHT JOIN (SELECT * FROM APPM_LC_STATE_TRANSITION WHERE "
                + "INITIAL_STATE = (SELECT LIFECYCLE_STATE_ID FROM APPM_APPLICATION WHERE UUID = ? AND TENANT_ID = ?)) "
                + "TRANSITION  ON TRANSITION.NEXT_STATE = STATE.ID";

        try {
            connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, applicationUUID);
            preparedStatement.setInt(2, tenantId);
            resultSet = preparedStatement.executeQuery();

            List<LifecycleStateTransition> lifecycleStateTransitions = new ArrayList<>();

            while (resultSet.next()) {
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

    @Override
    public void updateScreenShotCount(String applicationUUID, int tenantId, int count)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "UPDATE APPM_APPLICATION SET SCREEN_SHOT_COUNT = ? where UUID = ? and TENANT_ID = ?";

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, count);
            statement.setString(2, applicationUUID);
            statement.setInt(3, tenantId);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection while trying to update the screen-shot "
                    + "count for the application with UUID " + applicationUUID + " for the tenant " + tenantId);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("SQL exception while executing the query '" + sql + "' .", e);
        } finally {
            Util.cleanupResources(statement, null);
        }

    }

    @Override
    public boolean isApplicationExist(String categoryName) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM APPM_APPLICATION WHERE APPLICATION_CATEGORY_ID = (SELECT ID FROM "
                + "APPM_APPLICATION_CATEGORY WHERE NAME = ?)";
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, categoryName);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database Connection Exception while trying to check the " + "applications for teh category "
                            + categoryName, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL Exception while trying to get the application related with categories, while executing " + sql,
                    e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Application editApplication(Application application, int tenantId) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        String sql = "";
        boolean isBatchExecutionSupported = ConnectionManagerUtil.isBatchQuerySupported();
        try {
            conn = this.getDBConnection();
            int index = 0;
            sql += "UPDATE APPM_APPLICATION SET NAME = COALESCE (?, NAME), SHORT_DESCRIPTION = COALESCE "
                    + "(?, SHORT_DESCRIPTION), DESCRIPTION = COALESCE (?, DESCRIPTION), SCREEN_SHOT_COUNT = "
                    + "COALESCE (?, SCREEN_SHOT_COUNT), VIDEO_NAME = COALESCE (?, VIDEO_NAME), MODIFIED_AT = COALESCE "
                    + "(?, MODIFIED_AT), ";

            if (application.getPayment() != null) {
                sql += " IS_FREE = COALESCE (?, IS_FREE), ";
                if (application.getPayment().getPaymentCurrency() != null) {
                    sql += "PAYMENT_CURRENCY = COALESCE (?, PAYMENT_CURRENCY), ";
                }
                sql += "PAYMENT_PRICE = COALESCE (?, PAYMENT_PRICE), ";
            }
            if (application.getCategory() != null && application.getCategory().getId() != 0) {
                sql += "APPLICATION_CATEGORY_ID = COALESCE (?, APPLICATION_CATEGORY_ID), ";
            }
            if (application.getPlatform() != null && application.getPlatform().getId() != 0) {
                sql += "PLATFORM_ID = COALESCE (?, PLATFORM_ID), ";
            }

            sql += "TENANT_ID = COALESCE (?, TENANT_ID) WHERE UUID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(++index, application.getName());
            stmt.setString(++index, application.getShortDescription());
            stmt.setString(++index, application.getDescription());
            stmt.setInt(++index, application.getScreenShotCount());
            stmt.setString(++index, application.getVideoName());
            stmt.setDate(++index, new Date(application.getModifiedAt().getTime()));
            if (application.getPayment() != null) {
                stmt.setBoolean(++index, application.getPayment().isFreeApp());
                if (application.getPayment().getPaymentCurrency() != null) {
                    stmt.setString(++index, application.getPayment().getPaymentCurrency());
                }
                stmt.setFloat(++index, application.getPayment().getPaymentPrice());
            }

            if (application.getCategory() != null && application.getCategory().getId() != 0) {
                stmt.setInt(++index, application.getCategory().getId());
            }
            if (application.getPlatform() != null && application.getPlatform().getId() != 0) {
                stmt.setInt(++index, application.getPlatform().getId());
            }
            stmt.setInt(++index, tenantId);
            stmt.setString(++index, application.getUuid());
            stmt.executeUpdate();

            application.setId(getApplicationId(application.getUuid(), tenantId));

            sql = "DELETE FROM APPM_APPLICATION_TAG WHERE APPLICATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, application.getId());
            stmt.executeUpdate();

            // delete existing properties and add new ones. if no properties are set, existing ones will be deleted.
            sql = "DELETE FROM APPM_APPLICATION_PROPERTY WHERE APPLICATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, application.getId());
            stmt.executeUpdate();

            insertApplicationTagsAndProperties(application, stmt, conn, isBatchExecutionSupported);
            return application;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding the application", e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    /**
     * To insert application properties and Tags
     *
     * @param application Application in which the properties and tags need to be inserted
     */
    private void insertApplicationTagsAndProperties(Application application, PreparedStatement stmt, Connection
            conn, boolean isBatchExecutionSupported) throws SQLException {
        String sql;
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
    }

    @Override
    public void deleteApplication(String uuid, int tenantId) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getDBConnection();
            String sql = "DELETE FROM APPM_APPLICATION WHERE UUID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while deleting the application: " + uuid, e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteProperties(int applicationId) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getDBConnection();
            String sql = "DELETE FROM APPM_APPLICATION_PROPERTY WHERE APPLICATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, applicationId);
            stmt.executeUpdate();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while deleting properties of application: " + applicationId, e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteTags(int applicationId) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getDBConnection();
            String sql = "DELETE FROM AP_APP_TAG WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, applicationId);
            stmt.executeUpdate();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while deleting tags of application: " + applicationId, e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public int getApplicationId(String appName, String appType, int tenantId) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql;
        int id = -1;
        try {
            conn = this.getDBConnection();
            sql = "SELECT ID FROM AP_APP WHERE NAME = ? AND TYPE = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, appName);
            stmt.setString(2, appType);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return id;
    }

}
