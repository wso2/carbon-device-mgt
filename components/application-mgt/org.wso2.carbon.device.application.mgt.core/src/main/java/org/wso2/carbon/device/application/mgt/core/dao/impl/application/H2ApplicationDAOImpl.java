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
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class holds the generic implementation of ApplicationDAO which can be used to support ANSI db syntax.
 */
public class H2ApplicationDAOImpl extends AbstractApplicationDAOImpl {

    private static final Log log = LogFactory.getLog(H2ApplicationDAOImpl.class);

    @Override
    public void changeLifecycle(String applicationUUID, String lifecycleIdentifier) throws ApplicationManagementDAOException {

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

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementDAOException {

        if(log.isDebugEnabled()){
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

            sql += "SELECT APP.*, APL.NAME AS APL_NAME, APL.IDENTIFIER AS APL_IDENTIFIER," +
                    " CAT.NAME AS CAT_NAME ";
            sql += "FROM APPM_APPLICATION AS APP ";
            sql += "INNER JOIN APPM_PLATFORM_APPLICATION_MAPPING AS APM ON APP.PLATFORM_APPLICATION_MAPPING_ID = APM.ID ";
            sql += "INNER JOIN APPM_PLATFORM AS APL ON APM.PLATFORM_ID = APL.ID ";
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
    public Application getApplication(String uuid) throws ApplicationManagementDAOException {
        return null;
    }

    @Override
    public int getApplicationId(String uuid) throws ApplicationManagementDAOException {
        return 0;
    }

    @Override
    public void deleteApplication(String uuid) throws ApplicationManagementDAOException {

    }

    @Override
    public void deleteProperties(int applicationId) throws ApplicationManagementDAOException {

    }

    @Override
    public void deleteTags(int applicationId) throws ApplicationManagementDAOException {

    }

    @Override
    public Application editApplication(Application application) throws ApplicationManagementDAOException {
        return null;
    }

    @Override
    public void addProperties(Map<String, String> properties) throws ApplicationManagementDAOException {
        
    }

    @Override
    public void editProperties(Map<String, String> properties) throws ApplicationManagementDAOException {

    }

    @Override
    public void changeLifeCycle(LifecycleState lifecycleState) throws ApplicationManagementDAOException {

    }

    @Override
    public void addRelease(ApplicationRelease release) throws ApplicationManagementDAOException {

    }


}
