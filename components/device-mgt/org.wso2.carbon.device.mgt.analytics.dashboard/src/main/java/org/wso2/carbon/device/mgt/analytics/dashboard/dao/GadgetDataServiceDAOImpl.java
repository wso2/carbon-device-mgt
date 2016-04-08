package org.wso2.carbon.device.mgt.analytics.dashboard.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class GadgetDataServiceDAOImpl implements GadgetDataServiceDAO {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(GadgetDataServiceDAOImpl.class);

    @Override
    public int getTotalFilteredDeviceCount(String[] filters) throws GadgetDataServiceDAOException {
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        int totalFilteredDeviceCount = 0;
        try {
            con = this.getConnection();
            String sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES WHERE TENANT_ID = ?";
            if (filters.length > 0) {

            }
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                totalFilteredDeviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new GadgetDataServiceDAOException("Error occurred while fetching the registered device types", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return totalFilteredDeviceCount;
    }

    @Override
    public int getNonCompliantDeviceCount(String[] filters) {
        return 50;
    }

    @Override
    public int getUnmonitoredDeviceCount(String[] filters) {
        return 60;
    }

    @Override
    public int getAndroidDeviceCount(String[] filters) {
        return 0;
    }

    @Override
    public int getBYODDeviceCount(String[] filters) {
        return 0;
    }

    @Override
    public int getCOPEDeviceCount(String[] filters) {
        return 0;
    }

    private Connection getConnection() throws SQLException {
        return GadgetDataServiceDAOFactory.getConnection();
    }
}
