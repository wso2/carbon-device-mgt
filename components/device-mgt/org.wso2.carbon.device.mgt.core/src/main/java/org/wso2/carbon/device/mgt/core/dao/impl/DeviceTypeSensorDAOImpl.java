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
import org.wso2.carbon.device.mgt.common.sensor.mgt.SensorType;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeSensorDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO:Complete COmments
public class DeviceTypeSensorDAOImpl implements DeviceTypeSensorDAO {
    private static final Log log = LogFactory.getLog(DeviceTypeSensorDAOImpl.class);

    @Override
    public void addDeviceTypeSensor(int deviceTypeId, DeviceTypeSensor deviceTypeSensor)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        byte[] sensorTypeAsBytes;
        byte[] sensorPropertiesAsBytes;
        ByteArrayInputStream sensorTypeByteStream;
        ByteArrayInputStream sensorPropertiesByteStream;

        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            // Write byte stream of the SensorType Object of this DeviceType Sensor.
            objectOutputStream.writeObject(deviceTypeSensor.getSensorType());
            sensorTypeAsBytes = byteArrayOutputStream.toByteArray();
            sensorTypeByteStream = new ByteArrayInputStream(sensorTypeAsBytes);
            // Flush the ByteStream plus the ObjectStreams before reuse.
            byteArrayOutputStream.flush();
            objectOutputStream.flush();
            // Write byte stream of the properties HashMap Object of this DeviceType Sensor
            objectOutputStream.writeObject(deviceTypeSensor.getStaticProperties());
            sensorPropertiesAsBytes = byteArrayOutputStream.toByteArray();
            sensorPropertiesByteStream = new ByteArrayInputStream(sensorPropertiesAsBytes);

            conn = this.getConnection();
            String insertDBQuery =
                    "INSERT INTO DM_DEVICE_TYPE_SENSOR (" +
                            "SENSOR_NAME," +
                            "DEVICE_TYPE_ID," +
                            "DESCRIPTION," +
                            "SENSOR_TYPE," +
                            "STATIC_PROPERTIES," +
                            "STREAM_DEFINITION) " +
                            "VALUES (?,?,?,?,?,?)";

