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
import org.wso2.carbon.device.application.mgt.core.dto.Filter;
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
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementDAOException {

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

            conn = ConnectionManagerUtil.getCurrentConnection().get();

            sql += "SELECT SQL_CALC_FOUND_ROWS APP.*, APL.NAME AS APL_NAME, APL.CODE AS APL_CODE, CAT.NAME AS CAT_NAME ";
            sql += "FROM APPM_APPLICATION AS APP ";
            sql += "INNER JOIN APPM_PLATFORM_APPLICATION_MAPPING AS APM ON APP.PLATFORM_APPLICATION_MAPPING_ID = APM.ID ";
            sql += "INNER JOIN APPM_PLATFORM AS APL ON APM.PLATFORM_ID = APL.ID ";
            sql += "INNER JOIN APPM_APPLICATION_CATEGORY AS CAT ON APP.APPLICATION_CATEGORY_ID = CAT.ID ";
            if (filter.getSearchQuery() != null || "".equals(filter.getSearchQuery())) {
                sql += "WHERE APP.NAME LIKE ? ";
            }
            sql += "LIMIT ? ";
            sql += "OFFSET ?;";

            stmt = conn.prepareStatement(sql);
            int index = 0;
            if (filter.getSearchQuery() != null || "".equals(filter.getSearchQuery())) {
                stmt.setString(++index, "%" + filter.getSearchQuery() + "%");
            }
            stmt.setInt(++index, filter.getLimit());
            stmt.setInt(++index, filter.getOffset());

            rs = stmt.executeQuery();

            int length = 0;
            sql = "SELECT FOUND_ROWS() AS COUNT;";
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

                applications.add(ApplicationManagementDAOUtil.loadApplication(rs, rsProperties));
                length++;
            }

            pagination.setSize(length);

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
