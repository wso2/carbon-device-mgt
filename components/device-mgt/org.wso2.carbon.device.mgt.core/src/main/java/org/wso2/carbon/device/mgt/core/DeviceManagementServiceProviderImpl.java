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
package org.wso2.carbon.device.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.spi.DeviceManager;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.email.NotificationMessages;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.dto.Status;
import org.wso2.carbon.device.mgt.core.email.EmailConstants;
import org.wso2.carbon.device.mgt.core.internal.EmailServiceDataHolder;
import org.wso2.carbon.device.mgt.core.license.mgt.LicenseManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DeviceManagementServiceProviderImpl implements DeviceManagementService {

    private DeviceDAO deviceDAO;
    private DeviceTypeDAO deviceTypeDAO;
    private DeviceManagementRepository pluginRepository;
    private OperationManager operationManager;
    private LicenseManager licenseManager;

    private static Log log = LogFactory.getLog(DeviceManagementServiceProviderImpl.class);

    public DeviceManagementServiceProviderImpl(DeviceManagementRepository pluginRepository) {
        this.pluginRepository = pluginRepository;
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.operationManager = new OperationManagerImpl();
        this.licenseManager = new LicenseManagerImpl();
    }

    public DeviceManagementServiceProviderImpl(){
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
        this.operationManager = new OperationManagerImpl();
        this.licenseManager = new LicenseManagerImpl();
    }

    @Override
    public String getProviderType() {
        return null;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public FeatureManager getFeatureManager(String type) {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(type);
        return dms.getFeatureManager();
    }

    @Override
    public Device getCoreDevice(DeviceIdentifier deviceId) throws DeviceManagementException {

        Device convertedDevice = null;
        try {
            DeviceType deviceType = this.getDeviceTypeDAO().getDeviceType(deviceId.getType());
            org.wso2.carbon.device.mgt.core.dto.Device device = this.getDeviceDAO().getDevice(deviceId);
            if (device != null) {
                convertedDevice = DeviceManagementDAOUtil.convertDevice(device,
                        this.getDeviceTypeDAO().getDeviceType(deviceType.getId()));
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                    "'" + deviceId.getId() + "' and type:"+deviceId.getType(), e);
        }
        return convertedDevice;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(device.getType());
        boolean status = dms.enrollDevice(device);
        try {
            org.wso2.carbon.device.mgt.core.dto.Device deviceDto = DeviceManagementDAOUtil.convertDevice(device);
            DeviceType deviceType = this.getDeviceTypeDAO().getDeviceType(device.getType());
            deviceDto.setStatus(Status.ACTIVE);
            deviceDto.setDeviceTypeId(deviceType.getId());
            this.getDeviceDAO().addDevice(deviceDto);
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while enrolling the device " +
                    "'" + device.getId() + "'", e);
        }
        return status;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(device.getType());
        boolean status = dms.modifyEnrollment(device);
        try {
            this.getDeviceDAO().updateDevice(DeviceManagementDAOUtil.convertDevice(device));
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while modifying the device " +
                    "'" + device.getId() + "'", e);
        }
        return status;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(deviceId.getType());
        return dms.disenrollDevice(deviceId);
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(deviceId.getType());
        return dms.isEnrolled(deviceId);
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(deviceId.getType());
        return dms.isActive(deviceId);
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status)
            throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(deviceId.getType());
        return dms.setActive(deviceId, status);
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        List<Device> convertedDevicesList = new ArrayList<Device>();
        try {
            List<org.wso2.carbon.device.mgt.core.dto.Device> devicesList = this.deviceDAO.getDevices();
            for (int x = 0; x < devicesList.size(); x++) {
                org.wso2.carbon.device.mgt.core.dto.Device device = devicesList.get(x);
                device.setDeviceType(deviceTypeDAO.getDeviceType(device.getDeviceTypeId()));
                DeviceManager dms =
                        this.getPluginRepository().getDeviceManagementProvider(device.getDeviceType().getName());
                DeviceType deviceType = this.deviceTypeDAO.getDeviceType(device.getDeviceTypeId());
                Device convertedDevice = DeviceManagementDAOUtil.convertDevice(device, deviceType);
                DeviceIdentifier deviceIdentifier =
                        DeviceManagementDAOUtil.createDeviceIdentifier(device, deviceType);
                Device dmsDevice = dms.getDevice(deviceIdentifier);
                if (dmsDevice != null) {
                    convertedDevice.setProperties(dmsDevice.getProperties());
                    convertedDevice.setFeatures(dmsDevice.getFeatures());
                }
                convertedDevicesList.add(convertedDevice);
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining devices all devices", e);
        }
        return convertedDevicesList;
    }

    @Override
    public List<Device> getAllDevices(String type) throws DeviceManagementException {
        DeviceManager dms = this.getPluginRepository().getDeviceManagementProvider(type);
        List<Device> devicesList = new ArrayList<Device>();
        try {
            DeviceType dt = this.getDeviceTypeDAO().getDeviceType(type);
            List<org.wso2.carbon.device.mgt.core.dto.Device> devices =
                    this.getDeviceDAO().getDevices(dt.getId());

            for (org.wso2.carbon.device.mgt.core.dto.Device device : devices) {
                DeviceType deviceType = this.deviceTypeDAO.getDeviceType(device.getDeviceTypeId());
                Device convertedDevice = DeviceManagementDAOUtil.convertDevice(device, deviceType);
                DeviceIdentifier deviceIdentifier =
                        DeviceManagementDAOUtil.createDeviceIdentifier(device, deviceType);
                Device dmsDevice = dms.getDevice(deviceIdentifier);
                if (dmsDevice != null) {
                    convertedDevice.setProperties(dmsDevice.getProperties());
                    convertedDevice.setFeatures(dmsDevice.getFeatures());
                }
                devicesList.add(convertedDevice);
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for type " +
                    "'" + type + "'", e);
        }
        return devicesList;
    }

    @Override
    public List<Device> getDeviceListOfUser(String username) throws DeviceManagementException {
        List<Device> devicesOfUser = new ArrayList<Device>();
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            List<org.wso2.carbon.device.mgt.core.dto.Device> devicesList = this.deviceDAO
                    .getDeviceListOfUser(username, tenantId);
            for (int x = 0; x < devicesList.size(); x++) {
                org.wso2.carbon.device.mgt.core.dto.Device device = devicesList.get(x);
                device.setDeviceType(deviceTypeDAO.getDeviceType(device.getDeviceTypeId()));
                DeviceManager dms =
                        this.getPluginRepository().getDeviceManagementProvider(device.getDeviceType().getName());
                Device convertedDevice = DeviceManagementDAOUtil.convertDevice(device, device.getDeviceType());
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                deviceIdentifier.setId(device.getDeviceIdentificationId());
                deviceIdentifier.setType(device.getDeviceType().getName());
                Device dmsDevice = dms.getDevice(deviceIdentifier);
                if (dmsDevice != null) {
                    convertedDevice.setProperties(dmsDevice.getProperties());
                    convertedDevice.setFeatures(dmsDevice.getFeatures());
                }
                devicesOfUser.add(convertedDevice);
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining devices for user " +
                    "'" + username + "'", e);
        }
        return devicesOfUser;
    }

    @Override
    public void sendEnrolmentInvitation(EmailMessageProperties emailMessageProperties) throws DeviceManagementException {

        List<NotificationMessages> notificationMessages = DeviceConfigurationManager.getInstance()
                .getNotificationMessagesConfig().getNotificationMessagesList();

        String messageHeader = "";
        String messageBody = "";
        String messageFooter = "";
        String url = "";
        String subject = "";

        for(NotificationMessages notificationMessage : notificationMessages){
            if (DeviceManagementConstants.EmailNotifications.ENROL_NOTIFICATION_TYPE.
		                                                   equals(notificationMessage.getType())) {
                messageHeader = notificationMessage.getHeader();
                messageBody = notificationMessage.getBody();
                messageFooter = notificationMessage.getFooter();
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
            messageBody = messageBody + System.getProperty("line.separator") + url.replaceAll("\\{"
                            + EmailConstants.EnrolmentEmailConstants.DOWNLOAD_URL + "\\}",
                    URLDecoder.decode(emailMessageProperties.getEnrolmentUrl(),
                            EmailConstants.EnrolmentEmailConstants.ENCODED_SCHEME));

	        messageBuilder.append(messageHeader).append(System.getProperty("line.separator"));
            messageBuilder.append(messageBody).append(System.getProperty("line.separator")).append(messageFooter);

        } catch (IOException e) {
            log.error("IO error in processing enrol email message "+emailMessageProperties);
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
        String messageFooter = "";
        String url = "";
        String subject = "";

        for(NotificationMessages notificationMessage : notificationMessages){
            if (DeviceManagementConstants.EmailNotifications.USER_REGISTRATION_NOTIFICATION_TYPE.
		                                                          equals(notificationMessage.getType())) {
                messageHeader = notificationMessage.getHeader();
                messageBody = notificationMessage.getBody();
                messageFooter = notificationMessage.getFooter();
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

            messageBody = messageBody.replaceAll("\\{" + EmailConstants.EnrolmentEmailConstants.USERNAME + "\\}",
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
            messageBuilder.append(messageBody).append(System.getProperty("line.separator")).append(messageFooter);

        } catch (IOException e) {
            log.error("IO error in processing enrol email message "+emailMessageProperties);
            throw new DeviceManagementException("Error replacing tags in email template '" +
                    emailMessageProperties.getSubject() + "'", e);
        }
        emailMessageProperties.setMessageBody(messageBuilder.toString());
        emailMessageProperties.setSubject(subject);
        EmailServiceDataHolder.getInstance().getEmailServiceProvider().sendEmail(emailMessageProperties);
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {

        DeviceManager dms = this.getPluginRepository().getDeviceManagementProvider(deviceId.getType());
        Device convertedDevice = null;
        try {
            DeviceType deviceType =
                    this.getDeviceTypeDAO().getDeviceType(deviceId.getType());
            org.wso2.carbon.device.mgt.core.dto.Device device =
                    this.getDeviceDAO().getDevice(deviceId);
            if (device != null) {
                convertedDevice = DeviceManagementDAOUtil
                        .convertDevice(device, this.getDeviceTypeDAO().getDeviceType(deviceType.getId()));
                Device dmsDevice = dms.getDevice(deviceId);
                if (dmsDevice != null) {
                    convertedDevice.setProperties(dmsDevice.getProperties());
                    convertedDevice.setFeatures(dmsDevice.getFeatures());
                }
            }
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while obtaining the device for id " +
                    "'" + deviceId.getId() + "'", e);
        }
        return convertedDevice;
    }

    @Override
    public boolean updateDeviceInfo(Device device) throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(device.getType());
        return dms.updateDeviceInfo(device);
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        DeviceManager dms =
                this.getPluginRepository().getDeviceManagementProvider(deviceId.getType());
        return dms.setOwnership(deviceId, ownershipType);
    }

    @Override
    public License getLicense(String deviceType, String languageCode) throws LicenseManagementException {
        return licenseManager.getLicense(deviceType, languageCode);
    }

    @Override
    public boolean addLicense(String type, License license) throws LicenseManagementException {
        return licenseManager.addLicense(type, license);
    }

    public DeviceDAO getDeviceDAO() {
        return deviceDAO;
    }

    public DeviceTypeDAO getDeviceTypeDAO() {
        return deviceTypeDAO;
    }

    public DeviceManagementRepository getPluginRepository() {
        return pluginRepository;
    }

    @Override
    public boolean addOperation(Operation operation, List<DeviceIdentifier> devices) throws
            OperationManagementException, DeviceManagementException {
        return operationManager.addOperation(operation, devices);
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return operationManager.getOperations(deviceId);
    }

    @Override
    public List<? extends Operation> getPendingOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return operationManager.getPendingOperations(deviceId);
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        return operationManager.getNextPendingOperation(deviceId);
    }

    @Override
    public void updateOperation(int operationId, Operation.Status operationStatus)
            throws OperationManagementException {
        operationManager.updateOperation(operationId, operationStatus);
    }

    @Override
    public void deleteOperation(int operationId) throws OperationManagementException {
            operationManager.deleteOperation(operationId);
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException {
        return operationManager.getOperationByDeviceAndOperationId(deviceId, operationId);
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(DeviceIdentifier identifier,
            Operation.Status status) throws OperationManagementException, DeviceManagementException {
        return operationManager.getOperationsByDeviceAndStatus(identifier, status);
    }

    @Override
    public Operation getOperation(int operationId) throws OperationManagementException {
        return operationManager.getOperation(operationId);
    }

    @Override
    public List<? extends Operation> getOperationsForStatus(Operation.Status status)
            throws OperationManagementException {
        return operationManager.getOperationsForStatus(status);
    }
}
