/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementPluginRepository;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.email.NotificationMessages;
import org.wso2.carbon.device.mgt.core.dao.*;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.email.EmailConstants;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.internal.EmailServiceDataHolder;
import org.wso2.carbon.device.mgt.core.internal.PluginInitializationListener;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceManagementProviderServiceImpl implements DeviceManagementProviderService,
        PluginInitializationListener {

    private DeviceDAO deviceDAO;
    private DeviceTypeDAO deviceTypeDAO;
    private EnrolmentDAO enrolmentDAO;
    private DeviceManagementPluginRepository pluginRepository;
    private boolean isTest = false;

    private static Log log = LogFactory.getLog(DeviceManagementProviderServiceImpl.class);
    private int tenantId;

    public DeviceManagementProviderServiceImpl() {

        this.pluginRepository = new DeviceManagementPluginRepository();
        initDataAccessObjects();
        /* Registering a listener to retrieve events when some device management service plugin is installed after
        * the component is done getting initialized */
        DeviceManagementServiceComponent.registerPluginInitializationListener(this);
    }


    /**
     * This constructor calls from unit tests
     * @param pluginRepo
     */
    DeviceManagementProviderServiceImpl(DeviceManagementPluginRepository pluginRepo, boolean test){
        this.pluginRepository = pluginRepo;
        initDataAccessObjects();
        isTest = test;
    }

    private void initDataAccessObjects() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.enrolmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();
    }

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean saveConfiguration(TenantConfiguration configuration)
            throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(configuration.getType()).getDeviceManager();
        return dms.saveConfiguration(configuration);
    }

    @Override
    public TenantConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public TenantConfiguration getConfiguration(String type) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(type).getDeviceManager();
        return dms.getConfiguration();
    }

    @Override
    public FeatureManager getFeatureManager(String type) {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(type).getDeviceManager();
        return dms.getFeatureManager();
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {

        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(device.getType()).getDeviceManager();
        boolean status = dms.enrollDevice(device);
        try {
            if (dms.isClaimable(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()))) {
                device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.INACTIVE);
            } else {
                device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.ACTIVE);
            }
            int tenantId = getTenantId();

            DeviceManagementDAOFactory.beginTransaction();

            DeviceType type = deviceTypeDAO.getDeviceType(device.getType());
            int deviceId = deviceDAO.addDevice(type.getId(), device, tenantId);
            int enrolmentId = enrolmentDAO.addEnrollment(deviceId, device.getEnrolmentInfo(), tenantId);

            if (log.isDebugEnabled()) {
                log.debug("An enrolment is successfully created with the id '" + enrolmentId + "' associated with " +
                        "the device identified by key '" + device.getDeviceIdentifier() + "', which belongs to " +
                        "platform '" + device.getType() + " upon the user '" +
                        device.getEnrolmentInfo().getOwner() + "'");
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            try {
                DeviceManagementDAOFactory.rollbackTransaction();
            } catch (DeviceManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the current transaction", e);
            }
            throw new DeviceManagementException("Error occurred while enrolling the device " +
                    "'" + device.getId() + "'", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        return status;
    }



    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(device.getType()).getDeviceManager();
        boolean status = dms.modifyEnrollment(device);
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            DeviceType type = deviceTypeDAO.getDeviceType(device.getType());
            int deviceId = deviceDAO.updateDevice(type.getId(), device, tenantId);
            enrolmentDAO.updateEnrollment(deviceId, device.getEnrolmentInfo(), tenantId);

            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            try {
                DeviceManagementDAOFactory.rollbackTransaction();
            } catch (DeviceManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the current transaction", e);
            }
            throw new DeviceManagementException("Error occurred while modifying the device " +
                    "'" + device.getId() + "'", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        return status;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(deviceId.getType()).getDeviceManager();
        try {
            Device device = deviceDAO.getDevice(deviceId,tenantId);
            DeviceType deviceType = deviceTypeDAO.getDeviceType(device.getType());

            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.getEnrolmentInfo().setStatus(EnrolmentInfo.Status.REMOVED);
            deviceDAO.updateDevice(deviceType.getId(), device, tenantId);

        } catch (DeviceManagementDAOException e) {
            String errorMsg =  "Error occurred while fetch device for device Identifier:";
            log.error(errorMsg + deviceId.toString(),e);
            throw new DeviceManagementException(errorMsg, e);

        }
        return dms.disenrollDevice(deviceId);
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(deviceId.getType()).getDeviceManager();
        return dms.isEnrolled(deviceId);
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(deviceId.getType()).getDeviceManager();
        return dms.isActive(deviceId);
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status)
            throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(deviceId.getType()).getDeviceManager();
        return dms.setActive(deviceId, status);
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        List<Device> devices = new ArrayList<Device>();
        List<Device> allDevices;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.getConnection();
            allDevices = deviceDAO.getDevices(tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving device list pertaining to " +
                    "the current tenant", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        for (Device device : allDevices) {
            Device dmsDevice =
                    this.getPluginRepository().getDeviceManagementService(
                            device.getType()).getDeviceManager().getDevice(
                            new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }

    @Override
    public List<Device> getAllDevices(String type) throws DeviceManagementException {
        List<Device> devices = new ArrayList<Device>();
        List<Device> allDevices;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.getConnection();
            allDevices = deviceDAO.getDevices(type, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving all devices of type '" +
                    type + "' that are being managed within the scope of current tenant", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }

        for (Device device : allDevices) {
            Device dmsDevice =
                    this.getPluginRepository().getDeviceManagementService(
                            device.getType()).getDeviceManager().getDevice(
                            new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }

    @Override
    public void sendEnrolmentInvitation(EmailMessageProperties emailMessageProperties)
            throws DeviceManagementException {

        List<NotificationMessages> notificationMessages =
                DeviceConfigurationManager.getInstance().getNotificationMessagesConfig().getNotificationMessagesList();

        String messageHeader = "";
        String messageBody = "";
        String messageFooter1 = "";
        String messageFooter2 = "";
        String messageFooter3 = "";
        String url = "";
        String subject = "";

        for (NotificationMessages notificationMessage : notificationMessages) {
            if (org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailNotifications.ENROL_NOTIFICATION_TYPE.equals(
                    notificationMessage.getType())) {
                messageHeader = notificationMessage.getHeader();
                messageBody = notificationMessage.getBody();
                messageFooter1 = notificationMessage.getFooterLine1();
                messageFooter2 = notificationMessage.getFooterLine2();
                messageFooter3 = notificationMessage.getFooterLine3();
                url = notificationMessage.getUrl();
                subject = notificationMessage.getSubject();
                break;
            }
        }

        StringBuilder messageBuilder = new StringBuilder();

        try {
            messageHeader = messageHeader.replaceAll("\\{" + EmailConstants.EnrolmentEmailConstants.FIRST_NAME + "\\}",
                    URLEncoder.encode(emailMessageProperties.getFirstName(),
                            EmailConstants.EnrolmentEmailConstants.ENCODED_SCHEME));
            messageBody = messageBody.trim() + System.getProperty("line.separator") +
                    System.getProperty("line.separator") + url.replaceAll("\\{"
                    + EmailConstants.EnrolmentEmailConstants.DOWNLOAD_URL + "\\}",
                    URLDecoder.decode(emailMessageProperties.getEnrolmentUrl(),
                            EmailConstants.EnrolmentEmailConstants.ENCODED_SCHEME));

            messageBuilder.append(messageHeader).append(System.getProperty("line.separator"))
                    .append(System.getProperty("line.separator"));
            messageBuilder.append(messageBody);
            messageBuilder.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
            messageBuilder.append(messageFooter1.trim())
                    .append(System.getProperty("line.separator")).append(messageFooter2.trim()).append(System
                    .getProperty("line.separator")).append(messageFooter3.trim());

        } catch (IOException e) {
            log.error("IO error in processing enrol email message " + emailMessageProperties);
            throw new DeviceManagementException("Error replacing tags in email template '" +
                    emailMessageProperties.getSubject() + "'", e);
        }
        emailMessageProperties.setMessageBody(messageBuilder.toString());
        emailMessageProperties.setSubject(subject);
        EmailServiceDataHolder.getInstance().getEmailServiceProvider().sendEmail(emailMessageProperties);
    }

    @Override
    public void sendRegistrationEmail(EmailMessageProperties emailMessageProperties) throws DeviceManagementException {
        List<NotificationMessages> notificationMessages = DeviceConfigurationManager.getInstance()
                .getNotificationMessagesConfig().getNotificationMessagesList();

        String messageHeader = "";
        String messageBody = "";
        String messageFooter1 = "";
        String messageFooter2 = "";
        String messageFooter3 = "";
        String url = "";
        String subject = "";

        for (NotificationMessages notificationMessage : notificationMessages) {
            if (org.wso2.carbon.device.mgt.core.DeviceManagementConstants.EmailNotifications.USER_REGISTRATION_NOTIFICATION_TYPE.
                    equals(notificationMessage.getType())) {
                messageHeader = notificationMessage.getHeader();
                messageBody = notificationMessage.getBody();
                messageFooter1 = notificationMessage.getFooterLine1();
                messageFooter2 = notificationMessage.getFooterLine2();
                messageFooter3 = notificationMessage.getFooterLine3();
                url = notificationMessage.getUrl();
                subject = notificationMessage.getSubject();
                break;
            }
        }

        StringBuilder messageBuilder = new StringBuilder();

        try {
            messageHeader = messageHeader.replaceAll("\\{" + EmailConstants.EnrolmentEmailConstants.FIRST_NAME + "\\}",
                    URLEncoder.encode(emailMessageProperties.getFirstName(),
                            EmailConstants.EnrolmentEmailConstants.ENCODED_SCHEME));

            messageBody = messageBody.trim().replaceAll("\\{" + EmailConstants.EnrolmentEmailConstants
                    .USERNAME
                    + "\\}",
                    URLEncoder.encode(emailMessageProperties.getUserName(), EmailConstants.EnrolmentEmailConstants
                            .ENCODED_SCHEME));

            messageBody = messageBody.replaceAll("\\{" + EmailConstants.EnrolmentEmailConstants.PASSWORD + "\\}",
                    URLEncoder.encode(emailMessageProperties.getPassword(), EmailConstants.EnrolmentEmailConstants
                            .ENCODED_SCHEME));

            messageBody = messageBody + System.getProperty("line.separator") + url.replaceAll("\\{"
                    + EmailConstants.EnrolmentEmailConstants.DOWNLOAD_URL + "\\}",
                    URLDecoder.decode(emailMessageProperties.getEnrolmentUrl(),
                            EmailConstants.EnrolmentEmailConstants.ENCODED_SCHEME));

            messageBuilder.append(messageHeader).append(System.getProperty("line.separator"));
            messageBuilder.append(messageBody).append(System.getProperty("line.separator")).append(
                    messageFooter1.trim());
            messageBuilder.append(System.getProperty("line.separator")).append(messageFooter2.trim());
            messageBuilder.append(System.getProperty("line.separator")).append(messageFooter3.trim());

        } catch (IOException e) {
            log.error("IO error in processing enrol email message " + emailMessageProperties);
            throw new DeviceManagementException("Error replacing tags in email template '" +
                    emailMessageProperties.getSubject() + "'", e);
        }
        emailMessageProperties.setMessageBody(messageBuilder.toString());
        emailMessageProperties.setSubject(subject);
        EmailServiceDataHolder.getInstance().getEmailServiceProvider().sendEmail(emailMessageProperties);
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        Device device;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            device = deviceDAO.getDevice(deviceId, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                    "'" + deviceId.getId() + "'", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        if (device != null) {
            // The changes made here to prevent unit tests getting failed. They failed because when running the unit
            // tests there is no osgi services. So getDeviceManager() returns a null.
          DeviceManagementService service =  this.getPluginRepository().getDeviceManagementService(deviceId.getType());
            if(service != null) {
                DeviceManager dms = service.getDeviceManager();
                Device pluginSpecificInfo = dms.getDevice(deviceId);
                if (pluginSpecificInfo != null) {
                    device.setFeatures(pluginSpecificInfo.getFeatures());
                    device.setProperties(pluginSpecificInfo.getProperties());
                }
            }
        }
        return device;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(device.getType()).getDeviceManager();
        return dms.updateDeviceInfo(deviceIdentifier, device);
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(deviceId.getType()).getDeviceManager();
        return dms.setOwnership(deviceId, ownershipType);
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementService(deviceId.getType()).getDeviceManager();
        return dms.isClaimable(deviceId);
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();

            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            Device device = deviceDAO.getDevice(deviceId, tenantId);
            boolean success = enrolmentDAO.setStatus(device.getId(), currentOwner, status, tenantId);

            DeviceManagementDAOFactory.commitTransaction();
            return success;
        } catch (DeviceManagementDAOException e) {
            try {
                DeviceManagementDAOFactory.rollbackTransaction();
            } catch (DeviceManagementDAOException e1) {
                log.warn("Error occurred while rollbacking the current transaction", e);
            }
            throw new DeviceManagementException("Error occurred while setting enrollment status", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
    }

    @Override
    public License getLicense(String deviceType, String languageCode) throws LicenseManagementException {
        return DeviceManagementDataHolder.getInstance().getLicenseManager().getLicense(deviceType, languageCode);
    }

    @Override
    public boolean addLicense(String type, License license) throws LicenseManagementException {
        return DeviceManagementDataHolder.getInstance().getLicenseManager().addLicense(type, license);
    }

    private DeviceManagementPluginRepository getPluginRepository() {
        return pluginRepository;
    }

    @Override
    public int addOperation(Operation operation, List<DeviceIdentifier> devices) throws
            OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().addOperation(operation, devices);
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperations(deviceId);
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
    public void deleteOperation(int operationId) throws OperationManagementException {
        DeviceManagementDataHolder.getInstance().getOperationManager().deleteOperation(operationId);
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationByDeviceAndOperationId(
                deviceId, operationId);
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            DeviceIdentifier identifier,
            Operation.Status status) throws OperationManagementException, DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperationsByDeviceAndStatus(
                identifier, status);
    }

    @Override
    public Operation getOperation(int operationId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getOperationManager().getOperation(operationId);
    }

    @Override
    public List<Device> getDevicesOfUser(String username) throws DeviceManagementException {
        List<Device> devices = new ArrayList<Device>();
        List<Device> userDevices;
        try {
            DeviceManagementDAOFactory.getConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userDevices = deviceDAO.getDevicesOfUser(username, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the list of devices that " +
                    "belong to the user '" + username + "'", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }

        for (Device device : userDevices) {
            Device dmsDevice =
                    this.getPluginRepository().getDeviceManagementService(
                            device.getType()).getDeviceManager().getDevice(
                            new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;

    }

    @Override
    public List<Device> getAllDevicesOfRole(String role) throws DeviceManagementException {
        List<Device> devices = new ArrayList<Device>();

        String[] users;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            users =  DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager().getUserListOfRole(role);
        } catch (UserStoreException e) {
            throw new DeviceManagementException("Error occurred while obtaining the users, who are assigned " +
                    "with the role '" + role + "'", e);
        }

        List<Device> userDevices;
        for (String user : users) {
            userDevices = new ArrayList<Device>();
            try {
                DeviceManagementDAOFactory.getConnection();
                userDevices = deviceDAO.getDevicesOfUser(user, tenantId);
            } catch (DeviceManagementDAOException e) {
                log.error("Error occurred while obtaining the devices of user '" + user + "'", e);
            } finally {
                try {
                    DeviceManagementDAOFactory.closeConnection();
                } catch (DeviceManagementDAOException e) {
                    log.warn("Error occurred while closing the connection", e);
                }
            }
            for (Device device : userDevices) {
                Device dmsDevice =
                        this.getPluginRepository().getDeviceManagementService(
                                device.getType()).getDeviceManager().getDevice(
                                new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
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
    public int getDeviceCount() throws DeviceManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.getConnection();
            return deviceDAO.getDeviceCount(tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the device count", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
    }

    @Override
    public List<Device> getDevicesByName(String deviceName) throws DeviceManagementException {
        List<Device> devices = new ArrayList<Device>();
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.getConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            allDevices = deviceDAO.getDevicesByName(deviceName, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while fetching the list of devices that matches to '"
                    + deviceName + "'", e);
        } finally {
            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }
        for (Device device : allDevices) {
            Device dmsDevice =
                    this.getPluginRepository().getDeviceManagementService(
                            device.getType()).getDeviceManager().getDevice(
                            new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;

    }

    @Override
    public void updateDeviceEnrolmentInfo(Device device, EnrolmentInfo.Status status) throws DeviceManagementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceType deviceType = deviceTypeDAO.getDeviceType(device.getType());
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.getEnrolmentInfo().setStatus(status);
            deviceDAO.updateDevice(deviceType.getId(), device, tenantId);
        }catch (DeviceManagementDAOException deviceDaoEx){
            String errorMsg = "Error occured update device enrolment status : "+device.getId();
            log.error(errorMsg, deviceDaoEx);
            throw new DeviceManagementException(errorMsg, deviceDaoEx);
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


    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status) throws DeviceManagementException {
        List<Device> devices = new ArrayList<Device>();
        List<Device> allDevices;
        try {
            DeviceManagementDAOFactory.getConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            allDevices = deviceDAO.getDevicesByStatus(status, tenantId);

        } catch (DeviceManagementDAOException e) {
            String errorMsg = "Error occurred while fetching the list of devices that matches to status: '"
                              + status + "'";
            log.error(errorMsg, e);
            throw new DeviceManagementException(errorMsg, e);
        } finally {

            try {
                DeviceManagementDAOFactory.closeConnection();
            } catch (DeviceManagementDAOException e) {
                log.warn("Error occurred while closing the connection", e);
            }
        }

        for (Device device : allDevices) {
            Device dmsDevice =
                    this.getPluginRepository().getDeviceManagementService(
                            device.getType()).getDeviceManager().getDevice(
                            new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (dmsDevice != null) {
                device.setFeatures(dmsDevice.getFeatures());
                device.setProperties(dmsDevice.getProperties());
            }
            devices.add(device);
        }
        return devices;
    }



    private int getTenantId() {

        ThreadLocal<Integer> tenantId = new ThreadLocal<Integer>();
        int tenant = 0;

        if (isTest){
            tenant = DeviceManagerUtil.currentTenant.get();
        }else{
            tenant = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        }
        return tenant;
    }

}
