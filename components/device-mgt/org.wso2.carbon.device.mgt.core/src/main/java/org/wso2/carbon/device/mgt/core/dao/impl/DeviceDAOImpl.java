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
package org.wso2.carbon.device.mgt.core.dao.impl;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dao.DeviceAgentDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * DAO for Device
 */
public class DeviceDAOImpl implements DeviceDAO {

    @Override
    public Optional<Device> getDevice(String id, String type) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = DeviceManagementDAOFactory.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d" +
                    ".NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE " +
                    "t.NAME = ? AND t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 " +
                    "WHERE d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC, e.STATUS ASC";
            // Status adeed as an orderby clause to fix a bug : when an existing device is
            // re-enrolled, earlier enrollment is marked as removed and a new enrollment is added.
            // However, both enrollments share the same time stamp. When retrieving the device
            // due to same timestamp, enrollment information is incorrect, intermittently. Hence
            // status also should be taken into consideration when ordering. This should not present a
            // problem for other status transitions, as there would be an intermediary removed
            // state in between.
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type);
            stmt.setString(2, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = new Device();
                device.setId(rs.getInt("DEVICE_ID"));
                device.setName(rs.getString("DEVICE_NAME"));
                device.setDescription(rs.getString("DESCRIPTION"));
                device.setType(rs.getString("DEVICE_TYPE"));
                device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));

                EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
                enrolmentInfo.setId(rs.getInt("ENROLMENT_ID"));
                enrolmentInfo.setOwner(rs.getString("OWNER"));
                enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(rs.getString("OWNERSHIP")));
                enrolmentInfo.setDateOfEnrolment(rs.getTimestamp("DATE_OF_ENROLMENT").getTime());
                enrolmentInfo.setDateOfLastUpdate(rs.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
                enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("STATUS")));
                device.setEnrolmentInfo(enrolmentInfo);
            }
            return Optional.ofNullable(device);
        } catch (SQLException e) {
            throw new DeviceManagementDAOException(
                    "Error occurred while fetching " + type + " device with the id:" + id, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }
}
