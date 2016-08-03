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
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceTypeSensorTransactionObject;
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
    public void addDeviceTypeSensor(DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        int deviceTypeId = deviceTypeSensorTransactionObject.getDeviceTypeId();
        DeviceTypeSensor deviceTypeSensor = deviceTypeSensorTransactionObject.getDeviceTypeSensor();
        String deviceTypeSensorName = deviceTypeSensor.getUniqueSensorName();

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
            stmt.setString(1, deviceTypeSensorName);
            stmt.setInt(2, deviceTypeId);
            stmt.setString(3, deviceTypeSensor.getDescription());
            stmt.setBinaryStream(4, sensorTypeByteStream, sensorTypeAsBytes.length);
            stmt.setBinaryStream(5, sensorPropertiesByteStream, sensorPropertiesAsBytes.length);
            stmt.setString(6, deviceTypeSensor.getStreamDefinition());
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug(
                        "Details of DeviceTypeSensor [" + deviceTypeSensorName + "] of " +
                                "DeviceType with Id [" + deviceTypeId + "] was added successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new DeviceTypeSensor [" + deviceTypeSensorName +
                    "] for the DeviceType with Id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred whilst trying to get the byte streams of the " +
                    "'sensorType' Object and 'staticProperties' HashMap of the " +
                    "DeviceTypeSensor [" + deviceTypeSensorName + "] to store as BLOBs in the DB";
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
                            "WHERE SENSOR_ID = ?";

            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, deviceTypeSensorName);
            stmt.setString(2, deviceTypeSensor.getDescription());
            stmt.setBinaryStream(3, sensorTypeByteStream, sensorTypeAsBytes.length);
            stmt.setBinaryStream(4, sensorPropertiesByteStream, sensorPropertiesAsBytes.length);
            stmt.setString(5, deviceTypeSensor.getStreamDefinition());
            stmt.setInt(6, deviceTypeSensorId);
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug(
                        "Details of DeviceTypeSensor [" + deviceTypeSensorName + "] of " +
                                "DeviceType with Id [" + deviceTypeId + "] was updated successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new DeviceTypeSensor [" + deviceTypeSensorName +
                    "] for the DeviceType with Id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred whilst trying to get the byte streams of the " +
                    "'sensorType' Object and 'staticProperties' HashMap of the " +
                    "DeviceTypeSensor [" + deviceTypeSensorName + "] to store as BLOBs in the DB";
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

        String deviceTypeSensorName = deviceTypeSensor.getUniqueSensorName();
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
                            "DESCRIPTION = ?," +
                            "SENSOR_TYPE = ?," +
                            "STATIC_PROPERTIES = ?," +
                            "STREAM_DEFINITION = ? " +
                            "WHERE DEVICE_TYPE_ID = ? AND SENSOR_NAME = ?";

            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, deviceTypeSensor.getDescription());
            stmt.setBinaryStream(2, sensorTypeByteStream, sensorTypeAsBytes.length);
            stmt.setBinaryStream(3, sensorPropertiesByteStream, sensorPropertiesAsBytes.length);
            stmt.setString(4, deviceTypeSensor.getStreamDefinition());
            stmt.setInt(5, deviceTypeId);
            stmt.setString(6, deviceTypeSensorName);
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug(
                        "Details of DeviceTypeSensor [" + deviceTypeSensorName + "] of " +
                                "DeviceType with Id [" + deviceTypeId + "] was updated successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new DeviceTypeSensor [" + deviceTypeSensorName +
                    "] for the DeviceType with Id [" + deviceTypeId + "]";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred whilst trying to get the byte streams of the " +
                    "'sensorType' Object and 'staticProperties' HashMap of the " +
                    "DeviceTypeSensor [" + deviceTypeSensorName + "] to store as BLOBs in the DB";
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
                    "SELECT " +
                            "SENSOR_ID AS SENSOR_ID, " +
                            "SENSOR_NAME AS SENSOR_NAME, " +
                            "DESCRIPTION AS DESCRIPTION, " +
                            "SENSOR_TYPE AS SENSOR_TYPE, " +
                            "STATIC_PROPERTIES AS STATIC_PROPERTIES, " +
                            "STREAM_DEFINITION AS STREAM_DEFINITION " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                SensorType sensorType = null;
                Map<String, Object> staticProperties = null;
                String deviceTypeSensorName = resultSet.getString(
                        DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_NAME);
                int deviceTypeSensorId = resultSet.getInt(DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_ID);

                try {
                    // Read the BLOB of SensorType Object from DB as bytes.
                    sensorType = (SensorType) deSerializeBlobData(resultSet.getObject(
                            DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_TYPE), SensorType.class);
                    // Read the BLOB of Static-Properties Map from DB as bytes.
                    staticProperties = (Map<String, Object>) deSerializeBlobData(resultSet.getObject(
                            DeviceTypeSensorTransactionObject.DAOConstants.STATIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    String msg = "An error occurred whilst trying to cast the BLOB data of DeviceTypeSensor " +
                            "[" + deviceTypeSensorName + "] of device-type with id [" + deviceTypeId + "]";
                    log.error(msg, e);
                    throw new DeviceManagementDAOException(msg, e);
                }

                DeviceTypeSensor deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setUniqueSensorName(deviceTypeSensorName);
                deviceTypeSensor.setDescription(resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorType(sensorType);
                deviceTypeSensor.setStaticProperties(staticProperties);
                deviceTypeSensor.setStreamDefinition(
                        resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.STREAM_DEFINITION));

                DeviceTypeSensorTransactionObject deviceTypeSensorTransactionObject = new DeviceTypeSensorTransactionObject(deviceTypeSensor);
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
                    "SELECT " +
                            "SENSOR_ID AS SENSOR_ID, " +
                            "DESCRIPTION AS DESCRIPTION, " +
                            "SENSOR_TYPE AS SENSOR_TYPE, " +
                            "STATIC_PROPERTIES AS STATIC_PROPERTIES, " +
                            "STREAM_DEFINITION AS STREAM_DEFINITION " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE DEVICE_TYPE_ID = ? AND SENSOR_NAME = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeId);
            stmt.setString(2, sensorName);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                SensorType sensorType;
                Map<String, Object> staticProperties;
                int deviceTypeSensorId = resultSet.getInt(DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_ID);

                try {
                    // Read the BLOB of SensorType Object from DB as bytes.
                    sensorType = (SensorType) deSerializeBlobData(resultSet.getObject(
                            DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_TYPE), SensorType.class);
                    // Read the BLOB of Static-Properties Map from DB as bytes.
                    staticProperties = (Map<String, Object>) deSerializeBlobData(resultSet.getObject(
                            DeviceTypeSensorTransactionObject.DAOConstants.STATIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    String msg = "An error occurred whilst trying to cast the BLOB data of DeviceTypeSensor " +
                            "[" + sensorName + "] of device-type with id [" + deviceTypeId + "]";
                    log.error(msg, e);
                    throw new DeviceManagementDAOException(msg, e);
                }

                DeviceTypeSensor deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setUniqueSensorName(sensorName);
                deviceTypeSensor.setDescription(
                        resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorType(sensorType);
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
                    "SELECT " +
                            "SENSOR_NAME AS SENSOR_NAME, " +
                            "DEVICE_TYPE_ID AS DEVICE_TYPE_ID, " +
                            "DESCRIPTION AS DESCRIPTION, " +
                            "SENSOR_TYPE AS SENSOR_TYPE, " +
                            "STATIC_PROPERTIES AS STATIC_PROPERTIES, " +
                            "STREAM_DEFINITION AS STREAM_DEFINITION " +
                            "FROM DM_DEVICE_TYPE_SENSOR " +
                            "WHERE SENSOR_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setInt(1, deviceTypeSensorId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                SensorType sensorType;
                Map<String, Object> staticProperties;
                String sensorName = resultSet.getString(DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_NAME);
                int deviceTypeId = resultSet.getInt(DeviceTypeSensorTransactionObject.DAOConstants.DEVICE_TYPE_ID);

                try {
                    // Read the BLOB of SensorType Object from DB as bytes.
                    sensorType = (SensorType) deSerializeBlobData(resultSet.getObject(
                            DeviceTypeSensorTransactionObject.DAOConstants.SENSOR_TYPE), SensorType.class);
                    // Read the BLOB of Static-Properties Map from DB as bytes.
                    staticProperties = (Map<String, Object>) deSerializeBlobData(resultSet.getObject(
                            DeviceTypeSensorTransactionObject.DAOConstants.STATIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    throw new DeviceManagementDAOException(
                            "An error occurred whilst trying to cast the BLOB data of DeviceTypeSensor " +
                                    "[" + sensorName + "] of with id [" + deviceTypeSensorId + "]", e);
                }

                DeviceTypeSensor deviceTypeSensor = new DeviceTypeSensor();
                deviceTypeSensor.setUniqueSensorName(sensorName);
                deviceTypeSensor.setDescription(resultSet.getString(
                        DeviceTypeSensorTransactionObject.DAOConstants.DESCRIPTION));
                deviceTypeSensor.setSensorType(sensorType);
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
