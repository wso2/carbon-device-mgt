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
package org.wso2.carbon.device.application.mgt.core.dao.impl;

import org.json.JSONException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationManagementDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationManagementDAOUtil;
import org.wso2.carbon.device.application.mgt.core.dto.Application;
import org.wso2.carbon.device.application.mgt.core.dto.ApplicationList;
import org.wso2.carbon.device.application.mgt.core.dto.Pagination;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericAppManagementDAO implements ApplicationManagementDAO {

    @Override
    public void createApplication(Application application) throws ApplicationManagementDAOException {

    }

    @Override
    public ApplicationList getApplications() throws ApplicationManagementDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = null;
        ApplicationList applicationList = new ApplicationList();
        List<Application> applications = new ArrayList<>();
        Pagination pagination = new Pagination();

        try {

            conn = ConnectionManagerUtil.getCurrentConnection().get();

            sql = "SELECT SQL_CALC_FOUND_ROWS AP.*, AT.NAME AS AT_NAME, AT.CODE AS AT_CODE, CT.NAME AS CT_NAME " +
                    "FROM APPM_APPLICATION AS AP " +
                    "INNER JOIN APPM_APPLICATION_TYPE AS AT ON AP.APPLICATION_TYPE_ID = AT.ID " +
                    "INNER JOIN APPM_APPLICATION_CATEGORY AS CT ON AP.CATEGORY_ID = CT.ID;";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            int length = 0;
            sql = "SELECT FOUND_ROWS() AS COUNT;";
            stmt = conn.prepareStatement(sql);
            ResultSet rsCount = stmt.executeQuery();
            if(rsCount.next()){
                pagination.setCount(rsCount.getInt("COUNT"));
            }

            while (rs.next()) {

                //Getting properties
                sql = "SELECT * FROM APPM_APPLICATION_PROPERTIES WHERE APPLICATION_ID=?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, rs.getInt("ID"));
                ResultSet rsProperties = stmt.executeQuery();

                applications.add(ApplicationManagementDAOUtil.loadApplication(rs, rsProperties));
                length++;
            }

            pagination.setLength(length);

            applicationList.setApplications(applications);
            applicationList.setPagination(pagination);

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } finally {
            ApplicationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return applicationList;

    }
}
