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

import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.GenericApplicationDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This is a ApplicationDAO Implementation specific to Oracle.
 */
public class OracleApplicationDAOImpl extends GenericApplicationDAOImpl {

    @Override
    protected PreparedStatement generateGetApplicationsStatement(Filter filter, Connection conn,
            int tenantId) throws SQLException {
        int index = 0;
        String sql = "SELECT APP.*, APL.NAME AS APL_NAME, APL.IDENTIFIER AS APL_IDENTIFIER, CAT.ID AS CAT_ID, "
                + "CAT.NAME AS CAT_NAME,  LS.NAME AS LS_NAME, LS.IDENTIFIER AS LS_IDENTIFIER, "
                + "LS.DESCRIPTION AS LS_DESCRIPTION " + "FROM APPM_APPLICATION APP " + "INNER JOIN APPM_PLATFORM APL "
                + "ON APP.PLATFORM_ID = APL.ID " + "INNER JOIN APPM_APPLICATION_CATEGORY CAT "
                + "ON APP.APPLICATION_CATEGORY_ID = CAT.ID " + "INNER JOIN APPM_LIFECYCLE_STATE LS "
                + "ON APP.LIFECYCLE_STATE_ID = LS.ID WHERE APP.TENANT_ID = ? ";

        String userName = filter.getUserName();
        if (!userName.equals("ALL")) {
            sql += " AND APP.CREATED_BY = ? ";
        }
        if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
            sql += "AND APP.NAME LIKE ? ";
        }

        sql += " ORDER BY APP.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(++index, tenantId);

        if (!userName.equals("ALL")) {
            stmt.setString(++index, userName);
        }
        if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
            stmt.setString(++index, "%" + filter.getSearchQuery() + "%");
        }
        stmt.setInt(++index, filter.getOffset());
        stmt.setInt(++index, filter.getLimit());
        return stmt;
    }

}
