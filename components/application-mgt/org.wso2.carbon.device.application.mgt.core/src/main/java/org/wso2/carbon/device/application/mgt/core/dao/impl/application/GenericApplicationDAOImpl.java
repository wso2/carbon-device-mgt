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
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Category;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleStateTransition;
import org.wso2.carbon.device.application.mgt.common.Pagination;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This handles ApplicationDAO related operations.
 */
public class GenericApplicationDAOImpl extends AbstractDAOImpl implements ApplicationDAO {

    private static final Log log = LogFactory.getLog(GenericApplicationDAOImpl.class);

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
        String generatedColumns[] = {"ID"};
        boolean isBatchExecutionSupported = ConnectionManagerUtil.isBatchQuerySupported();
        int index = 0;
        try {
            conn = this.getDBConnection();
            sql += "INSERT INTO APPM_APPLICATION (UUID, NAME, SHORT_DESCRIPTION, DESCRIPTION, "
                    + "VIDEO_NAME, SCREEN_SHOT_COUNT, CREATED_BY, CREATED_AT, MODIFIED_AT, "
                    + "APPLICATION_CATEGORY_ID, PLATFORM_ID, TENANT_ID, LIFECYCLE_STATE_ID, "
                    + "LIFECYCLE_STATE_MODIFIED_AT, LIFECYCLE_STATE_MODIFIED_BY) VALUES "
                    + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql, generatedColumns);
            stmt.setString(++index, application.getUuid());
            stmt.setString(++index, application.getName());
            stmt.setString(++index, application.getShortDescription());
            stmt.setString(++index, application.getDescription());
            stmt.setString(++index, application.getVideoName());
            stmt.setInt(++index, application.getScreenShotCount());
            stmt.setString(++index, application.getUser().getUserName());
            stmt.setDate(++index, new Date(application.getCreatedAt().getTime()));
            stmt.setDate(++index, new Date(application.getModifiedAt().getTime()));
            stmt.setInt(++index, application.getCategory().getId());
            stmt.setInt(++index, application.getPlatform().getId());
            stmt.setInt(++index, application.getUser().getTenantId());
            stmt.setInt(++index, application.getCurrentLifecycle().getLifecycleState().getId());
            stmt.setDate(++index, new Date(application.getCurrentLifecycle().getLifecycleStateModifiedAt().getTime()));
            stmt.setString(++index, application.getCurrentLifecycle().getGetLifecycleStateModifiedBy());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                application.setId(rs.getInt(1));
            }
            insertApplicationTagsAndProperties(application, stmt, conn, isBatchExecutionSupported);
            return application;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding the application", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }


    @Override
    public ApplicationList getApplications(Filter filter, int tenantId) throws ApplicationManagementDAOException {
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
            conn = this.getDBConnection();
            stmt = this.generateGetApplicationsStatement(filter, conn, tenantId);
            rs = stmt.executeQuery();

            int length = 0;

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
            pagination.setCount(this.getApplicationCount(filter));
            applicationList.setApplications(applications);
            applicationList.setPagination(pagination);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application list for the tenant"
                    + " " + tenantId + ". While executing " + sql, e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON, while getting application"
                    + " list for the tenant " + tenantId, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection while "
                    + "getting application list for the tenant " + tenantId, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return applicationList;
    }

    /**
     * This method is used to generate the statement that is used to get the applications with the given filter.
     *
     * @param filter   Filter to filter out the applications.
     * @param conn     Database Connection.
     * @param tenantId ID of the tenant to retrieve the applications.
     * @return the statement for getting applications that are belong to a particular filter.
     * @throws SQLException SQL Exception
     */
    protected PreparedStatement generateGetApplicationsStatement(Filter filter, Connection conn,
                                                                 int tenantId) throws SQLException {
        int index = 0;
        String sql = "SELECT APP.*, APL.NAME AS APL_NAME, APL.IDENTIFIER AS APL_IDENTIFIER, CAT.ID AS CAT_ID, "
                + "CAT.NAME AS CAT_NAME,  LS.NAME AS LS_NAME, LS.IDENTIFIER AS LS_IDENTIFIER, "
                + "LS.DESCRIPTION AS LS_DESCRIPTION FROM APPM_APPLICATION APP INNER JOIN APPM_PLATFORM APL "
                + "ON APP.PLATFORM_ID = APL.ID INNER JOIN APPM_APPLICATION_CATEGORY CAT "
                + "ON APP.APPLICATION_CATEGORY_ID = CAT.ID INNER JOIN APPM_LIFECYCLE_STATE LS "
                + "ON APP.LIFECYCLE_STATE_ID = LS.ID WHERE APP.TENANT_ID = ? ";

        String userName = filter.getUserName();
        if (!userName.equals("ALL")) {
            sql += " AND APP.CREATED_BY = ? ";
        }
        if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
            sql += "AND LOWER (APP.NAME) ";
            if (filter.isFullMatch()) {
                sql += "= ?";
            } else {
                sql += "LIKE ?";
            }
        }

        sql += "LIMIT ? OFFSET ?";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(++index, tenantId);

        if (!userName.equals("ALL")) {
            stmt.setString(++index, userName);
        }
        if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
            if (filter.isFullMatch()) {
                stmt.setString(++index, filter.getSearchQuery().toLowerCase());
            } else {
                stmt.setString(++index, "%" + filter.getSearchQuery().toLowerCase() + "%");
            }
        }

        stmt.setInt(++index, filter.getLimit());
        stmt.setInt(++index, filter.getOffset());

        return stmt;
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
            sql += "SELECT COUNT(APP.ID) AS APP_COUNT ";
            sql += "FROM APPM_APPLICATION AS APP ";
            sql += "INNER JOIN APPM_PLATFORM AS APL ON APP.PLATFORM_ID = APL.ID ";
            sql += "INNER JOIN APPM_APPLICATION_CATEGORY AS CAT ON APP.APPLICATION_CATEGORY_ID = CAT.ID ";

            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                sql += "WHERE LOWER (APP.NAME) LIKE ? ";
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
    public Application getApplication(String uuid, int tenantId, String userName) throws
            ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the UUID(" + uuid + ") from the database");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";
        Application application = null;
        try {

            conn = this.getDBConnection();
            sql += "SELECT APP.*, APL.NAME AS APL_NAME, APL.IDENTIFIER AS APL_IDENTIFIER, CAT.ID AS CAT_ID, "
                    + "CAT.NAME AS CAT_NAME,  LS.NAME AS LS_NAME, LS.IDENTIFIER AS LS_IDENTIFIER, "
                    + "LS.DESCRIPTION AS LS_DESCRIPTION "
                    + "FROM APPM_APPLICATION APP "
                    + "INNER JOIN APPM_PLATFORM APL "
                    + "ON APP.PLATFORM_ID = APL.ID "
                    + "INNER JOIN APPM_APPLICATION_CATEGORY CAT "
                    + "ON APP.APPLICATION_CATEGORY_ID = CAT.ID "
                    + "INNER JOIN APPM_LIFECYCLE_STATE LS "
                    + " ON APP.LIFECYCLE_STATE_ID = LS.ID "
                    + "WHERE UUID = ? AND APP.TENANT_ID = ? ";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid);
            stmt.setInt(2, tenantId);

            if (!userName.equals("ALL")) {
                sql += "AND APP.CREATED_BY = ?";
                stmt.setString(3, userName);
            }
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application with the UUID " + uuid);
            }

            if (rs.next()) {
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
            return application;
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application details with UUID " + uuid + " While executing query "
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
        Connection connection = null;
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
    public Category addCategory(Category category) throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "INSERT INTO APPM_APPLICATION_CATEGORY (NAME, DESCRIPTION) VALUES (?, ?)";
        String[] generatedColumns = { "ID" };
        ResultSet rs = null;

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql, generatedColumns);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            if (rs.next()) {
                category.setId(rs.getInt(1));
            }
            return category;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection while trying to update the categroy " + category.getName(), e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("SQL exception while executing the query '" + sql + "' .", e);
        } finally {
            Util.cleanupResources(statement, rs);
        }
    }

    @Override
    public List<Category> getCategories() throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM APPM_APPLICATION_CATEGORY";
        List<Category> categories = new ArrayList<>();

        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("ID"));
                category.setName(rs.getString("NAME"));
                category.setDescription(rs.getString("DESCRIPTION"));
                categories.add(category);
            }
            return categories;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database Connection Exception while trying to get the "
                    + "application categories", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("SQL Exception while trying to get the application "
                    + "categories, while executing " + sql, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }

    }

    @Override
    public Category getCategory(String name) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM APPM_APPLICATION_CATEGORY WHERE NAME = ?";

        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("ID"));
                category.setName(rs.getString("NAME"));
                category.setDescription(rs.getString("DESCRIPTION"));
                return category;
            }
            return null;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database Connection Exception while trying to get the "
                    + "application categories", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("SQL Exception while trying to get the application "
                    + "categories, while executing " + sql, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean isApplicationExistForCategory(String name) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM APPM_APPLICATION WHERE APPLICATION_CATEGORY_ID = (SELECT ID FROM "
                + "APPM_APPLICATION_CATEGORY WHERE NAME = ?)";

        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database Connection Exception while trying to check the " + "applications for teh category "
                            + name, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL Exception while trying to get the application related with categories, while executing "
                            + sql, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteCategory(String name) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        String sql = "DELETE FROM APPM_APPLICATION_CATEGORY WHERE NAME = ?";

        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database Connection Exception while trying to delete the category " + name, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL Exception while trying to delete the category " + name + " while executing the query " +
                            sql, e);
        } finally {
            Util.cleanupResources(stmt, null);
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
            String sql = "DELETE FROM APPM_APPLICATION_TAG WHERE APPLICATION_ID = ?";
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
    public int getApplicationId(String uuid, int tenantId) throws ApplicationManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql;
        int id = -1;
        try {
            conn = this.getDBConnection();
            sql = "SELECT ID FROM APPM_APPLICATION WHERE UUID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid);
            stmt.setInt(2, tenantId);
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
