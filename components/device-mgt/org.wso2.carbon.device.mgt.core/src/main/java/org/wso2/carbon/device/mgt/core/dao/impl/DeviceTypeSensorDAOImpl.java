/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.sensor.mgt.DeviceTypeSensor;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceTypeSensorTransactionObject;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeSensorDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO:Complete COmments
public class DeviceTypeSensorDAOImpl implements DeviceTypeSensorDAO {
    private static final Log log = LogFactory.getLog(DeviceTypeSensorDAOImpl.class);

    @Override
    public void addDeviceTypeSensor(DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject)
            throws DeviceManagementDAOException {
        Connection conn;
        ResultSet rs;
        PreparedStatement stmt = null;

        int deviceTypeSensorId;
        int deviceTypeId = deviceTypeSensorTransactionObject.getDeviceTypeId();
        DeviceTypeSensor deviceTypeSensor = deviceTypeSensorTransactionObject.getDeviceTypeSensor();
        String deviceTypeSensorName = deviceTypeSensor.getUniqueSensorName();
        String deviceTypeSensorTypeTAG = deviceTypeSensor.getSensorTypeTAG();
        Map<String, String> deviceTypeSensorProperties = deviceTypeSensor.getStaticProperties();

        try {
            conn = this.getConnection();
            String insertDBQuery =
                    "INSERT INTO DM_DEVICE_TYPE_SENSOR (" +
                            "SENSOR_NAME," +
                            "DEVICE_TYPE_ID," +
                            "DESCRIPTION," +
                            "SENSOR_TYPE," +
                            "STREAM_DEFINITION) " +
                            "VALUES (?,?,?,?,?)";

            stmt = conn.prepareStatement(insertDBQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, deviceTypeSensorName);
            stmt.setInt(2, deviceTypeId);
            stmt.setString(3, deviceTypeSensor.getDescription());
            stmt.setString(4, deviceTypeSensorTypeTAG);
            stmt.setString(5, deviceTypeSensor.getStreamDefinition());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Details of DeviceTypeSensor [" + deviceTypeSensorName + "] of " +
                                    "DeviceType with Id [" + deviceTypeId + "] was added successfully.");
                }

                rs = stmt.getGeneratedKeys();
                rs.next();
                deviceTypeSensorId = rs.getInt(1);

