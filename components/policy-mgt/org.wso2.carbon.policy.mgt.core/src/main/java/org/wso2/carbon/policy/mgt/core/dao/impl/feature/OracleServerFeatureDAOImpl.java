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
package org.wso2.carbon.policy.mgt.core.dao.impl.feature;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class OracleServerFeatureDAOImpl extends AbstractFeatureDAO {
    /**
     * Batch sizes greater than 10 throws array out of bound exception.
     */
    private static int BATCH_SIZE = 10;

    @Override
    public List<ProfileFeature> addProfileFeatures(List<ProfileFeature> features, int profileId) throws
            FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_PROFILE_FEATURES (PROFILE_ID, FEATURE_CODE, DEVICE_TYPE, CONTENT, " +
                    "TENANT_ID) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query, new String[]{"id"});
            int noRecords = 0;
            for (ProfileFeature feature : features) {
                stmt.setInt(1, profileId);
                stmt.setString(2, feature.getFeatureCode());
                stmt.setString(3, feature.getDeviceType());
                stmt.setBytes(4, PolicyManagerUtil.getBytes(feature.getContent()));
                stmt.setInt(5, tenantId);
                stmt.addBatch();
                noRecords++;
                if (noRecords >= BATCH_SIZE && noRecords % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    generatedKeys = stmt.getGeneratedKeys();
                    int i = noRecords - this.BATCH_SIZE;
                    while (generatedKeys.next()) {
                        features.get(i).setId(generatedKeys.getInt(1));
                        i++;
                    }
                }
            }
            stmt.executeBatch();
            generatedKeys = stmt.getGeneratedKeys();
            int i = 0;
            if (noRecords > BATCH_SIZE) {
                i = noRecords - BATCH_SIZE;
            }
            while (generatedKeys.next()) {
                features.get(i).setId(generatedKeys.getInt(1));
                i++;
            }
        } catch (SQLException | IOException e) {
            throw new FeatureManagerDAOException("Error occurred while adding the feature list to the database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return features;
    }

    private Connection getConnection() throws FeatureManagerDAOException {
        return PolicyManagementDAOFactory.getConnection();
    }
}
