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

package org.wso2.carbon.device.application.mgt.core.dao.impl.application.release;

import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GenericApplicationReleaseDAOImpl holds the implementation of ApplicationRelease related DAO operations.
 */
public class GenericApplicationReleaseDAOImpl extends AbstractDAOImpl implements ApplicationReleaseDAO  {

    @Override
    public ApplicationRelease createRelease(ApplicationRelease applicationRelease) throws
            ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "insert into APPM_APPLICATION_RELEASE(VERSION_NAME, RESOURCE, RELEASE_CHANNEL ,"
                + "RELEASE_DETAILS, CREATED_AT, APPM_APPLICATION_ID, IS_DEFAULT) values (?, ?, ?, ?, ?, ?, ?)";
        int index = 0;

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(++index, applicationRelease.getVersionName());
            statement.setString(++index, applicationRelease.getResource());
            statement.setString(++index, String.valueOf(applicationRelease.getReleaseChannel()));
            statement.setString(++index, applicationRelease.getReleaseDetails());
            statement.setDate(++index, new Date(applicationRelease.getCreatedAt().getTime()));
            statement.setInt(++index, applicationRelease.getApplication().getId());
            statement.setBoolean(++index, applicationRelease.isDefault());
            statement.executeUpdate();
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                applicationRelease.setId(resultSet.getInt(1));
            }
            insertApplicationReleaseProperties(connection, applicationRelease);
            return applicationRelease;
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL Exception while trying to release an application (UUID : " + applicationRelease
                            .getApplication().getUuid() + "), by executing the query " + sql, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database Connection Exception while trying to release the " + "applcation with UUID "
                            + applicationRelease.getApplication().getUuid(), e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    @Override
    public ApplicationRelease getRelease(String applicationUuid, String versionName)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "SELECT * FROM APPM_APPLICATION_RELEASE WHERE VERSION_NAME = ?  AND APPM_APPLICATION_ID = "
                + "(SELECT ID FROM APPM_APPLICATION WHERE UUID = ?)";
        ApplicationRelease applicationRelease = null;
        ResultSet rsProperties = null;

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, versionName);
            statement.setString(2, applicationUuid);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                applicationRelease = new ApplicationRelease();
                applicationRelease.setVersionName(versionName);
                applicationRelease.setDefault(resultSet.getBoolean("IS_DEFAULT"));
                applicationRelease.setCreatedAt(resultSet.getDate("CREATED_AT"));
                applicationRelease.setReleaseChannel(resultSet.getString("RELEASE_CHANNEL"));
                applicationRelease.setReleaseDetails(resultSet.getString("RELEASE_DETAILS"));
                applicationRelease.setResource(resultSet.getString("RESOURCE"));

                sql = "SELECT * FROM APPM_RELEASE_PROPERTY WHERE APPLICATION_RELEASE_ID=?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, resultSet.getInt("ID"));
                rsProperties = statement.executeQuery();

                Map<String, String> properties = new HashMap<>();
                while (rsProperties.next()) {
                    properties.put(rsProperties.getString("PROP_KEY"),
                            rsProperties.getString("PROP_VAL"));
                }
                applicationRelease.setProperties(properties);
            }
            return applicationRelease;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection exception while trying to get the "
                    + "release details of the application with UUID " + applicationUuid + " and version " +
                    versionName, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error while getting release details of the application " +
                    applicationUuid +  " and version " + versionName + " , while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
            Util.cleanupResources(null, rsProperties);
        }
    }

    @Override
    public List<ApplicationRelease> getApplicationReleases(String applicationUUID)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "SELECT * FROM APPM_APPLICATION_RELEASE WHERE APPM_APPLICATION_ID = (SELECT ID FROM "
                + "APPM_APPLICATION WHERE UUID = ?)";
        List<ApplicationRelease> applicationReleases = new ArrayList<>();
        ResultSet rsProperties = null;

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, applicationUUID);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ApplicationRelease applicationRelease = new ApplicationRelease();
                applicationRelease.setVersionName(resultSet.getString("VERSION_NAME"));
                applicationRelease.setDefault(resultSet.getBoolean("IS_DEFAULT"));
                applicationRelease.setCreatedAt(resultSet.getDate("CREATED_AT"));
                applicationRelease.setReleaseChannel(resultSet.getString("RELEASE_CHANNEL"));
                applicationRelease.setReleaseDetails(resultSet.getString("RELEASE_DETAILS"));
                applicationRelease.setResource(resultSet.getString("RESOURCE"));

                sql = "SELECT * FROM APPM_RELEASE_PROPERTY WHERE APPLICATION_RELEASE_ID= ?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, resultSet.getInt("ID"));
                rsProperties = statement.executeQuery();

                Map<String, String> properties = new HashMap<>();
                while (rsProperties.next()) {
                    properties.put(rsProperties.getString("PROP_KEY"), rsProperties.getString("PROP_VAL"));
                }
                applicationRelease.setProperties(properties);
                applicationReleases.add(applicationRelease);
            }
            return applicationReleases;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection exception while trying to get the "
                    + "release details of the application with UUID " + applicationUUID, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while getting all the release details of the " + "application " + applicationUUID
                            + ", while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
            Util.cleanupResources(null, rsProperties);
        }
    }

    @Override
    public ApplicationRelease updateRelease(ApplicationRelease applicationRelease)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "UPDATE APPM_APPLICATION_RELEASE SET RESOURCE = IFNULL (?, RESOURCE), RELEASE_CHANNEL = IFNULL "
                + "(?, RELEASE_CHANNEL), RELEASE_DETAILS = IFNULL (?, RELEASE_DETAILS), IS_DEFAULT = IFNULL "
                + "(?, IS_DEFAULT) WHERE  APPM_APPLICATION_ID = ? AND VERSION_NAME = ?";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, applicationRelease.getResource());
            statement.setString(2, String.valueOf(applicationRelease.getReleaseChannel()));
            statement.setString(3, applicationRelease.getReleaseDetails());
            statement.setBoolean(4, applicationRelease.isDefault());
            statement.setInt(5, applicationRelease.getApplication().getId());
            statement.setString(6, applicationRelease.getVersionName());
            statement.executeUpdate();

            sql = "DELETE FROM APPM_RELEASE_PROPERTY WHERE APPLICATION_RELEASE_ID = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, applicationRelease.getId());
            statement.executeUpdate();
            insertApplicationReleaseProperties(connection, applicationRelease);
            return applicationRelease;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection exception while trying to update the "
                    + "Application release for the application with UUID " + applicationRelease.getApplication()
                    .getUuid() + "  for the version " + applicationRelease.getVersionName(), e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while updating the release, while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public void deleteRelease(int id, String version) throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "DELETE FROM APPM_APPLICATION_RELEASE WHERE APPM_APPLICATION_ID = ? AND VERSION_NAME = ?";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.setString(2, version);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to delete the release with version " + version, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while deleting the release with version " + version + ",while executing the query "
                            + "sql", e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public void deleteReleaseProperties(int id) throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "DELETE FROM APPM_RELEASE_PROPERTY WHERE APPLICATION_RELEASE_ID = ?";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to delete the release properties ", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while deleting the properties of application release with the id " + id, e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    /**
     * To insert the application release properties.
     * @param connection Database Connection
     * @param applicationRelease Application Release the properties of which that need to be inserted.
     * @throws SQLException SQL Exception.
     */
    private void insertApplicationReleaseProperties(Connection connection, ApplicationRelease applicationRelease)
            throws SQLException {
        String sql;
        boolean isBatchExecutionSupported = ConnectionManagerUtil.isBatchQuerySupported();

        if (applicationRelease.getProperties() != null && applicationRelease.getProperties().size() != 0) {
            sql = "INSERT INTO APPM_RELEASE_PROPERTY (PROP_KEY, PROP_VALUE, APPLICATION_RELEASE_ID) VALUES (?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            for (Object entry : applicationRelease.getProperties().entrySet()) {
                Map.Entry<String, String> property = (Map.Entry) entry;
                statement.setString(1, property.getKey());
                statement.setString(2, property.getValue());
                statement.setInt(3, applicationRelease.getId());
                if (isBatchExecutionSupported) {
                    statement.addBatch();
                } else {
                    statement.execute();
                }
            }
            if (isBatchExecutionSupported) {
                statement.executeBatch();
            }
        }
    }
}
