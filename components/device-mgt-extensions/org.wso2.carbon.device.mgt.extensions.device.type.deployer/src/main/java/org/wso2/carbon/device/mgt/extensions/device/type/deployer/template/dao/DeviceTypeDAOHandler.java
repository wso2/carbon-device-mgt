package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeDeployerFileException;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeMgtPluginException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This component handles the connections
 */
public class DeviceTypeDAOHandler {

    private static final Log log = LogFactory.getLog(DeviceTypeDAOHandler.class);

    private DataSource dataSource;
    private ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public DeviceTypeDAOHandler(String datasourceName) {
        initDAO(datasourceName);
    }

    public void initDAO(String datasourceName) {
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(datasourceName);
        } catch (NamingException e) {
            throw new DeviceTypeDeployerFileException("Error while looking up the data source: " + datasourceName, e);
        }
    }

    public void beginTransaction() throws DeviceTypeMgtPluginException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new DeviceTypeMgtPluginException("Error occurred while retrieving datasource connection", e);
        }
    }

    public Connection getConnection() throws DeviceTypeMgtPluginException {
        if (currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
            } catch (SQLException e) {
                throw new DeviceTypeMgtPluginException("Error occurred while retrieving data source connection", e);
            }
        }
        return currentConnection.get();
    }

    public void commitTransaction() throws DeviceTypeMgtPluginException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.commit();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence commit "
                                      + "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new DeviceTypeMgtPluginException("Error occurred while committing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public void closeConnection() throws DeviceTypeMgtPluginException {

        Connection con = currentConnection.get();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("Error occurred while close the connection");
            }
        }
        currentConnection.remove();
    }

    public void rollbackTransaction() throws DeviceTypeMgtPluginException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.rollback();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence rollback "
                                      + "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new DeviceTypeMgtPluginException("Error occurred while rollback the transaction", e);
        } finally {
            closeConnection();
        }
    }
}
