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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.DeviceTypeIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.InitialOperationConfig;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.DeviceManagementPluginRepository;
import org.wso2.carbon.device.mgt.core.cache.impl.DeviceCacheManagerImpl;
import org.wso2.carbon.device.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.EnrollmentDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.internal.PluginInitializationListener;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.email.sender.core.ContentProviderInfo;
import org.wso2.carbon.email.sender.core.EmailContext;
import org.wso2.carbon.email.sender.core.EmailSendingFailedException;
import org.wso2.carbon.email.sender.core.TypedValue;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DeviceManagementProviderServiceImpl implements DeviceManagementProviderService,
        PluginInitializationListener {

    private static Log log = LogFactory.getLog(DeviceManagementProviderServiceImpl.class);
    private DeviceDAO deviceDAO;
    private DeviceDetailsDAO deviceInfoDAO;
    private DeviceTypeDAO deviceTypeDAO;
    private EnrollmentDAO enrollmentDAO;
    private ApplicationDAO applicationDAO;
    private DeviceManagementPluginRepository pluginRepository;

    public DeviceManagementProviderServiceImpl() {
        this.pluginRepository = new DeviceManagementPluginRepository();
        initDataAccessObjects();
        /* Registering a listener to retrieve events when some device management service plugin is installed after
        * the component is done getting initialized */
        DeviceManagementServiceComponent.registerPluginInitializationListener(this);
    }

    private void initDataAccessObjects() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceInfoDAO = DeviceManagementDAOFactory.getDeviceDetailsDAO();
        this.applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.enrollmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();
    }

    @Override
    public boolean saveConfiguration(PlatformConfiguration configuration) throws DeviceManagementException {
        DeviceManager dms =
                pluginRepository.getDeviceManagementService(configuration.getType(),
                        this.getTenantId()).getDeviceManager();
        return dms.saveConfiguration(configuration);
    }

    @Override
    public PlatformConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public PlatformConfiguration getConfiguration(String deviceType) throws DeviceManagementException {
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

        Device existingDevice = this.getDevice(deviceIdentifier, false);

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

        if (status) {
            addDeviceToGroups(deviceIdentifier, device.getEnrolmentInfo().getOwnership());
            addInitialOperations(deviceIdentifier, device.getType());

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
            Device currentDevice = this.getDevice(deviceIdentifier, false);
            DeviceManagementDAOFactory.beginTransaction();
            device.setId(currentDevice.getId());
            if (device.getEnrolmentInfo().getId() == 0) {
                device.getEnrolmentInfo().setId(currentDevice.getEnrolmentInfo().getId());
            }
            if (device.getName() == null) {
                device.setName(currentDevice.getName());
            }
            deviceDAO.updateDevice(device, tenantId);
            enrollmentDAO.updateEnrollment(device.getEnrolmentInfo());
            DeviceManagementDAOFactory.commitTransaction();
            this.removeDeviceFromCache(deviceIdentifier);
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

        int tenantId = this.getTenantId();

        Device device = this.getDevice(deviceId, false);
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
            return true;
        }

        try {
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.REMOVED);
            DeviceManagementDAOFactory.beginTransaction();
            enrollmentDAO.updateEnrollment(device.getId(), device.getEnrolmentInfo(), tenantId);
            deviceDAO.updateDevice(device, tenantId);

            DeviceManagementDAOFactory.commitTransaction();
            this.removeDeviceFromCache(deviceId);
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
        Device device = this.getDevice(deviceId, false);
        if (device != null) {
            return true;
        }
        return false;
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
    public List<Device> getAllDevices(String deviceType) throws DeviceManagementException {
        return this.getAllDevices(deviceType, true);
    }

    @Override
    public List<Device> getAllDevices(String deviceType, boolean requireDeviceInfo) throws DeviceManagementException {
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(deviceType, this.getTenantId());
            if (allDevices == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceType + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving all devices of type '" +
                    deviceType + "' that are being managed within the scope of current tenant", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        return this.getAllDevices(true);
    }

    @Override
    public List<Device> getAllDevices(boolean requireDeviceInfo) throws DeviceManagementException {
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

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public List<Device> getDevices(Date since) throws DeviceManagementException {
        return this.getDevices(since, true);
    }

    @Override
    public List<Device> getDevices(Date since, boolean requireDeviceInfo) throws DeviceManagementException {
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevices(since.getTime(), this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving device list pertaining to " +
                    "the current tenant", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public PaginationResult getDevicesByType(PaginationRequest request) throws DeviceManagementException {
        return this.getDevicesByType(request, true);
    }

    @Override
    public PaginationResult getDevicesByType(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException {
        PaginationResult paginationResult = new PaginationResult();
        List<Device> allDevices = new ArrayList<>();
        int count = 0;
        int tenantId = this.getTenantId();
        String deviceType = request.getDeviceType();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
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

        if (requireDeviceInfo) {
            paginationResult.setData(this.getAllDeviceInfo(allDevices));
        } else {
            paginationResult.setData(allDevices);
        }

        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    @Override
    public PaginationResult getAllDevices(PaginationRequest request) throws DeviceManagementException {
        return this.getAllDevices(request, true);
    }

    @Override
    public PaginationResult getAllDevices(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException {
        List<Device> devicesForRoles = null;
        PaginationResult paginationResult = new PaginationResult();
        List<Device> allDevices = new ArrayList<>();
        int count = 0;
        int tenantId = this.getTenantId();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
        if (!StringUtils.isEmpty(request.getOwnerRole())) {
            devicesForRoles = this.getAllDevicesOfRole(request.getOwnerRole(), false);
            if (devicesForRoles != null) {
                count = devicesForRoles.size();
                if (requireDeviceInfo) {
                    paginationResult.setData(getAllDeviceInfo(devicesForRoles));
                }
            }
        } else {
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
            if (requireDeviceInfo) {
                paginationResult.setData(getAllDeviceInfo(allDevices));
            } else {
                paginationResult.setData(allDevices);
            }
        }
        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, boolean requireDeviceInfo) throws DeviceManagementException {
        int tenantId = this.getTenantId();
        Device device = this.getDeviceFromCache(deviceId);
        if (device == null) {
            try {
                DeviceManagementDAOFactory.openConnection();
                device = deviceDAO.getDevice(deviceId, tenantId);
                if (device == null) {
                    String msg = "No device is found upon the type '" + deviceId.getType() + "' and id '" +
                            deviceId.getId() + "'";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    return null;
                }
                this.addDeviceToCache(deviceId, device);
            } catch (DeviceManagementDAOException e) {
                throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                        "'" + deviceId.getId() + "'", e);
            } catch (SQLException e) {
                throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public void sendEnrolmentInvitation(String templateName, EmailMetaInfo metaInfo) throws DeviceManagementException {
        Map<String, TypedValue<Class<?>, Object>> params = new HashMap<>();
        Properties props = metaInfo.getProperties();
        Enumeration e = props.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            params.put(key, new TypedValue<Class<?>, Object>(String.class, props.getProperty(key)));
        }
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTPS,
                new TypedValue<Class<?>, Object>(String.class, DeviceManagerUtil.getServerBaseHttpsUrl()));
        params.put(org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailAttributes.SERVER_BASE_URL_HTTP,
                new TypedValue<Class<?>, Object>(String.class, DeviceManagerUtil.getServerBaseHttpUrl()));
        try {
            EmailContext ctx =
                    new EmailContext.EmailContextBuilder(new ContentProviderInfo(templateName, params),
                            metaInfo.getRecipients()).build();
            DeviceManagementDataHolder.getInstance().getEmailSenderService().sendEmail(ctx);
        } catch (EmailSendingFailedException ex) {
            throw new DeviceManagementException("Error occurred while sending enrollment invitation", ex);
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
                    new EmailContext.EmailContextBuilder(
                            new ContentProviderInfo(
                                    DeviceManagementConstants.EmailAttributes.USER_REGISTRATION_TEMPLATE,
                                    params),
                            metaInfo.getRecipients()).build();
            DeviceManagementDataHolder.getInstance().getEmailSenderService().sendEmail(ctx);
        } catch (EmailSendingFailedException e) {
            throw new DeviceManagementException("Error occurred while sending user registration notification", e);
        }
    }

    @Override
    public HashMap<Integer, Device> getTenantedDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        HashMap<Integer, Device> deviceHashMap;
        try {
            DeviceManagementDAOFactory.openConnection();
            deviceHashMap = deviceDAO.getDevice(deviceIdentifier);
            if (deviceHashMap == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceIdentifier.getType() + "' and id '" +
                            deviceIdentifier.getId() + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                    "'" + deviceIdentifier.getId() + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return deviceHashMap;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return this.getDevice(deviceId, true);
    }

    @Override
    public Device getDeviceWithTypeProperties(DeviceIdentifier deviceId) throws DeviceManagementException {
        Device device = this.getDevice(deviceId, false);

        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'isEnrolled'");
            }
            return device;
        }
        Device dmsDevice =
                deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
        if (dmsDevice != null) {
            device.setFeatures(dmsDevice.getFeatures());
            device.setProperties(dmsDevice.getProperties());
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, Date since) throws DeviceManagementException {
        return this.getDevice(deviceId, since, true);
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, Date since, boolean requireDeviceInfo) throws DeviceManagementException {
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, since, this.getTenantId());
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceId.getType() + "' and id '" +
                            deviceId.getId() + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                    "'" + deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status) throws DeviceManagementException {
        return this.getDevice(deviceId, status, true);
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status, boolean requireDeviceInfo)
            throws DeviceManagementException {
        Device device;
        try {
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, status, this.getTenantId());
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the type '" + deviceId.getType() + "' and id '" +
                            deviceId.getId() + "'");
                }
                return null;
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                    "'" + deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        if (requireDeviceInfo) {
            device = this.getAllDeviceInfo(device);
        }

        return device;
    }

    @Override
    public List<String> getAvailableDeviceTypes() throws DeviceManagementException {
        List<String> deviceTypesProvidedByTenant;
        List<String> publicSharedDeviceTypesInDB;
        List<String> deviceTypesResponse = new ArrayList<>();
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
                    for (String deviceType : deviceTypesProvidedByTenant) {
                        DeviceTypeIdentifier providerKey = new DeviceTypeIdentifier(deviceType, tenantId);
                        if (registeredTypes.get(providerKey) != null) {
                            deviceTypesResponse.add(deviceType);
                            deviceTypeSetForTenant.add(deviceType);
                        }
                    }
                }
                // Get the device from the public space, however if there is another device with same name then give
                // priority to that
                if (publicSharedDeviceTypesInDB != null) {
                    for (String deviceType : publicSharedDeviceTypesInDB) {
                        DeviceTypeIdentifier providerKey = new DeviceTypeIdentifier(deviceType);
                        if (registeredTypes.get(providerKey) != null && !deviceTypeSetForTenant.contains(deviceType)) {
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
            boolean success = false;
            int tenantId = this.getTenantId();
            Device device = this.getDevice(deviceId, false);
            EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
            DeviceManagementDAOFactory.beginTransaction();
            if (enrolmentInfo != null) {
                success = enrollmentDAO.setStatus(enrolmentInfo.getId(), currentOwner, status, tenantId);
            }
            DeviceManagementDAOFactory.commitTransaction();
            this.removeDeviceFromCache(deviceId);
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
                                 List<DeviceIdentifier> devices) throws OperationManagementException, InvalidDeviceException {
        return pluginRepository.getOperationManager(type, this.getTenantId()).addOperation(operation, devices);
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId()).getOperations(deviceId);
    }

    @Override
    public PaginationResult getOperations(DeviceIdentifier deviceId, PaginationRequest request)
            throws OperationManagementException {
        request = DeviceManagerUtil.validateOperationListPageSize(request);
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getOperations(deviceId, request);
    }

    @Override
    public List<? extends Operation> getPendingOperations(DeviceIdentifier deviceId)
            throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getPendingOperations(deviceId);
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getNextPendingOperation(deviceId);
    }

    @Override
    public void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException {
        pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .updateOperation(deviceId, operation);
    }

    @Override
    public void deleteOperation(String type, int operationId) throws OperationManagementException {
        pluginRepository.getOperationManager(type, this.getTenantId()).deleteOperation(operationId);
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId,
                                                        int operationId) throws OperationManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getOperationByDeviceAndOperationId(deviceId, operationId);
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            DeviceIdentifier deviceId,
            Operation.Status status) throws OperationManagementException, DeviceManagementException {
        return pluginRepository.getOperationManager(deviceId.getType(), this.getTenantId())
                .getOperationsByDeviceAndStatus(deviceId, status);
    }

    @Override
    public Operation getOperation(String type, int operationId) throws OperationManagementException {
        return pluginRepository.getOperationManager(type, this.getTenantId()).getOperation(operationId);
    }

    @Override
    public Activity getOperationByActivityId(String activity) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationByActivityId(activity);
    }

    public Activity getOperationByActivityIdAndDevice(String activity, DeviceIdentifier deviceId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationByActivityIdAndDevice(activity, deviceId);
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivitiesUpdatedAfter(timestamp);
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementException {
        limit = DeviceManagerUtil.validateActivityListPageSize(limit);
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivitiesUpdatedAfter(timestamp, limit, offset);
    }

    @Override
    public int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getActivityCountUpdatedAfter(timestamp);
    }

    @Override
    public List<MonitoringOperation> getMonitoringOperationList(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);

        OperationMonitoringTaskConfig operationMonitoringTaskConfig = dms.getOperationMonitoringConfig();
        return operationMonitoringTaskConfig.getMonitoringOperation();
    }

    @Override
    public int getDeviceMonitoringFrequency(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        OperationMonitoringTaskConfig operationMonitoringTaskConfig = dms.getOperationMonitoringConfig();
        return operationMonitoringTaskConfig.getFrequency();
    }

    @Override
    public boolean isDeviceMonitoringEnabled(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        OperationMonitoringTaskConfig operationMonitoringTaskConfig = dms.getOperationMonitoringConfig();
        return operationMonitoringTaskConfig.isEnabled();
    }

    @Override
    public PolicyMonitoringManager getPolicyMonitoringManager(String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementService dms = pluginRepository.getDeviceManagementService(deviceType, tenantId);
        return dms.getPolicyMonitoringManager();
    }

    @Override
    public List<Device> getDevicesOfUser(String username) throws DeviceManagementException {
        return this.getDevicesOfUser(username, true);
    }

    @Override
    public List<Device> getDevicesOfUser(String username, boolean requireDeviceInfo) throws DeviceManagementException {
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

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(userDevices);
        }
        return userDevices;
    }

    @Override
    public List<Device> getDevicesOfUser(String username, String deviceType) throws DeviceManagementException {
        return  this.getDevicesOfUser(username, deviceType, true);
    }

    @Override
    public List<Device> getDevicesOfUser(String username, String deviceType, boolean requireDeviceInfo) throws
            DeviceManagementException {
        List<Device> userDevices;
        try {
            DeviceManagementDAOFactory.openConnection();
            userDevices = deviceDAO.getDevicesOfUser(username, deviceType, this.getTenantId());
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the list of devices that " +
                    "belong to the user '" + username + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(userDevices);
        }
        return userDevices;
    }

    @Override
    public PaginationResult getDevicesOfUser(PaginationRequest request) throws DeviceManagementException {
        return this.getDevicesOfUser(request, true);
    }

    @Override
    public PaginationResult getDevicesOfUser(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        PaginationResult result = new PaginationResult();
        int deviceCount = 0;
        int tenantId = this.getTenantId();
        String username = request.getOwner();
        List<Device> userDevices = new ArrayList<>();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
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

        if (requireDeviceInfo) {
            result.setData(this.getAllDeviceInfo(userDevices));
        } else {
            result.setData(userDevices);
        }

        result.setRecordsTotal(deviceCount);
        result.setRecordsFiltered(deviceCount);
        return result;
    }

    @Override
    public PaginationResult getDevicesByOwnership(PaginationRequest request)
            throws DeviceManagementException {
        return this.getDevicesByOwnership(request, true);
    }

    @Override
    public PaginationResult getDevicesByOwnership(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        PaginationResult result = new PaginationResult();
        List<Device> allDevices;
        int deviceCount = 0;
        int tenantId = this.getTenantId();
        String ownerShip = request.getOwnership();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
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
        if (requireDeviceInfo) {
            result.setData(this.getAllDeviceInfo(allDevices));
        } else {
            result.setData(allDevices);
        }

        result.setRecordsTotal(deviceCount);
        result.setRecordsFiltered(deviceCount);
        return result;
    }

    @Override
    public List<Device> getAllDevicesOfRole(String role) throws DeviceManagementException {
        return this.getAllDevicesOfRole(role, true);
    }

    @Override
    public List<Device> getAllDevicesOfRole(String role, boolean requireDeviceInfo) throws DeviceManagementException {
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
            if (requireDeviceInfo) {
                this.getAllDeviceInfo(userDevices);
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
    public List<Device> getDevicesByNameAndType(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        List<Device> devices = new ArrayList<>();
        List<Device> allDevices;
        int limit = DeviceManagerUtil.validateDeviceListPageSize(request.getRowCount());
        try {
            DeviceManagementDAOFactory.openConnection();
            allDevices = deviceDAO.getDevicesByNameAndType(request.getDeviceName(), request.getDeviceType(),
                    this.getTenantId(), request.getStartIndex(), limit);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while fetching the list of devices that matches to '"
                    + request.getDeviceName() + "'", e);
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public PaginationResult getDevicesByName(PaginationRequest request) throws DeviceManagementException {
        return this.getDevicesByName(request, true);
    }

    @Override
    public PaginationResult getDevicesByName(PaginationRequest request, boolean requireDeviceInfo) throws
            DeviceManagementException {
        PaginationResult result = new PaginationResult();
        int tenantId = this.getTenantId();
        List<Device> allDevices = new ArrayList<>();
        String deviceName = request.getDeviceName();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
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
        if (requireDeviceInfo) {
            result.setData(this.getAllDeviceInfo(allDevices));
        } else {
            result.setData(allDevices);
        }
        return result;
    }

    @Override
    public void updateDeviceEnrolmentInfo(Device device, EnrolmentInfo.Status status) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.getEnrolmentInfo().setStatus(status);
            deviceDAO.updateDevice(device, this.getTenantId());
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
        } catch (DeviceManagementException e) {
            log.error("Error occurred while registering device management plugin '" +
                    deviceManagementService.getType() + "'", e);
        }
    }

    @Override
    public void unregisterDeviceManagementService(DeviceManagementService deviceManagementService) {
        try {
            pluginRepository.removeDeviceManagementProvider(deviceManagementService);
        } catch (DeviceManagementException e) {
            log.error("Error occurred while un-registering device management plugin '" +
                    deviceManagementService.getType() + "'", e);
        }
    }

    @Override
    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status) throws DeviceManagementException {
        return this.getDevicesByStatus(status, true);
    }

    @Override
    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status, boolean requireDeviceInfo) throws
            DeviceManagementException {
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
        if (requireDeviceInfo) {
            return this.getAllDeviceInfo(allDevices);
        }
        return allDevices;
    }

    @Override
    public PaginationResult getDevicesByStatus(PaginationRequest request) throws DeviceManagementException {
        return this.getDevicesByStatus(request, true);
    }

    @Override
    public PaginationResult getDevicesByStatus(PaginationRequest request, boolean requireDeviceInfo)
            throws DeviceManagementException {
        PaginationResult result = new PaginationResult();
        List<Device> allDevices = new ArrayList<>();
        int tenantId = this.getTenantId();
        String status = request.getStatus();
        request = DeviceManagerUtil.validateDeviceListPageSize(request);
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
        if (requireDeviceInfo) {
            result.setData(this.getAllDeviceInfo(allDevices));
        } else {
            result.setData(allDevices);
        }
        return result;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId, String user) throws DeviceManagementException {
        Device device = this.getDevice(deviceId, false);
        if (device != null && device.getEnrolmentInfo() != null && device.getEnrolmentInfo().getOwner().equals(user)) {
            return true;
        }
        return false;
    }

    @Override
    public NotificationStrategy getNotificationStrategyByDeviceType(String deviceType) throws DeviceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        OperationManager operationManager = pluginRepository.getOperationManager(deviceType, tenantId);
        if (operationManager != null) {
            return operationManager.getNotificationStrategy();
        } else {
            throw new DeviceManagementException("Cannot find operation manager for given device type :" + deviceType);
        }
    }

    /**
     * Change device status.
     *
     * @param deviceIdentifier {@link DeviceIdentifier} object
     * @param newStatus        New status of the device
     * @return Whether status is changed or not
     * @throws DeviceManagementException on errors while trying to change device status
     */
    @Override
    public boolean changeDeviceStatus(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status newStatus)
            throws DeviceManagementException {
        boolean isDeviceUpdated = false;
        Device device = getDevice(deviceIdentifier, false);
        int deviceId = device.getId();
        EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
        enrolmentInfo.setStatus(newStatus);
        int tenantId = this.getTenantId();
        switch (newStatus) {
            case ACTIVE:
                isDeviceUpdated = updateEnrollment(deviceId, enrolmentInfo, tenantId);
                break;
            case INACTIVE:
                isDeviceUpdated = updateEnrollment(deviceId, enrolmentInfo, tenantId);
                break;
            case REMOVED:
                isDeviceUpdated = disenrollDevice(deviceIdentifier);
                break;
            default:
                throw new DeviceManagementException("Invalid status retrieved. Status : " + newStatus);
        }
        return isDeviceUpdated;
    }

    private boolean updateEnrollment(int deviceId, EnrolmentInfo enrolmentInfo, int tenantId)
            throws DeviceManagementException {
        boolean isUpdatedEnrollment = false;
        boolean isAutoCommit = true;
        try {
            DeviceManagementDAOFactory.openConnection();
            isAutoCommit = DeviceManagementDAOFactory.getConnection().getAutoCommit();
            DeviceManagementDAOFactory.getConnection().setAutoCommit(true);
            int updatedRows = enrollmentDAO.updateEnrollment(deviceId, enrolmentInfo, tenantId);
            if (updatedRows > 0) {
                isUpdatedEnrollment = true;
            }
        } catch (SQLException e) {
            throw new DeviceManagementException("Error occurred while opening a connection to the data source", e);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while updating the enrollment information device for" +
                    "id '" + deviceId + "' .", e);
        } finally {
            try {
                DeviceManagementDAOFactory.getConnection().setAutoCommit(isAutoCommit);
            } catch (SQLException e) {
                log.error("Exception occurred while setting auto commit.");
            }
            DeviceManagementDAOFactory.closeConnection();
        }
        return isUpdatedEnrollment;
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

    /**
     * Adds the enrolled devices to the default groups based on ownership
     *
     * @param deviceIdentifier of the device.
     * @param ownership        of the device.
     * @throws DeviceManagementException If error occurred in adding the device to the group.
     */
    private void addDeviceToGroups(DeviceIdentifier deviceIdentifier, EnrolmentInfo.OwnerShip ownership)
            throws DeviceManagementException {
        GroupManagementProviderService groupManagementProviderService = new GroupManagementProviderServiceImpl();
        try {
            DeviceGroup defaultGroup = createDefaultGroup(groupManagementProviderService, ownership.toString());
            if (defaultGroup != null) {
                List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                deviceIdentifiers.add(deviceIdentifier);
                groupManagementProviderService.addDevices(defaultGroup.getGroupId(), deviceIdentifiers);
            }
        } catch (DeviceNotFoundException e) {
            throw new DeviceManagementException("Unable to find the device with the id: '" + deviceIdentifier.getId(),
                    e);
        } catch (GroupManagementException e) {
            throw new DeviceManagementException("An error occurred when adding the device to the group.", e);
        }
    }

    private void addInitialOperations(DeviceIdentifier deviceIdentifier, String deviceType) throws DeviceManagementException {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        DeviceManagementService deviceManagementService =
                pluginRepository.getDeviceManagementService(deviceType, this.getTenantId());
        InitialOperationConfig init = deviceManagementService.getInitialOperationConfig();
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        deviceIdentifiers.add(deviceIdentifier);
        if (init != null) {
            List<String> initialOperations = init.getOperations();
            if (initialOperations != null) {
                for (String str : initialOperations) {
                    CommandOperation operation = new CommandOperation();
                    operation.setEnabled(true);
                    operation.setType(Operation.Type.COMMAND);
                    operation.setCode(str);
                    try {
                        deviceManagementProviderService.addOperation(deviceType, operation, deviceIdentifiers);
                    } catch (OperationManagementException e) {
                        throw new DeviceManagementException("Unable to add the operation for the device with the id: '"
                                                                    + deviceIdentifier.getId(), e);
                    } catch (InvalidDeviceException e) {
                        throw new DeviceManagementException("Unable to find the device with the id: '"
                                                                    + deviceIdentifier.getId(), e);
                    }
                }
            }
        }
    }

    /**
     * Checks for the default group existence and create group based on device ownership
     *
     * @param service   {@link GroupManagementProviderService} instance.
     * @param groupName of the group to create.
     * @return Group with details.
     * @throws GroupManagementException
     */
    private DeviceGroup createDefaultGroup(GroupManagementProviderService service, String groupName)
            throws GroupManagementException {
        DeviceGroup defaultGroup = service.getGroup(groupName);
        if (defaultGroup == null) {
            defaultGroup = new DeviceGroup(groupName);
            // Setting system level user (wso2.system.user) as the owner
            defaultGroup.setOwner(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            defaultGroup.setDescription("Default system group for devices with " + groupName + " ownership.");
            try {
                service.createGroup(defaultGroup, DeviceGroupConstants.Roles.DEFAULT_ADMIN_ROLE,
                        DeviceGroupConstants.Permissions.DEFAULT_ADMIN_PERMISSIONS);
            } catch (GroupAlreadyExistException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Default group: " + defaultGroup.getName() + " already exists. Skipping group creation.",
                            e);
                }
            }
            return service.getGroup(groupName);
        } else {
            return defaultGroup;
        }
    }

    /**
     *  Returns all the device-info including location of the given device.
     */
    private DeviceInfo getDeviceInfo(Device device) {
        DeviceInfo info = null;
        try {
            DeviceManagementDAOFactory.openConnection();
            info = deviceInfoDAO.getDeviceInformation(device.getId());
            DeviceLocation location = deviceInfoDAO.getDeviceLocation(device.getId());
            if (info != null) {
                info.setLocation(location);
            }
        } catch (DeviceDetailsMgtDAOException e) {
            log.error("Error occurred while retrieving advance info of '" + device.getType() +
                    "' that carries the id '" + device.getDeviceIdentifier() + "'");
        } catch (SQLException e) {
            log.error("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return info;
    }

    /**
     *  Returns all the installed apps of the given device.
     */
    private List<Application> getInstalledApplications(Device device) {
        List<Application> applications = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.openConnection();
            applications = applicationDAO.getInstalledApplications(device.getId());
            device.setApplications(applications);
        } catch (DeviceManagementDAOException e) {
            log.error("Error occurred while retrieving the application list of '" + device.getType() + "', " +
                    "which carries the id '" + device.getId() + "'", e);
        } catch (SQLException e) {
            log.error("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return applications;
    }

    /**
     *  Returns all the available information (device-info, location, applications and plugin-db data)
     *  of the given device list.
     */
    private List<Device> getAllDeviceInfo(List<Device> allDevices)
            throws DeviceManagementException {
        List<Device> devices = new ArrayList<>();
        if (allDevices != null) {
            for (Device device : allDevices) {
                device.setDeviceInfo(this.getDeviceInfo(device));
                device.setApplications(this.getInstalledApplications(device));

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
        }
        return devices;
    }

    /**
     *  Returns all the available information (device-info, location, applications and plugin-db data)
     *  of a given device.
     */
    private Device getAllDeviceInfo(Device device) throws DeviceManagementException {
        device.setDeviceInfo(this.getDeviceInfo(device));
        device.setApplications(this.getInstalledApplications(device));

        DeviceManager deviceManager = this.getDeviceManager(device.getType());
        if (deviceManager == null) {
            if (log.isDebugEnabled()) {
                log.debug("Device Manager associated with the device type '" + device.getType() + "' is null. " +
                        "Therefore, not attempting method 'isEnrolled'");
            }
            return device;
        }
        Device dmsDevice =
                deviceManager.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
        if (dmsDevice != null) {
            device.setFeatures(dmsDevice.getFeatures());
            device.setProperties(dmsDevice.getProperties());
        }
        return device;
    }

    private Device getDeviceFromCache(DeviceIdentifier deviceIdentifier) {
        return DeviceCacheManagerImpl.getInstance().getDeviceFromCache(deviceIdentifier, this.getTenantId());
    }

    private void addDeviceToCache(DeviceIdentifier deviceIdentifier, Device device) {
        DeviceCacheManagerImpl.getInstance().addDeviceToCache(deviceIdentifier, device, this.getTenantId());
    }

    private void removeDeviceFromCache(DeviceIdentifier deviceIdentifier) {
        DeviceCacheManagerImpl.getInstance().removeDeviceFromCache(deviceIdentifier, this.getTenantId());
    }
}