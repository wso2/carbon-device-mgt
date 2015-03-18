/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.policy.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.core.dao.PolicyDAO;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyDAOImpl implements PolicyDAO {

    private static final Log log = LogFactory.getLog(PolicyDAOImpl.class);


    @Override
    public Policy  addPolicy(Policy policy) throws PolicyManagerDAOException {
        persistPolicy(policy);
        return policy;
    }

    @Override
    public Policy addPolicy(String deviceType, Policy policy) throws PolicyManagerDAOException {

        // First persist the policy to the data base.
        persistPolicy(policy);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_DEVICE_TYPE_POLICY (DEVICE_TYPE_ID, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, getDeviceTypeId(deviceType));
            stmt.setInt(2, policy.getId());

            stmt.executeQuery();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the device type policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }

        return policy;

    }

    @Override
    public Policy addPolicyToRole(String roleName, Policy policy) throws PolicyManagerDAOException {
        return null;
    }

    @Override
    public Policy addPolicy(String deviceID, String deviceType, Policy policy) throws PolicyManagerDAOException {

        // First persist the policy to the data base.
        persistPolicy(policy);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (SQLException e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return policy;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (Exception e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return policy;
    }

    @Override
    public Policy getPolicy() throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (Exception e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return null;
    }

    @Override
    public Policy getPolicy(String deviceType) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (Exception e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return null;
    }

    @Override
    public Policy getPolicy(String deviceID, String deviceType) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (Exception e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return null;
    }

    @Override
    public void deletePolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Policy (" + policy.getPolicyName() + ") delete from database.");
            }
        } catch (Exception e) {
            String msg = "Unable to delete the policy (" + policy.getPolicyName() + ") from database.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

    }

    private Connection getConnection() throws PolicyManagerDAOException {
        try {
            return PolicyManagementDAOFactory.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while obtaining a connection from the policy " +
                    "management metadata repository datasource", e);
        }
    }


    private void persistPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        // TODO : find a way to get the tenant Id.
        int tenantId = -1234;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY (NAME, PROFILE_ID, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query, stmt.RETURN_GENERATED_KEYS);

            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getProfile().getProfileId());
            stmt.setInt(3, tenantId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0 && log.isDebugEnabled()) {
                String msg = "No rows are updated on the policy table.";
                log.debug(msg);
            }
            generatedKeys = stmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                policy.setId(generatedKeys.getInt(1));
            }
            // checking policy id here, because it object could have passed with id from the calling method.
            if (policy.getId() == 0) {
                throw new RuntimeException("No rows were inserted, policy id cannot be null.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding policy to the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }
    }








    /**
     * This method returns the device type id when supplied with device type name.
     *
     * @param deviceType
     * @return
     * @throws PolicyManagerDAOException
     */
    private int getDeviceTypeId(String deviceType) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int deviceTypeId = -1;
        try {
            conn = this.getConnection();
            String query = "SELECT ID FROM DM_DEVICE_TYPE WHERE NAME = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, deviceType);

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                deviceTypeId = resultSet.getInt("ID");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while selecting the device type id.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return deviceTypeId;
    }

}