                if (deviceTypeSensorProperties != null &&
                        !addDeviceTypeSensorProperties(deviceTypeSensorId, deviceTypeSensorProperties)) {
                    String msg = "Error occurred whilst adding Properties of the registered new DeviceTypeSensor " +
                            "[" + deviceTypeSensorName + "] whose Id is [" + deviceTypeSensorId + "]";
                    log.error(msg);
                    throw new DeviceManagementDAOException(msg);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new DeviceTypeSensor [" + deviceTypeSensorName +
                    "] for the DeviceType with Id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateDeviceTypeSensor(DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        int deviceTypeId = deviceTypeSensorTransactionObject.getDeviceTypeId();
        int deviceTypeSensorId = deviceTypeSensorTransactionObject.getDeviceTypeSensorId();
        DeviceTypeSensor deviceTypeSensor = deviceTypeSensorTransactionObject.getDeviceTypeSensor();
        String deviceTypeSensorName = deviceTypeSensor.getUniqueSensorName();
        String deviceTypeSensorTypeTAG = deviceTypeSensor.getSensorTypeTAG();
        Map<String, String> deviceTypeSensorProperties = deviceTypeSensor.getStaticProperties();

        try {
            conn = this.getConnection();
            String updateDBQuery =
                    "UPDATE DM_DEVICE_TYPE_SENSOR SET " +
                            "SENSOR_NAME = ?," +
                            "DESCRIPTION = ?," +
                            "SENSOR_TYPE = ?," +
                            "STREAM_DEFINITION = ? " +
                            "WHERE SENSOR_ID = ?";

            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, deviceTypeSensorName);
            stmt.setString(2, deviceTypeSensor.getDescription());
            stmt.setString(3, deviceTypeSensorTypeTAG);
            stmt.setString(4, deviceTypeSensor.getStreamDefinition());
            stmt.setInt(5, deviceTypeSensorId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Details of DeviceTypeSensor [" + deviceTypeSensorName + "] of " +
                                    "DeviceType with Id [" + deviceTypeId + "] was updated successfully.");
                }

                if (deviceTypeSensorProperties != null &&
                        !addDeviceTypeSensorProperties(deviceTypeSensorId, deviceTypeSensorProperties)) {
                    String msg = "Error occurred whilst adding Properties of the registered new DeviceTypeSensor " +
                            "[" + deviceTypeSensorName + "] whose Id is [" + deviceTypeSensorId + "]";
                    log.error(msg);
                    throw new DeviceManagementDAOException(msg);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new DeviceTypeSensor [" + deviceTypeSensorName +
                    "] for the DeviceType with Id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateDeviceTypeSensor(int deviceTypeId, DeviceTypeSensor deviceTypeSensor)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet;

        int deviceTypeSensorId = -1;
        String deviceTypeSensorName = deviceTypeSensor.getUniqueSensorName();
        String deviceTypeSensorTypeTAG = deviceTypeSensor.getSensorTypeTAG();
        Map<String, String> deviceTypeSensorProperties = deviceTypeSensor.getStaticProperties();

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT SENSOR_ID " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ? AND SENSOR_NAME = ?";

            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setString(2, deviceTypeSensorName);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                deviceTypeSensorId = resultSet.getInt(DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_ID);
            }
            stmt.close();

            String updateDBQuery =
                    "UPDATE DM_DEVICE_TYPE_SENSOR SET " +
                            "DESCRIPTION = ?," +
                            "SENSOR_TYPE = ?," +
                            "STREAM_DEFINITION = ? " +
                            "WHERE DEVICE_TYPE_ID = ? AND SENSOR_NAME = ?";

            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, deviceTypeSensor.getDescription());
            stmt.setString(2, deviceTypeSensorTypeTAG);
            stmt.setString(3, deviceTypeSensor.getStreamDefinition());
            stmt.setInt(4, deviceTypeId);
            stmt.setString(5, deviceTypeSensorName);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Details of DeviceTypeSensor [" + deviceTypeSensorName + "] of " +
                                    "DeviceType with Id [" + deviceTypeId + "] was updated successfully.");
                }

                if (!addDeviceTypeSensorProperties(deviceTypeSensorId, deviceTypeSensorProperties)) {
                    String msg = "Error occurred whilst adding Properties of the registered new DeviceTypeSensor " +
                            "[" + deviceTypeSensorName + "] whose Id is [" + deviceTypeSensorId + "]";
                    log.error(msg);
                    throw new DeviceManagementDAOException(msg);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new DeviceTypeSensor [" + deviceTypeSensorName +
                    "] for the DeviceType with Id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<DeviceTypeSensorTransactionObject> getDeviceTypeSensors(int deviceTypeId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceTypeSensorTransactionObject> deviceTypeSensorTransactionObjects = new ArrayList<>();

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT  SENSOR_ID, " +
                            "SENSOR_NAME, " +
                            "DESCRIPTION, " +
                            "SENSOR_TYPE, " +
                            "STREAM_DEFINITION " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String deviceTypeSensorName = resultSet.getString(
                        DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_NAME);
                String deviceTypeSensorTypeTAG = resultSet.getString(
                        DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_TYPE);
                int deviceTypeSensorId = resultSet.getInt(DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_ID);
                Map<String, String> staticProperties = getDeviceTypeSensorProperties(deviceTypeSensorId);

                DeviceTypeSensor deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setUniqueSensorName(deviceTypeSensorName);
                deviceTypeSensor.setDescription(
                        resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorTypeTAG(deviceTypeSensorTypeTAG);
                deviceTypeSensor.setStaticProperties(staticProperties);
                deviceTypeSensor.setStreamDefinition(
                        resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.STREAM_DEFINITION));

                DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject =
                        new DeviceTypeSensorTransactionObject(deviceTypeSensor);
                deviceTypeSensorTransactionObject.setDeviceTypeSensorId(deviceTypeSensorId);
                deviceTypeSensorTransactionObject.setDeviceTypeId(deviceTypeId);
                deviceTypeSensorTransactionObjects.add(deviceTypeSensorTransactionObject);
            }
            return deviceTypeSensorTransactionObjects;
        } catch (SQLException e) {
            String msg =
                    "A SQL error occurred whilst trying to get all the DeviceTypeSensors of the device-type with " +
                            "id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    //TODO: Check deviceType in SensorObject
    @Override
    public DeviceTypeSensorTransactionObject getDeviceTypeSensor(int deviceTypeId, String sensorName)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject = null;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT  SENSOR_ID, " +
                            "DESCRIPTION, " +
                            "SENSOR_TYPE, " +
                            "STREAM_DEFINITION " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ? AND SENSOR_NAME = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setString(2, sensorName);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String deviceTypeSensorTypeTAG = resultSet.getString(
                        DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_TYPE);
                int deviceTypeSensorId = resultSet.getInt(
                        DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_ID);
                Map<String, String> staticProperties = getDeviceTypeSensorProperties(deviceTypeSensorId);

                DeviceTypeSensor deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setUniqueSensorName(sensorName);
                deviceTypeSensor.setDescription(
                        resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorTypeTAG(deviceTypeSensorTypeTAG);
                deviceTypeSensor.setStaticProperties(staticProperties);
                deviceTypeSensor.setStreamDefinition(
                        resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.STREAM_DEFINITION));

                deviceTypeSensorTransactionObject = new DeviceTypeSensorTransactionObject(deviceTypeSensor);
                deviceTypeSensorTransactionObject.setDeviceTypeSensorId(deviceTypeSensorId);
                deviceTypeSensorTransactionObject.setDeviceTypeId(deviceTypeId);
            }

            return deviceTypeSensorTransactionObject;
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the DeviceTypeSensor [" + sensorName + "] " +
                    "of the device-type with id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public DeviceTypeSensorTransactionObject getDeviceTypeSensor(int deviceTypeSensorId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject = null;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT  SENSOR_NAME, " +
                            "DEVICE_TYPE_ID, " +
                            "DESCRIPTION, " +
                            "SENSOR_TYPE, " +
                            "STREAM_DEFINITION " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE SENSOR_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeSensorId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String sensorName = resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_NAME);
                String deviceTypeSensorTypeTAG = resultSet.getString(
                        DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_TYPE);
                int deviceTypeId = resultSet.getInt(DeviceTypeSensorTransactionObject.DAOConstants.DEVICE_TYPE_ID);
                Map<String, String> staticProperties = getDeviceTypeSensorProperties(deviceTypeSensorId);

                DeviceTypeSensor deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setUniqueSensorName(sensorName);
                deviceTypeSensor.setDescription(resultSet.getString(
                        DeviceTypeSensorTransactionObject.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorTypeTAG(deviceTypeSensorTypeTAG);
                deviceTypeSensor.setStaticProperties(staticProperties);
                deviceTypeSensor.setStreamDefinition(
                        resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.STREAM_DEFINITION));

                deviceTypeSensorTransactionObject = new DeviceTypeSensorTransactionObject(deviceTypeSensor);
                deviceTypeSensorTransactionObject.setDeviceTypeSensorId(deviceTypeSensorId);
                deviceTypeSensorTransactionObject.setDeviceTypeId(deviceTypeId);
            }

            return deviceTypeSensorTransactionObject;
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the DeviceTypeSensor with " +
                    "ID [" + deviceTypeSensorId + "].";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public void removeDeviceTypeSensor(int deviceTypeId, String sensorName) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet;
        int deviceTypeSensorId = -1;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT  SENSOR_ID " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ? AND SENSOR_NAME = ?";

            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setString(2, sensorName);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                deviceTypeSensorId = resultSet.getInt(DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_ID);
            }
            stmt.close();

            String deleteDBQuery =
                    "DELETE FROM DM_DEVICE_TYPE_SENSOR WHERE DEVICE_TYPE_ID = ? AND SENSOR_NAME = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setString(2, sensorName);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("DeviceTypeSensor [" + sensorName + "] of device-type with Id [" + deviceTypeId + "] " +
                                      "has been deleted successfully.");
                }

                if (!removeDeviceTypeSensorProperties(deviceTypeSensorId)) {
                    String msg = "Error occurred whilst deleting Properties of the DeviceTypeSensor with " +
                            "Id [" + deviceTypeSensorId + "]";
                    log.error(msg);
                    throw new DeviceManagementDAOException(msg);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the DeviceTypeSensor [" + sensorName + "] of the " +
                    "device with Id [" + deviceTypeId + "].";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeDeviceTypeSensor(int deviceTypeSensorId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM DM_DEVICE_TYPE_SENSOR WHERE SENSOR_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setInt(1, deviceTypeSensorId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("DeviceTypeSensor with Id [" + deviceTypeSensorId + "] has been deleted successfully.");
                }

                if (!removeDeviceTypeSensorProperties(deviceTypeSensorId)) {
                    String msg = "Error occurred whilst deleting Properties of the DeviceTypeSensor with " +
                            "Id [" + deviceTypeSensorId + "]";
                    log.error(msg);
                    throw new DeviceManagementDAOException(msg);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the DeviceTypeSensor with " +
                    "Id [" + deviceTypeSensorId + "].";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeDeviceTypeSensors(int deviceTypeId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet;
        int deviceTypeSensorId;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT SENSOR_ID " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ?";

            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                deviceTypeSensorId = resultSet.getInt(DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_ID);
                if (!removeDeviceTypeSensorProperties(deviceTypeSensorId)) {
                    String msg = "Error occurred whilst deleting Properties of the DeviceTypeSensor with " +
                            "Id [" + deviceTypeSensorId + "] of DeviceType with Id [" + deviceTypeId + "]";
                    log.error(msg);
                    throw new DeviceManagementDAOException(msg);
                }
            }
            stmt.close();

            String deleteDBQuery =
                    "DELETE FROM DM_DEVICE_TYPE_SENSOR WHERE DEVICE_TYPE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setInt(1, deviceTypeId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("All DeviceTypeSensors of device-type with Id [" + deviceTypeId + "] has been " +
                                      "deleted successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the DeviceTypeSensors of the device with " +
                    "Id [" + deviceTypeId + "].";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    /**
     * @return
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    /**
     * @param deviceTypeSensorId
     * @param deviceTypeSensorProperties
     * @return
     * @throws DeviceManagementDAOException
     */
    private boolean addDeviceTypeSensorProperties(int deviceTypeSensorId,
                                                  Map<String, String> deviceTypeSensorProperties)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            for (String property : deviceTypeSensorProperties.keySet()) {
                String value = deviceTypeSensorProperties.get(property);

                conn = this.getConnection();
                String insertDBQuery =
                        "INSERT INTO DM_DEVICE_TYPE_SENSOR_STATIC_PROPERTIES (" +
                                "SENSOR_ID," +
                                "PROPERTY_KEY," +
                                "PROPERTY_VALUE) " +
                                "VALUES (?,?,?)";

                stmt = conn.prepareStatement(insertDBQuery);
                stmt.setInt(1, deviceTypeSensorId);
                stmt.setString(2, property);
                stmt.setString(3, value);
                int rows = stmt.executeUpdate();

                if (rows > 0 && log.isDebugEnabled()) {
                    log.debug("Properties of DeviceTypeSensor with Id [" + deviceTypeSensorId + "] " +
                                      "was added successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst adding properties (after adding the sensor) of a new " +
                    "DeviceTypeSensor whose is Id [" + deviceTypeSensorId + "].";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }


    private Map<String, String> getDeviceTypeSensorProperties(int deviceTypeSensorId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Map<String, String> deviceTypeSensorProperties = new HashMap<>();

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT PROPERTY_KEY, " +
                            "PROPERTY_VALUE " +
                            "FROM DM_DEVICE_TYPE_SENSOR_STATIC_PROPERTIES " +
                            "WHERE SENSOR_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeSensorId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String propertyKey = resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.PROPERTY_KEY);
                String propertyVal = resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.PROPERTY_VALUE);
                deviceTypeSensorProperties.put(propertyKey, propertyVal);
            }
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the properties of DeviceTypeSensor with " +
                    "ID [" + deviceTypeSensorId + "].";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceTypeSensorProperties;
    }

    private boolean removeDeviceTypeSensorProperties(int deviceTypeSensorId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM DM_DEVICE_TYPE_SENSOR_STATIC_PROPERTIES WHERE SENSOR_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setInt(1, deviceTypeSensorId);
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug("Properties of DeviceTypeSensor with Id [" + deviceTypeSensorId + "] " +
                                  "was deleted successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst removing properties (after removing the sensor) of the " +
                    "DeviceTypeSensor whose is Id [" + deviceTypeSensorId + "].";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }
}