            stmt = conn.prepareStatement(insertDBQuery);
            stmt.setString(1, deviceTypeSensor.getName());
            stmt.setInt(2, deviceTypeId);
            stmt.setString(3, deviceTypeSensor.getDescription());
            stmt.setBinaryStream(4, sensorTypeByteStream, sensorTypeAsBytes.length);
            stmt.setBinaryStream(5, sensorPropertiesByteStream, sensorPropertiesAsBytes.length);
            stmt.setString(6, deviceTypeSensor.getStreamDefinition());
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug(
                        "Details of DeviceTypeSensor [" + deviceTypeSensor.getName() + "] of " +
                                "DeviceType with Id [" + deviceTypeId + "] was added successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new DeviceTypeSensor [" + deviceTypeSensor.getName() +
                    "] for the DeviceType with Id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred whilst trying to get the byte streams of the " +
                    "'sensorType' Object and 'staticProperties' HashMap of the " +
                    "DeviceTypeSensor [" + deviceTypeSensor.getName() + "] to store as BLOBs in the DB";
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

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        byte[] sensorTypeAsBytes;
        byte[] sensorPropertiesAsBytes;
        ByteArrayInputStream sensorTypeByteStream;
        ByteArrayInputStream sensorPropertiesByteStream;

        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            // Write byte stream of the SensorType Object of this DeviceType Sensor.
            objectOutputStream.writeObject(deviceTypeSensor.getSensorType());
            sensorTypeAsBytes = byteArrayOutputStream.toByteArray();
            sensorTypeByteStream = new ByteArrayInputStream(sensorTypeAsBytes);
            // Flush the ByteStream plus the ObjectStreams before reuse.
            byteArrayOutputStream.flush();
            objectOutputStream.flush();
            // Write byte stream of the properties HashMap Object of this DeviceType Sensor
            objectOutputStream.writeObject(deviceTypeSensor.getStaticProperties());
            sensorPropertiesAsBytes = byteArrayOutputStream.toByteArray();
            sensorPropertiesByteStream = new ByteArrayInputStream(sensorPropertiesAsBytes);

            conn = this.getConnection();
            String updateDBQuery =
                    "UPDATE DM_DEVICE_TYPE_SENSOR SET " +
                            "SENSOR_NAME = ?," +
                            "DESCRIPTION = ?," +
                            "SENSOR_TYPE = ?," +
                            "STATIC_PROPERTIES = ?," +
                            "STREAM_DEFINITION = ? " +
                            "WHERE SENSOR_ID = ? AND DEVICE_TYPE_ID = ?";

            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, deviceTypeSensor.getName());
            stmt.setString(2, deviceTypeSensor.getDescription());
            stmt.setBinaryStream(3, sensorTypeByteStream, sensorTypeAsBytes.length);
            stmt.setBinaryStream(4, sensorPropertiesByteStream, sensorPropertiesAsBytes.length);
            stmt.setString(5, deviceTypeSensor.getStreamDefinition());
            stmt.setInt(6, deviceTypeSensor.getTypeId());
            stmt.setInt(7, deviceTypeId);
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug(
                        "Details of DeviceTypeSensor [" + deviceTypeSensor.getName() + "] of " +
                                "DeviceType with Id [" + deviceTypeId + "] was updated successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new DeviceTypeSensor [" + deviceTypeSensor.getName() +
                    "] for the DeviceType with Id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred whilst trying to get the byte streams of the " +
                    "'sensorType' Object and 'staticProperties' HashMap of the " +
                    "DeviceTypeSensor [" + deviceTypeSensor.getName() + "] to store as BLOBs in the DB";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<DeviceTypeSensor> getDeviceTypeSensors(int deviceTypeId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceTypeSensor> deviceTypeSensors = new ArrayList<>();

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT " +
                            "SENSOR_ID AS SENSOR_ID, " +
                            "SESNOR_NAME AS SESNOR_NAME " +
                            "DESCRIPTION AS DESCRIPTION " +
                            "SENSOR_TYPE AS SENSOR_TYPE " +
                            "STATIC_PROPERTIES AS STATIC_PROPERTIES " +
                            "STREAM_DEFINITION AS DESTREAM_DEFINITIONVICE_TYPE_ID " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                SensorType sensorType = null;
                Map<String, Object> staticProperties = null;
                String deviceTypeSensorName = resultSet.getString(DeviceTypeSensor.DAOConstants.SENSOR_NAME);

                try {
                    // Read the BLOB of SensorType Object from DB as bytes.
                    sensorType = (SensorType) deSerializeBlobData(
                            resultSet.getObject(DeviceTypeSensor.DAOConstants.SENSOR_TYPE), SensorType.class);
                    // Read the BLOB of Static-Properties Map from DB as bytes.
                    staticProperties = (Map<String, Object>) deSerializeBlobData(
                            resultSet.getObject(DeviceTypeSensor.DAOConstants.STATIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    String msg = "An error occurred whilst trying to cast the BLOB data of DeviceTypeSensor " +
                            "[" + deviceTypeSensorName + "] of device-type with id [" + deviceTypeId + "]";
                    log.error(msg, e);
                    throw new DeviceManagementDAOException(msg, e);
                }

                DeviceTypeSensor deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setTypeId(resultSet.getInt(DeviceTypeSensor.DAOConstants.SENSOR_ID));
                deviceTypeSensor.setName(deviceTypeSensorName);
                deviceTypeSensor.setDescription(resultSet.getString(DeviceTypeSensor.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorType(sensorType);
                deviceTypeSensor.setStaticProperties(staticProperties);
                deviceTypeSensor.setStreamDefinition(
                        resultSet.getString(DeviceTypeSensor.DAOConstants.STREAM_DEFINITION));
                deviceTypeSensors.add(deviceTypeSensor);
            }
            return deviceTypeSensors;
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
    public DeviceTypeSensor getDeviceTypeSensor(int deviceTypeId, String sensorName)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        DeviceTypeSensor deviceTypeSensor = null;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT " +
                            "SENSOR_ID AS SENSOR_ID, " +
                            "DESCRIPTION AS DESCRIPTION " +
                            "SENSOR_TYPE AS SENSOR_TYPE " +
                            "STATIC_PROPERTIES AS STATIC_PROPERTIES " +
                            "STREAM_DEFINITION AS DESTREAM_DEFINITIONVICE_TYPE_ID " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ? AND SENSOR_NAME = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setString(2, sensorName);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                SensorType sensorType;
                Map<String, Object> staticProperties;

                try {
                    // Read the BLOB of SensorType Object from DB as bytes.
                    sensorType = (SensorType) deSerializeBlobData(
                            resultSet.getObject(DeviceTypeSensor.DAOConstants.SENSOR_TYPE), SensorType.class);
                    // Read the BLOB of Static-Properties Map from DB as bytes.
                    staticProperties = (Map<String, Object>) deSerializeBlobData(
                            resultSet.getObject(DeviceTypeSensor.DAOConstants.STATIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    String msg = "An error occurred whilst trying to cast the BLOB data of DeviceTypeSensor " +
                            "[" + sensorName + "] of device-type with id [" + deviceTypeId + "]";
                    log.error(msg, e);
                    throw new DeviceManagementDAOException(msg, e);
                }

                deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setTypeId(resultSet.getInt(DeviceTypeSensor.DAOConstants.SENSOR_ID));
                deviceTypeSensor.setName(sensorName);
                deviceTypeSensor.setDescription(resultSet.getString(DeviceTypeSensor.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorType(sensorType);
                deviceTypeSensor.setStaticProperties(staticProperties);
                deviceTypeSensor.setStreamDefinition(
                        resultSet.getString(DeviceTypeSensor.DAOConstants.STREAM_DEFINITION));
            }

            return deviceTypeSensor;
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
    public DeviceTypeSensor getDeviceTypeSensor(int deviceTypeId, int sensorId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        DeviceTypeSensor deviceTypeSensor = null;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT " +
                            "SENSOR_NAME AS SENSOR_NAME, " +
                            "DESCRIPTION AS DESCRIPTION " +
                            "SENSOR_TYPE AS SENSOR_TYPE " +
                            "STATIC_PROPERTIES AS STATIC_PROPERTIES " +
                            "STREAM_DEFINITION AS DESTREAM_DEFINITIONVICE_TYPE_ID " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ? AND SENSOR_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setInt(2, sensorId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                SensorType sensorType;
                Map<String, Object> staticProperties;
                String sensorName = resultSet.getString(DeviceTypeSensor.DAOConstants.SENSOR_NAME);

                try {
                    // Read the BLOB of SensorType Object from DB as bytes.
                    sensorType = (SensorType) deSerializeBlobData(
                            resultSet.getObject(DeviceTypeSensor.DAOConstants.SENSOR_TYPE), SensorType.class);
                    // Read the BLOB of Static-Properties Map from DB as bytes.
                    staticProperties = (Map<String, Object>) deSerializeBlobData(
                            resultSet.getObject(DeviceTypeSensor.DAOConstants.STATIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    throw new DeviceManagementDAOException(
                            "An error occurred whilst trying to cast the BLOB data of DeviceTypeSensor " +
                                    "[" + sensorName + "] of device-type with id [" + deviceTypeId + "]", e);
                }

                deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setTypeId(sensorId);
                deviceTypeSensor.setName(sensorName);
                deviceTypeSensor.setDescription(resultSet.getString(DeviceTypeSensor.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorType(sensorType);
                deviceTypeSensor.setStaticProperties(staticProperties);
                deviceTypeSensor.setStreamDefinition(
                        resultSet.getString(DeviceTypeSensor.DAOConstants.STREAM_DEFINITION));
            }

            return deviceTypeSensor;
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the DeviceTypeSensor with ID [" + sensorId + "] " +
                    "of the device-type with id [" + deviceTypeId + "]";
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
        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM DM_DEVICE_TYPE_SENSOR WHERE DEVICE_ID = ? AND SENSOR_NAME = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setString(2, sensorName);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("DeviceTypeSensor [" + sensorName + "] of device-type with Id [" + deviceTypeId + "] " +
                                      "has been deleted successfully.");
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
    public void removeDeviceTypeSensor(int deviceTypeId, int sensorId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM DM_DEVICE_TYPE_SENSOR WHERE DEVICE_ID = ? AND SENSOR_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setInt(2, sensorId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("DeviceTypeSensor with Id [" + sensorId + "] of device-type with " +
                                      "Id [" + deviceTypeId + "] has been deleted successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the DeviceTypeSensor with Id [" + sensorId + "] " +
                    "of the device with Id [" + deviceTypeId + "].";
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
        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM DM_DEVICE_TYPE_SENSOR WHERE DEVICE_ID = ?";
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
     * @param blobObjectFromDB
     * @param blobClass
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object deSerializeBlobData(Object blobObjectFromDB, Class blobClass)
            throws IOException, ClassNotFoundException {
        Object dataObjectFromDB = null;

        byte[] blobByteArray = (byte[]) blobObjectFromDB;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(blobByteArray);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        if (blobClass.isInstance(objectInputStream.readObject())) {
            dataObjectFromDB = objectInputStream.readObject();
        }
        return dataObjectFromDB;
    }
}
