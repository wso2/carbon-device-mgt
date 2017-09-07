/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.dao.impl.visibility;

import org.wso2.carbon.device.application.mgt.common.Visibility;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.VisibilityManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic database level implementation for the DAO which can be used by different databases.
 */
public class GenericVisibilityDAOImpl extends AbstractDAOImpl implements VisibilityDAO {

    @Override
    public int getVisibilityID(Visibility.Type visibilityType) throws VisibilityManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection connection = getDBConnection();
            String sql = "SELECT ID FROM APPM_RESOURCE_TYPE WHERE NAME = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, visibilityType.toString().toUpperCase());
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID");
            }
            return -1;
        } catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the connection " +
                    "for the visibility management of applications", e);
        } catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred when trying to get the ID of the" +
                    " visibility type - " + visibilityType.toString(), e);
        } finally {
            Util.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public void add(int applicationID, int visibilityTypeID, List<String> allowedList)
            throws VisibilityManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection connection = getDBConnection();
            String sql = "INSERT INTO APPM_VISIBILITY (VALUE, RESOURCE_TYPE_ID, APPLICATION_ID) VALUES (?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            if (allowedList == null) {
                stmt.setString(1, null);
                stmt.setInt(2, visibilityTypeID);
                stmt.setInt(3, applicationID);
                stmt.execute();
            } else {
                for (String allowed : allowedList) {
                    stmt.setString(1, allowed);
                    stmt.setInt(2, visibilityTypeID);
                    stmt.setInt(3, applicationID);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the connection " +
                    "for adding the visibility mapping for the application ID - " + applicationID, e);
        } catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while adding the visibility mapping " +
                    "for the application ID - " + applicationID, e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public void delete(int applicationId) throws VisibilityManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection connection = getDBConnection();
            String sql = "DELETE FROM APPM_VISIBILITY WHERE APPLICATION_ID = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, applicationId);
            stmt.execute();
        } catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the connection " +
                    "for deleting the visibility mapping for the application ID - " + applicationId, e);
        } catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while deleting the visibility mapping " +
                    "for the application ID - " + applicationId, e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    public Visibility get(int applicationId) throws VisibilityManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        final String visibilityTypeColumn = "VISIBILITY_TYPE";
        final String allowedValColumn = "ALLOWED_VAL";
        try {
            Connection connection = getDBConnection();
            String sql = "SELECT APPM_VISIBILITY.VALUE as " + allowedValColumn + ", APPM_RESOURCE_TYPE.NAME AS " +
                    visibilityTypeColumn + " FROM APPM_VISIBILITY JOIN APPM_RESOURCE_TYPE " +
                    "ON APPM_VISIBILITY.RESOURCE_TYPE_ID = APPM_RESOURCE_TYPE.ID " +
                    "WHERE APPM_VISIBILITY.APPLICATION_ID = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, applicationId);
            resultSet = stmt.executeQuery();
            Visibility visibility = new Visibility();
            List<String> allowedVal = new ArrayList<>();
            while (resultSet.next()) {
                if (visibility.getType() == null) {
                    visibility.setType(Visibility.Type.valueOf(resultSet.getString(visibilityTypeColumn)));
                }
                String val = resultSet.getString(allowedValColumn);
                if (val != null) {
                    allowedVal.add(val);
                }
            }
            if (!allowedVal.isEmpty()) {
                visibility.setAllowedList(allowedVal);
            }
            return visibility;
        } catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the connection " +
                    "for getting the visibility mapping for the application ID - " + applicationId, e);
        } catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while getting the visibility mapping " +
                    "for the application ID - " + applicationId, e);
        } finally {
            Util.cleanupResources(stmt, resultSet);
        }
    }
}
