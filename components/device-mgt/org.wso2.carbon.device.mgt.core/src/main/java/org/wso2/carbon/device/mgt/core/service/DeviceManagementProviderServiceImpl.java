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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.DeviceTypeIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementPluginRepository;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.EnrollmentDAO;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.internal.PluginInitializationListener;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerRepository;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.email.sender.core.ContentProviderInfo;
import org.wso2.carbon.email.sender.core.EmailContext;
import org.wso2.carbon.email.sender.core.EmailSendingFailedException;
import org.wso2.carbon.email.sender.core.TypedValue;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceManagementProviderServiceImpl implements DeviceManagementProviderService,
        PluginInitializationListener {

    private static Log log = LogFactory.getLog(DeviceManagementProviderServiceImpl.class);
    private DeviceDAO deviceDAO;
    private DeviceTypeDAO deviceTypeDAO;
    private EnrollmentDAO enrollmentDAO;
    private DeviceManagementPluginRepository pluginRepository;
    private OperationManagerRepository operationManagerRepository;

    public DeviceManagementProviderServiceImpl() {
        this.pluginRepository = new DeviceManagementPluginRepository();
        this.operationManagerRepository = new OperationManagerRepository();
        initDataAccessObjects();
        /* Registering a listener to retrieve events when some device management service plugin is installed after
        * the component is done getting initialized */
        DeviceManagementServiceComponent.registerPluginInitializationListener(this);
    }

    private void initDataAccessObjects() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.enrollmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();
    }

    @Override
    public boolean saveConfiguration(TenantConfiguration configuration) throws DeviceManagementException {
        DeviceManager dms =
                pluginRepository.getDeviceManagementService(configuration.getType(),
                        this.getTenantId()).getDeviceManager();
        return dms.saveConfiguration(configuration);
    }

    @Override
    public TenantConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public TenantConfiguration getConfiguration(String deviceType) throws DeviceManagementException {
        DeviceManager dms =
                pluginRepository.getDeviceManagementService(deviceType, this.getTenantId()).getDeviceManager();
        if (dms == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device type '" + deviceType + "' does not have an associated device management " +
                        "plugin registered within the framework. Therefore, not attempting getConfiguration method");
            }
            return null;
        }
        return dms.getConfiguration();
    }

    @Override
    public FeatureManager getFeatureManager(String deviceType) {
        DeviceManager deviceManager = this.getDeviceManager(deviceType);
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceType + "' is null. " +
                        "Therefore, not attempting method 'getFeatureManager'");
            }
            return null;
        }
        return deviceManager.getFeatureManager();
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status = false;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());

        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'enrollDevice'");
            }
            return false;
        }
        deviceManager.enrollDevice(device);

        if (deviceManager.isClaimable(deviceIdentifier)) {
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.INACTIVE);
        } else {
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.ACTIVE);
        }
        int tenantId = this.getTenantId();

        Device existingDevice = this.getDevice(deviceIdentifier);

        if (existingDevice != null) {
            EnrolmentInfo existingEnrolmentInfo = existingDevice.getEnrolmentInfo();
            EnrolmentInfo newEnrolmentInfo = device.getEnrolmentInfo();
            if (existingEnrolmentInfo != null && newEnrolmentInfo != null) {
                //Get all the enrollments of current user for the same device
                List<EnrolmentInfo> enrolmentInfos = this.getEnrollmentsOfUser(existingDevice.getId(),
                        newEnrolmentInfo.getOwner());
                for (EnrolmentInfo enrolmentInfo : enrolmentInfos) {
                    //If the enrollments are same then we'll update the existing enrollment.
                    if (enrolmentInfo.equals(newEnrolmentInfo)) {
                        device.setId(existingDevice.getId());
                        device.getEnrolmentInfo().setDateOfEnrolment(enrolmentInfo.getDateOfEnrolment());
                        device.getEnrolmentInfo().setId(enrolmentInfo.getId());
                        this.modifyEnrollment(device);
                        status = true;
                        break;
                    }
                }
                if (!status) {
                    int enrolmentId, updateStatus = 0;
                    try {
                        //Remove the existing enrollment
                        DeviceManagementDAOFactory.beginTransaction();
                        if (!EnrolmentInfo.Status.REMOVED.equals(existingEnrolmentInfo.getStatus())) {
                            existingEnrolmentInfo.setStatus(EnrolmentInfo.Status.REMOVED);
                            updateStatus = enrollmentDAO.updateEnrollment(existingEnrolmentInfo);
                        }
                        if ((updateStatus > 0) || EnrolmentInfo.Status.REMOVED.
                                equals(existingEnrolmentInfo.getStatus())) {
                            enrolmentId = enrollmentDAO.
                                    addEnrollment(existingDevice.getId(), newEnrolmentInfo, tenantId);
                            DeviceManagementDAOFactory.commitTransaction();
                            if (log.isDebugEnabled()) {
                                log.debug("An enrolment is successfully added with the id '" + enrolmentId +
                                        "' associated with " + "the device identified by key '" +
                                        device.getDeviceIdentifier() + "', which belongs to " + "platform '" +
                                        device.getType() + " upon the user '" + device.getEnrolmentInfo().getOwner() +
                                        "'");
                            }
                            status = true;
                        } else {
                            log.warn("Unable to update device enrollment for device : " + device.getDeviceIdentifier() +
                                    " belonging to user : " + device.getEnrolmentInfo().getOwner());
                        }
                    } catch (DeviceManagementDAOException e) {
                        DeviceManagementDAOFactory.rollbackTransaction();
                        throw new DeviceManagementException("Error occurred while adding enrolment related metadata", e);
                    } catch (TransactionManagementException e) {
                        throw new DeviceManagementException("Error occurred while initiating transaction", e);
                    } finally {
                        DeviceManagementDAOFactory.closeConnection();
                    }
                }
            }
        } else {
            int enrolmentId = 0;
            try {
                DeviceManagementDAOFactory.beginTransaction();
                DeviceType type = deviceTypeDAO.getDeviceType(device.getType(), tenantId);
                int deviceId = deviceDAO.addDevice(type.getId(), device, tenantId);
                enrolmentId = enrollmentDAO.addEnrollment(deviceId, device.getEnrolmentInfo(), tenantId);
                DeviceManagementDAOFactory.commitTransaction();
            } catch (DeviceManagementDAOException e) {
                DeviceManagementDAOFactory.rollbackTransaction();
                throw new DeviceManagementException("Error occurred while adding metadata of '" + device.getType() +
                        "' device carrying the identifier '" + device.getDeviceIdentifier() + "'", e);
            } catch (TransactionManagementException e) {
                throw new DeviceManagementException("Error occurred while initiating transaction", e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }

            if (log.isDebugEnabled()) {
                log.debug("An enrolment is successfully created with the id '" + enrolmentId + "' associated with " +
                        "the device identified by key '" + device.getDeviceIdentifier() + "', which belongs to " +
                        "platform '" + device.getType() + " upon the user '" +
                        device.getEnrolmentInfo().getOwner() + "'");
            }
            status = true;
        }

        return status;
    }


    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'modifyEnrolment'");
            }
            return false;
        }
        boolean status = deviceManager.modifyEnrollment(device);
        try {
            int tenantId = this.getTenantId();
            DeviceManagementDAOFactory.beginTransaction();

            DeviceType type = deviceTypeDAO.getDeviceType(device.getType(), tenantId);
            Device currentDevice = deviceDAO.getDevice(deviceIdentifier, tenantId);
            device.setId(currentDevice.getId());
            device.getEnrolmentInfo().setId(currentDevice.getEnrolmentInfo().getId());

            deviceDAO.updateDevice(type.getId(), device, tenantId);
            enrollmentDAO.updateEnrollment(device.getEnrolmentInfo());
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while modifying the device " +
                    "'" + device.getId() + "'", e);
        } catch (TransactionManagementException e) {
            throw new DeviceManagementException("Error occurred while initiating transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return status;
    }

    private List<EnrolmentInfo> getEnrollmentsOfUser(int deviceId, String user)
            throws DeviceManagementException {
        List<EnrolmentInfo> enrolmentInfos = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.openConnection();
            enrolmentInfos = enrollmentDAO.getEnrollmentsOfUser(deviceId, user, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the enrollment information device for" +
                    "id '" + deviceId + "' and user : " + user, e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return enrolmentInfos;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'dis-enrollDevice'");
            }
            return false;
        }
        try {
            int tenantId = this.getTenantId();
            DeviceManagementDAOFactory.beginTransaction();

            Device device = deviceDAO.getDevice(deviceId, tenantId);
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found for id '" + deviceId.getId() + "'");
                }
                return false;
            }

            if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.REMOVED)) {
                if (log.isDebugEnabled()) {
                    log.debug("Device has already disenrolled : " + deviceId.getId() + "'");
                }
                return false;
            }
            DeviceType deviceType = deviceTypeDAO.getDeviceType(device.getType(), tenantId);

            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.REMOVED);
            enrollmentDAO.updateEnrollment(device.getId(), device.getEnrolmentInfo(), tenantId);
            deviceDAO.updateDevice(deviceType.getId(), device, tenantId);

            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while dis-enrolling '" + deviceId.getType() +
                    "' device with the identifier '" + deviceId.getId() + "'", e);
        } catch (TransactionManagementException e) {
            throw new DeviceManagementException("Error occurred while initiating transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceManager.disenrollDevice(deviceId);
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'isEnrolled'");
            }
            return false;
        }
        return deviceManager.isEnrolled(deviceId);
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'isActive'");
            }
            return false;
        }
        return deviceManager.isActive(deviceId);
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'setActive'");
            }
            return false;
        }
        return deviceManager.setActive(deviceId, status);
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving device list pertaining to " +
                    "the current tenant", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        for (Device device : allDevices) {
            DeviceManager deviceManager = this.getDeviceManager(device.getType());
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                            "Therefore, not attempting method 'isEnrolled'");
                }
                devices.add(device);
                continue;
            }
            Device dmsDevice =
                    deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }

    @Override
    public PaginationResult getDevicesByType(PaginationRequest request) throws DeviceManagementException {
        PaginationResult paginationResult = new PaginationResult();
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices = new ArrayList<>();
        int count = 0;
        int tenantId = this.getTenantId();
        String deviceType = request.getDeviceType();
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(request, tenantId);
            count = deviceDAO.getDeviceCountByType(deviceType, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving device list pertaining to " +
                    "the current tenant of type " + deviceType, e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        for (Device device : allDevices) {
            DeviceManager deviceManager = this.getDeviceManager(device.getType());
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                            "Therefore, not attempting method 'isEnrolled'");
                }
                devices.add(device);
                continue;
            }
            Device dmsDevice =
                    deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        paginationResult.setData(devices);
        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    @Override
    public PaginationResult getAllDevices(PaginationRequest request) throws DeviceManagementException {
        PaginationResult paginationResult = new PaginationResult();
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices = new ArrayList<>();
        int count = 0;
        int tenantId = this.getTenantId();
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(request, tenantId);
            count = deviceDAO.getDeviceCount(request, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving device list pertaining to " +
                    "the current tenant", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        for (Device device : allDevices) {
            DeviceManager deviceManager = this.getDeviceManager(device.getType());
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                            "Therefore, not attempting method 'isEnrolled'");
                }
                devices.add(device);
                continue;
            }
            Device dmsDevice =
                    deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        paginationResult.setData(devices);
        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    @Override
    public List<Device> getAllDevices(String deviceType) throws DeviceManagementException {
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(deviceType, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving all devices of type '" +
                    deviceType + "' that are being managed within the scope of current tenant", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        for (Device device : allDevices) {
            DeviceManager deviceManager = this.getDeviceManager(deviceType);
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + deviceType + "' is null. " +
                            "Therefore, not attempting method 'isEnrolled'");
                }
                devices.add(device);
                continue;
            }
            Device dmsDevice =
                    deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }

    @Override
    public void sendEnrolmentInvitation(EmailMetaInfo metaInfo) throws DeviceManagementException {
        Map<String, TypedValue<Class<?>, Object>> params = new HashMap<>();
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.FIRST_NAME,
                new TypedValue<Class<?>, Object>(String.class, metaInfo.getProperty("first-name")));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTPS,
                new TypedValue<Class<?>, Object>(String.class, DeviceManagerUtil.getServerBaseHttpsUrl()));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTP,
                new TypedValue<Class<?>, Object>(String.class, DeviceManagerUtil.getServerBaseHttpUrl()));
        try {
            EmailContext ctx =
                    new EmailContext.EmailContextBuilder(new ContentProviderInfo("user-enrollment", params),
                            metaInfo.getRecipients()).build();
            DeviceManagementDataHolder.getInstance().getEmailSenderService().sendEmail(ctx);
        } catch (EmailSendingFailedException e) {
            throw new DeviceManagementException("Error occurred while sending enrollment invitation", e);
        }
    }

    @Override
    public void sendRegistrationEmail(EmailMetaInfo metaInfo) throws DeviceManagementException {
        Map<String, TypedValue<Class<?>, Object>> params = new HashMap<>();
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.FIRST_NAME,
                new TypedValue<Class<?>, Object>(String.class, metaInfo.getProperty("first-name")));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.USERNAME,
                new TypedValue<Class<?>, Object>(String.class, metaInfo.getProperty("username")));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.PASSWORD,
                new TypedValue<Class<?>, Object>(String.class, metaInfo.getProperty("password")));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.DOMAIN,
                new TypedValue<Class<?>, Object>(String.class, metaInfo.getProperty("domain")));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTPS,
                new TypedValue<Class<?>, Object>(String.class, DeviceManagerUtil.getServerBaseHttpsUrl()));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTP,
                new TypedValue<Class<?>, Object>(String.class, DeviceManagerUtil.getServerBaseHttpUrl()));
        try {
            EmailContext ctx =
                    new EmailContext.EmailContextBuilder(new ContentProviderInfo("user-registration", params),
                            metaInfo.getRecipients()).build();
            DeviceManagementDataHolder.getInstance().getEmailSenderService().sendEmail(ctx);
        } catch (EmailSendingFailedException e) {
            throw new DeviceManagementException("Error occurred while sending user registration notification", e);
        }
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                    "'" + deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (device != null) {
            // The changes made here to prevent unit tests getting failed. They failed because when running the unit
            // tests there is no osgi services. So getDeviceManager() returns a null.
            DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                            "Therefore, not attempting method 'getDevice'");
                }
                return device;
            }
            Device pluginSpecificInfo = deviceManager.getDevice(deviceId);
            if (pluginSpecificInfo != null) {
                device.setFeatures(pluginSpecificInfo.getFeatures());
                device.setProperties(pluginSpecificInfo.getProperties());
            }
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status) throws DeviceManagementException {
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, status, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                    "'" + deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (device != null) {
            // The changes made here to prevent unit tests getting failed. They failed because when running the unit
            // tests there is no osgi services. So getDeviceManager() returns a null.
            DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                            "Therefore, not attempting method 'getDevice'");
                }
                return device;
            }
            Device pluginSpecificInfo = deviceManager.getDevice(deviceId);
            if (pluginSpecificInfo != null) {
                device.setFeatures(pluginSpecificInfo.getFeatures());
                device.setProperties(pluginSpecificInfo.getProperties());
            }
        }
        return device;
    }

    @Override
    public List<DeviceType> getAvailableDeviceTypes() throws DeviceManagementException {
        List<DeviceType> deviceTypesProvidedByTenant;
        List<DeviceType> publicSharedDeviceTypesInDB;
        List<DeviceType> deviceTypesResponse = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = this.getTenantId();
            deviceTypesProvidedByTenant = deviceTypeDAO.getDeviceTypesByProvider(tenantId);
            publicSharedDeviceTypesInDB = deviceTypeDAO.getSharedDeviceTypes();
            Map<DeviceTypeIdentifier, DeviceManagementService> registeredTypes =
                    pluginRepository.getAllDeviceManagementServices(tenantId);
            Set<String> deviceTypeSetForTenant = new HashSet<>();

            if (registeredTypes != null) {
                if (deviceTypesProvidedByTenant != null) {
                    for (DeviceType deviceType : deviceTypesProvidedByTenant) {
                        DeviceTypeIdentifier providerKey = new DeviceTypeIdentifier(deviceType.getName(), tenantId);
                        if (registeredTypes.get(providerKey) != null) {
                            deviceTypesResponse.add(deviceType);
                            deviceTypeSetForTenant.add(deviceType.getName());
                        }
                    }
                }
                // Get the device from the public space, however if there is another device with same name then give
                // priority to that
                if (publicSharedDeviceTypesInDB != null) {
                    for (DeviceType deviceType : publicSharedDeviceTypesInDB) {
                        DeviceTypeIdentifier providerKey = new DeviceTypeIdentifier(deviceType.getName());
                        if (registeredTypes.get(providerKey) != null && !deviceTypeSetForTenant.contains(
                                deviceType.getName())) {
                            deviceTypesResponse.add(deviceType);
                        }
                    }
                }

            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device types.", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceTypesResponse;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceId, Device device) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'updateDeviceInfo'");
            }
            return false;
        }
        return deviceManager.updateDeviceInfo(deviceId, device);
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'setOwnership'");
            }
            return false;
        }
        return deviceManager.setOwnership(deviceId, ownershipType);
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceId.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceId.getType() + "' is null. " +
                        "Therefore, not attempting method 'isClaimable'");
            }
            return false;
        }
        return deviceManager.isClaimable(deviceId);
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = this.getTenantId();
            Device device = deviceDAO.getDevice(deviceId, tenantId);
            boolean success = enrollmentDAO.setStatus(device.getId(), currentOwner, status, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            return success;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while setting enrollment status", e);
        } catch (TransactionManagementException e) {
            throw new DeviceManagementException("Error occurred while initiating transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void notifyOperationToDevices(Operation operation, List<DeviceIdentifier> deviceIds)
            throws DeviceManagementException {

        for (DeviceIdentifier deviceId : deviceIds) {
            DeviceManagementService dms =
                    pluginRepository.getDeviceManagementService(deviceId.getType(), this.getTenantId());
            //TODO FIX THIS WITH PUSH NOTIFICATIONS
            //dms.notifyOperationToDevices(operation, deviceIds);
        }

    }

    @Override
    public License getLicense(String deviceType, String languageCode) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceType);
        License license;
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceType + "' is null. " +
                        "Therefore, not attempting method 'getLicense'");
            }
            return null;
        }
        try {
            license = deviceManager.getLicense(languageCode);
            if (license == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find a license for '" + deviceType + "' device type");
                }
            }
            return license;
        } catch (LicenseManagementException e) {
            throw new DeviceManagementException("Error occurred while retrieving license configured for " +
                    "device type '" + deviceType + "' and language code '" + languageCode + "'", e);
        }
    }

    @Override
    public void addLicense(String deviceType, License license) throws DeviceManagementException {
        DeviceManager deviceManager = this.getDeviceManager(deviceType);
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + deviceType + "' is null. " +
                        "Therefore, not attempting method 'isEnrolled'");
            }
            return;
        }
        try {
            deviceManager.addLicense(license);
        } catch (LicenseManagementException e) {
            throw new DeviceManagementException("Error occurred while adding license for " +
                    "device type '" + deviceType + "'", e);
        }
    }

    @Override
    public Activity addOperation(String type, Operation operation,
                                 List<DeviceIdentifier> devices) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().addOperation(operation, devices);
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperations(deviceId);
    }

    @Override
    public PaginationResult getOperations(DeviceIdentifier deviceId, PaginationRequest request)
            throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperations(deviceId, request);
    }

    @Override
    public List<? extends Operation> getPendingOperations(DeviceIdentifier deviceId)
            throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getPendingOperations(deviceId);
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getNextPendingOperation(deviceId);
    }

    @Override
    public void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException {
        DeviceManagementDataHolder.getInstance().getOperationManager().updateOperation(deviceId, operation);
    }

    @Override
    public void deleteOperation(String type, int operationId) throws OperationManagementException {
        DeviceManagementDataHolder.getInstance().getOperationManager().deleteOperation(operationId);
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId,
                                                        int operationId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationByDeviceAndOperationId(
                deviceId, operationId);
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            DeviceIdentifier deviceId,
            Operation.Status status) throws OperationManagementException, DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationsByDeviceAndStatus(
                deviceId, status);
    }

    @Override
    public Operation getOperation(String type, int operationId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperation(operationId);
    }

    @Override
    public Activity getOperationByActivityId(String activity) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationByActivityId(activity);
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivitiesUpdatedAfter(timestamp);
    }

    @Override
    public List<Device> getDevicesOfUser(String username) throws DeviceManagementException {
        List<Device> devices = new ArrayList<>();
        List<Device> userDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            userDevices = deviceDAO.getDevicesOfUser(username, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the list of devices that " +
                    "belong to the user '" + username + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        for (Device device : userDevices) {
            DeviceManager deviceManager = this.getDeviceManager(device.getType());
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                            "Therefore, not attempting method 'isEnrolled'");
                }
                devices.add(device);
                continue;
            }
            Device dmsDevice =
                    deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }

    @Override
    public PaginationResult getDevicesOfUser(PaginationRequest request)
            throws DeviceManagementException {
        PaginationResult result = new PaginationResult();
        int deviceCount = 0;
        int tenantId = this.getTenantId();
        String username = request.getOwner();
        List<Device> devices = new ArrayList<>();
        List<Device> userDevices = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.openConnection();
            userDevices = deviceDAO.getDevicesOfUser(request, tenantId);
            deviceCount = deviceDAO.getDeviceCountByUser(username, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the list of devices that " +
                    "belong to the user '" + username + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        for (Device device : userDevices) {
            DeviceManager deviceManager = this.getDeviceManager(device.getType());
            if (deviceManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                            "Therefore, not attempting method 'isEnrolled'");
                }
                devices.add(device);
                continue;
            }
            Device dmsDevice =
                    deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        result.setData(devices);
        result.setRecordsTotal(deviceCount);
        result.setRecordsFiltered(deviceCount);
        return result;
    }

    @Override
    public PaginationResult getDevicesByOwnership(PaginationRequest request)
            throws DeviceManagementException {
        PaginationResult result = new PaginationResult();
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices;
        int deviceCount = 0;
        int tenantId = this.getTenantId();
        String ownerShip = request.getOwnership();
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByOwnership(request, tenantId);
            deviceCount = deviceDAO.getDeviceCountByOwnership(ownerShip, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException(
                    "Error occurred while fetching the list of devices that matches to ownership : '" + ownerShip + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        for (Device device : allDevices) {
            Device dmsDevice = this.getDeviceManager(device.getType()).
                    getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        result.setData(devices);
        result.setRecordsTotal(deviceCount);
        result.setRecordsFiltered(deviceCount);
        return result;
    }

    @Override
    public List<Device> getAllDevicesOfRole(String role) throws DeviceManagementException {
        List<Device> devices = new ArrayList<>();
        String[] users;
        int tenantId = this.getTenantId();
        try {
            users = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager().getUserListOfRole(role);
        } catch (UserStoreException e) {
            throw new DeviceManagementException("Error occurred while obtaining the users, who are assigned " +
                    "with the role '" + role + "'", e);
        }

        List<Device> userDevices;
        for (String user : users) {
            userDevices = new ArrayList<>();
            try {
                DeviceManagementDAOFactory.openConnection();
                userDevices = deviceDAO.getDevicesOfUser(user, tenantId);
            } catch (DeviceManagementDAOException | SQLException e) {
                log.error("Error occurred while obtaining the devices of user '" + user + "'", e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
            for (Device device : userDevices) {
                Device dmsDevice = this.getDeviceManager(device.getType()).
                        getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
                if (dmsDevice != null) {
                    device.setFeatures(dmsDevice.getFeatures());
                    device.setProperties(dmsDevice.getProperties());
                }
                devices.add(device);
            }
        }
        return devices;
    }

    @Override
    public int getDeviceCount(String username) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCount(username, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the device count of user '"
                    + username + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getDeviceCount() throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return deviceDAO.getDeviceCount(this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the device count", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Device> getDevicesByName(String deviceName) throws DeviceManagementException {
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByName(deviceName, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while fetching the list of devices that matches to '"
                    + deviceName + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        for (Device device : allDevices) {
            Device dmsDevice = this.getDeviceManager(device.getType()).
                    getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }

    @Override
    public PaginationResult getDevicesByName(PaginationRequest request)
            throws DeviceManagementException {
        PaginationResult result = new PaginationResult();
        int tenantId = this.getTenantId();
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices = new ArrayList<>();
        String deviceName = request.getDeviceName();
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByName(request, tenantId);
            int deviceCount = deviceDAO.getDeviceCountByName(deviceName, tenantId);
            result.setRecordsTotal(deviceCount);
            result.setRecordsFiltered(deviceCount);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while fetching the list of devices that matches to '"
                    + deviceName + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        for (Device device : allDevices) {
            Device dmsDevice = this.getDeviceManager(device.getType()).
                    getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        result.setData(devices);
        return result;
    }

    @Override
    public void updateDeviceEnrolmentInfo(Device device, EnrolmentInfo.Status status) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceType deviceType = deviceTypeDAO.getDeviceType(device.getType(), this.getTenantId());
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.getEnrolmentInfo().setStatus(status);
            deviceDAO.updateDevice(deviceType.getId(), device, this.getTenantId());
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred update device enrolment status : '" +
                    device.getId() + "'", e);
        } catch (TransactionManagementException e) {
            throw new DeviceManagementException("Error occurred while initiating transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void registerDeviceManagementService(DeviceManagementService deviceManagementService) {
        try {
            pluginRepository.addDeviceManagementProvider(deviceManagementService);
            PushNotificationConfig pushNoteConfig = deviceManagementService.getPushNotificationConfig();
            if (pushNoteConfig != null) {
                NotificationStrategy notificationStrategy =
                        DeviceManagementDataHolder.getInstance().getPushNotificationProviderRepository().getProvider(
                                pushNoteConfig.getType()).getNotificationStrategy(pushNoteConfig);
                operationManagerRepository.addOperationManager(
                        deviceManagementService.getType(), new OperationManagerImpl(notificationStrategy));
            } else {
                operationManagerRepository.addOperationManager(
                        deviceManagementService.getType(), new OperationManagerImpl());
            }
        } catch (DeviceManagementException e) {
            log.error("Error occurred while registering device management plugin '" +
                    deviceManagementService.getType() + "'", e);
        }
    }

    @Override
    public void unregisterDeviceManagementService(DeviceManagementService deviceManagementService) {
        try {
            pluginRepository.removeDeviceManagementProvider(deviceManagementService);
            operationManagerRepository.removeOperationManager(deviceManagementService.getType());
        } catch (DeviceManagementException e) {
            log.error("Error occurred while un-registering device management plugin '" +
                    deviceManagementService.getType() + "'", e);
        }
    }

    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status) throws DeviceManagementException {
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByStatus(status, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException(
                    "Error occurred while fetching the list of devices that matches to status: '" + status + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        for (Device device : allDevices) {
            Device dmsDevice = this.getDeviceManager(device.getType()).
                    getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }

    @Override
    public PaginationResult getDevicesByStatus(PaginationRequest request)
            throws DeviceManagementException {
        PaginationResult result = new PaginationResult();
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices = new ArrayList<>();
        int tenantId = this.getTenantId();
        String status = request.getStatus();
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByStatus(request, tenantId);
            int deviceCount = deviceDAO.getDeviceCountByStatus(status, tenantId);
            result.setRecordsTotal(deviceCount);
            result.setRecordsFiltered(deviceCount);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException(
                    "Error occurred while fetching the list of devices that matches to status: '" + status + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        for (Device device : allDevices) {
            Device dmsDevice = this.getDeviceManager(device.getType()).
                    getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        result.setData(devices);
        return result;
    }

    private int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private DeviceManager getDeviceManager(String deviceType) {
        DeviceManagementService deviceManagementService =
                pluginRepository.getDeviceManagementService(deviceType, this.getTenantId());
        if (deviceManagementService == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device type '" + deviceType + "' does not have an associated device management " +
                        "plugin registered within the framework. Therefore, returning null");
            }
            return null;
        }
        return deviceManagementService.getDeviceManager();
    }

}
