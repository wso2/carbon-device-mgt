/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.core.operation.mgt.util.DeviceIDHolder;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;


public final class DeviceManagerUtil {

    private static final Log log = LogFactory.getLog(DeviceManagerUtil.class);

    public static Document convertToDocument(File file) throws DeviceManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new DeviceManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    /**
     * Resolve data source from the data source definition.
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException("Device Management Repository data source configuration is null and thus, " +
                    "is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    /**
     * Adds a new device type to the database if it does not exists.
     *
     * @param typeName device type
     * @param tenantId provider tenant Id
     * @param isSharedWithAllTenants is this device type shared with all tenants.
     * @return status of the operation
     */
    public static boolean registerDeviceType(String typeName, int tenantId, boolean isSharedWithAllTenants)
            throws DeviceManagementException {
        boolean status;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            DeviceType deviceType = deviceTypeDAO.getDeviceType(typeName, tenantId);
            if (deviceType == null) {
                deviceType = new DeviceType();
                deviceType.setName(typeName);
                deviceTypeDAO.addDeviceType(deviceType, tenantId, isSharedWithAllTenants);
            }
            DeviceManagementDAOFactory.commitTransaction();
            status = true;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while registering the device type '"
                                                        + typeName + "'", e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("SQL occurred while registering the device type '"
                                                        + typeName + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return status;
    }

    /**
     * Un-registers an existing device type from the device management metadata repository.
     *
     * @param typeName device type
     * @return status of the operation
     */
    public static boolean unregisterDeviceType(String typeName, int tenantId) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            DeviceType deviceType = deviceTypeDAO.getDeviceType(typeName, tenantId);
            if (deviceType != null) {
                deviceTypeDAO.removeDeviceType(typeName, tenantId);
            }
            DeviceManagementDAOFactory.commitTransaction();
            return true;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while registering the device type '" +
                                                        typeName + "'", e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("SQL occurred while registering the device type '" +
                                                        typeName + "'", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    public static Map<String, String> convertDevicePropertiesToMap(List<Device.Property> properties) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        for (Device.Property prop : properties) {
            propertiesMap.put(prop.getName(), prop.getValue());
        }
        return propertiesMap;
    }

    public static List<DeviceIdentifier> convertDevices(List<Device> devices) {

        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(device.getDeviceIdentifier());
            identifier.setType(device.getType());
            deviceIdentifiers.add(identifier);
        }
        return deviceIdentifiers;
    }

    public static List<DeviceIdentifier> getValidDeviceIdentifiers(List<Device> devices) {
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            if (device.getEnrolmentInfo() != null) {
                switch (device.getEnrolmentInfo().getStatus()) {
                    case BLOCKED:
                    case REMOVED:
                    case SUSPENDED:
                        break;
                    default:
                        DeviceIdentifier identifier = new DeviceIdentifier();
                        identifier.setId(device.getDeviceIdentifier());
                        identifier.setType(device.getType());
                        deviceIdentifiers.add(identifier);
                }
            }
        }
        return deviceIdentifiers;
    }


    public static String getServerBaseHttpsUrl() {
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        String mgtConsoleTransport = CarbonUtils.getManagementTransport();
        ConfigurationContextService configContextService =
                DeviceManagementDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
        int httpsProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        mgtConsoleTransport);
        if (httpsProxyPort > 0) {
            port = httpsProxyPort;
        }
        return "https://" + hostName + ":" + port;
    }

    public static String getServerBaseHttpUrl() {
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        ConfigurationContextService configContextService =
                DeviceManagementDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, "http");
        int httpProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        "http");
        if (httpProxyPort > 0) {
            port = httpProxyPort;
        }
        return "http://" + hostName + ":" + port;
    }

    /**
     * returns the tenant Id of the specific tenant Domain
     *
     * @param tenantDomain
     * @return
     * @throws DeviceManagementException
     */
    public static int getTenantId(String tenantDomain) throws DeviceManagementException {
        try {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                return MultitenantConstants.SUPER_TENANT_ID;
            }
            TenantManager tenantManager = DeviceManagementDataHolder.getInstance().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            if (tenantId == -1) {
                throw new DeviceManagementException("invalid tenant Domain :" + tenantDomain);
            }
            return tenantId;
        } catch (UserStoreException e) {
            throw new DeviceManagementException("invalid tenant Domain :" + tenantDomain);
        }
    }

    public static int validateActivityListPageSize(int limit) throws OperationManagementException {
        if (limit == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                return deviceManagementConfig.getPaginationConfiguration().getActivityListPageSize();
            } else {
                throw new OperationManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                    "cdm-config.xml file.");
            }
        }
        return limit;
    }

    public static PaginationRequest validateOperationListPageSize(PaginationRequest paginationRequest) throws
                                                                                          OperationManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getOperationListPageSize());
            } else {
                throw new OperationManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                    "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static PaginationRequest validateNotificationListPageSize(PaginationRequest paginationRequest) throws
                                                                                       NotificationManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getNotificationListPageSize());
            } else {
                throw new NotificationManagementException("Device-Mgt configuration has not initialized. Please check the " +
                          "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static PaginationRequest validateDeviceListPageSize(PaginationRequest paginationRequest) throws
                                                                                             DeviceManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration().
                        getDeviceListPageSize());
            } else {
                throw new DeviceManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                    "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static GroupPaginationRequest validateGroupListPageSize(GroupPaginationRequest paginationRequest) throws
                                                                                                    GroupManagementException {
        if (paginationRequest.getRowCount() == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance()
                    .getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                paginationRequest.setRowCount(deviceManagementConfig.getPaginationConfiguration()
                                                      .getDeviceListPageSize());
            } else {
                throw new GroupManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                   "cdm-config.xml file.");
            }
        }
        return paginationRequest;
    }

    public static int validateDeviceListPageSize(int limit) throws DeviceManagementException {
        if (limit == 0) {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig();
            if (deviceManagementConfig != null) {
                return deviceManagementConfig.getPaginationConfiguration().getDeviceListPageSize();
            } else {
                throw new DeviceManagementException("Device-Mgt configuration has not initialized. Please check the " +
                                                    "cdm-config.xml file.");
            }
        }
        return limit;
    }

    public static DeviceIDHolder validateDeviceIdentifiers(List<DeviceIdentifier> deviceIDs) {

        List<String> errorDeviceIdList = new ArrayList<String>();
        List<DeviceIdentifier> validDeviceIDList = new ArrayList<DeviceIdentifier>();

        int deviceIDCounter = 0;
        for (DeviceIdentifier deviceIdentifier : deviceIDs) {

            deviceIDCounter++;
            String deviceID = deviceIdentifier.getId();

            if (deviceID == null || deviceID.isEmpty()) {
                errorDeviceIdList.add(String.format(OperationMgtConstants.DeviceConstants.DEVICE_ID_NOT_FOUND,
                        deviceIDCounter));
                continue;
            }

            try {

                if (isValidDeviceIdentifier(deviceIdentifier)) {
                    validDeviceIDList.add(deviceIdentifier);
                } else {
                    errorDeviceIdList.add(deviceID);
                }
            } catch (DeviceManagementException e) {
                errorDeviceIdList.add(deviceID);
            }
        }

        DeviceIDHolder deviceIDHolder = new DeviceIDHolder();
        deviceIDHolder.setValidDeviceIDList(validDeviceIDList);
        deviceIDHolder.setErrorDeviceIdList(errorDeviceIdList);

        return deviceIDHolder;
    }

    public static boolean isValidDeviceIdentifier(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier);
        if (device == null || device.getDeviceIdentifier() == null ||
                device.getDeviceIdentifier().isEmpty() || device.getEnrolmentInfo() == null) {
            return false;
        } else if (EnrolmentInfo.Status.REMOVED.equals(device.getEnrolmentInfo().getStatus())) {
            return false;
        }
        return true;
    }
}
