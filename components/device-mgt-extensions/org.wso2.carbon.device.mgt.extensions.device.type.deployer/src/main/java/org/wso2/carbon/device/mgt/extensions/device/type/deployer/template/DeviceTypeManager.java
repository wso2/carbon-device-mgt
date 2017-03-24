/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.*;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.util.DeviceTypePluginConstants;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeDeployerFileException;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeMgtPluginException;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.dao.DeviceDAODefinition;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.dao.DeviceTypePluginDAOManager;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.feature.ConfigurationBasedFeatureManager;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.util.DeviceTypeUtils;
import org.wso2.carbon.device.mgt.extensions.license.mgt.registry.RegistryBasedLicenseManager;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * This holds the implementation of the device manager. From which an instance of it will be created using the
 * deployer file.
 */
public class DeviceTypeManager implements DeviceManager {

    private static final Log log = LogFactory.getLog(DeviceTypeManager.class);
    private String deviceType;
    private DeviceTypePluginDAOManager deviceTypePluginDAOManager;
    private LicenseManager licenseManager;
    private boolean propertiesExist;
    private boolean requiredDeviceTypeAuthorization;
    private boolean claimable;

    private FeatureManager featureManager;

    public DeviceTypeManager(DeviceTypeConfigIdentifier deviceTypeConfigIdentifier,
                             DeviceTypeConfiguration deviceTypeConfiguration) {
        deviceType = deviceTypeConfigIdentifier.getDeviceType();
        if (deviceTypeConfiguration.getFeatures() != null && deviceTypeConfiguration.getFeatures().
                getFeature() != null) {
            List<Feature> features = deviceTypeConfiguration.getFeatures().getFeature();
            if (features != null) {
                featureManager = new ConfigurationBasedFeatureManager(features);
            }
        }
        if (deviceTypeConfiguration.getDeviceAuthorizationConfig() != null) {
            requiredDeviceTypeAuthorization = deviceTypeConfiguration.getDeviceAuthorizationConfig().
                    isAuthorizationRequired();
        } else {
            requiredDeviceTypeAuthorization = true;
        }
        //add license to registry.
        this.licenseManager = new RegistryBasedLicenseManager();
        try {
            if (licenseManager.getLicense(deviceType, DeviceTypePluginConstants.LANGUAGE_CODE_ENGLISH_US) == null) {

                if (deviceTypeConfiguration.getLicense() != null) {
                    License defaultLicense = new License();
                    defaultLicense.setLanguage(deviceTypeConfiguration.getLicense().getLanguage());
                    defaultLicense.setVersion(deviceTypeConfiguration.getLicense().getVersion());
                    defaultLicense.setText(deviceTypeConfiguration.getLicense().getText());
                    licenseManager.addLicense(deviceType, defaultLicense);
                }
            }
        } catch (LicenseManagementException e) {
            String msg = "Error occurred while adding default license for " + deviceType + " devices";
            throw new DeviceTypeDeployerFileException(msg, e);
        }
        claimable = false;
        if (deviceTypeConfiguration.getClaimable() != null ) {
            claimable = deviceTypeConfiguration.getClaimable().isEnabled();
        }

        DeviceDetails deviceDetails = deviceTypeConfiguration.getDeviceDetails();

        if (deviceDetails != null) {

            //Check whether device dao definition exist.
            String tableName = deviceTypeConfiguration.getDeviceDetails().getTableId();
            if (tableName != null && !tableName.isEmpty()) {
                List<Table> tables = deviceTypeConfiguration.getDataSource().getTableConfig().getTable();
                Table deviceDefinitionTable = null;
                for (Table table : tables) {
                    if (tableName.equals(table.getName())) {
                        deviceDefinitionTable = table;
                        break;
                    }
                }
                if (deviceDefinitionTable == null) {
                    throw new DeviceTypeDeployerFileException("Could not find definition for table: " + tableName);
                }
                propertiesExist = true;

                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);

                    DeviceDAODefinition deviceDAODefinition = new DeviceDAODefinition(deviceDefinitionTable);
                    String datasourceName = deviceTypeConfiguration.getDataSource().getJndiConfig().getName();
                    if (datasourceName != null && !datasourceName.isEmpty()) {
                        String setupOption = System.getProperty("setup");
                        if (setupOption != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("-Dsetup is enabled. Device management repository schema initialization is about " +
                                        "to begin");
                            }
                            try {
                                DeviceTypeUtils.setupDeviceManagementSchema(datasourceName, deviceType,
                                        deviceDAODefinition.getDeviceTableName());
                            } catch (DeviceTypeMgtPluginException e) {
                                log.error("Exception occurred while initializing device management database schema", e);
                            }
                        }
                        deviceTypePluginDAOManager = new DeviceTypePluginDAOManager(datasourceName, deviceDAODefinition);
                    } else {
                        throw new DeviceTypeDeployerFileException("Invalid datasource name.");
                    }
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
    }

    @Override
    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    @Override
    public boolean saveConfiguration(PlatformConfiguration tenantConfiguration)
            throws DeviceManagementException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Persisting " + deviceType + " configurations in Registry");
            }
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(PlatformConfiguration.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(tenantConfiguration, writer);

            Resource resource = DeviceTypeUtils.getConfigurationRegistry().newResource();
            resource.setContent(writer.toString());
            resource.setMediaType(DeviceTypePluginConstants.MEDIA_TYPE_XML);
            DeviceTypeUtils.putRegistryResource(deviceType, resource);
            return true;
        } catch (DeviceTypeMgtPluginException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry instance : " + e.getMessage(), e);
        } catch (RegistryException e) {
            throw new DeviceManagementException(
                    "Error occurred while persisting the Registry resource of " + deviceType + " Configuration : "
                            + e.getMessage(), e);
        } catch (JAXBException e) {
            throw new DeviceManagementException(
                    "Error occurred while parsing the " + deviceType + " configuration : " + e.getMessage(), e);
        }
    }

    @Override
    public PlatformConfiguration getConfiguration() throws DeviceManagementException {
        Resource resource;
        try {
            resource = DeviceTypeUtils.getRegistryResource(deviceType);
            if (resource != null) {
                JAXBContext context = JAXBContext.newInstance(PlatformConfiguration.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (PlatformConfiguration) unmarshaller.unmarshal(
                        new StringReader(new String((byte[]) resource.getContent(), Charset.
                                forName(DeviceTypePluginConstants.CHARSET_UTF8))));
            }
            return null;
        } catch (DeviceTypeMgtPluginException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry instance : " + e.getMessage(), e);
        } catch (JAXBException e) {
            throw new DeviceManagementException(
                    "Error occurred while parsing the " + deviceType + " configuration : " + e.getMessage(), e);
        } catch (RegistryException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry resource of " + deviceType + " Configuration : "
                            + e.getMessage(), e);
        }
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        if (propertiesExist) {
            boolean status = false;
            boolean isEnrolled = this.isEnrolled(
                    new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Enrolling a new Android device : " + device.getDeviceIdentifier());
                }
                if (isEnrolled) {
                    this.modifyEnrollment(device);
                } else {
                    deviceTypePluginDAOManager.getDeviceTypeDAOHandler().beginTransaction();
                    status = deviceTypePluginDAOManager.getDeviceDAO().addDevice(device);
                    deviceTypePluginDAOManager.getDeviceTypeDAOHandler().commitTransaction();
                }
            } catch (DeviceTypeMgtPluginException e) {
                try {
                    deviceTypePluginDAOManager.getDeviceTypeDAOHandler().rollbackTransaction();
                } catch (DeviceTypeMgtPluginException ex) {
                    String msg = "Error occurred while roll back the device enrol transaction :" +
                            device.toString();
                    log.warn(msg, ex);
                }
                String msg = "Error while enrolling the " + deviceType + " device : " + device.getDeviceIdentifier();
                throw new DeviceManagementException(msg, e);
            }
            return status;
        }
        return true;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        if (propertiesExist) {
            boolean status;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Modifying the Android device enrollment data");
                }
                deviceTypePluginDAOManager.getDeviceTypeDAOHandler().beginTransaction();
                status = deviceTypePluginDAOManager.getDeviceDAO().updateDevice(device);
                deviceTypePluginDAOManager.getDeviceTypeDAOHandler().commitTransaction();
            } catch (DeviceTypeMgtPluginException e) {
                try {
                    deviceTypePluginDAOManager.getDeviceTypeDAOHandler().rollbackTransaction();
                } catch (DeviceTypeMgtPluginException mobileDAOEx) {
                    String msg = "Error occurred while roll back the update device transaction :" +
                            device.toString();
                    log.warn(msg, mobileDAOEx);
                }
                String msg = "Error while updating the enrollment of the " + deviceType + " device : " +
                        device.getDeviceIdentifier();
                throw new DeviceManagementException(msg, e);
            }
            return status;
        }
        return true;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        //Here we don't have anything specific to do. Hence returning.
        return true;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        if (propertiesExist) {
            boolean isEnrolled = false;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Checking the enrollment of Android device : " + deviceId.getId());
                }
                Device device =
                        deviceTypePluginDAOManager.getDeviceDAO().getDevice(deviceId.getId());
                if (device != null) {
                    isEnrolled = true;
                }
            } catch (DeviceTypeMgtPluginException e) {
                String msg = "Error while checking the enrollment status of " + deviceType + " device : " +
                        deviceId.getId();
                throw new DeviceManagementException(msg, e);
            }
            return isEnrolled;
        }
        return true;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status)
            throws DeviceManagementException {
        return true;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        if (propertiesExist) {
            Device device;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Getting the details of " + deviceType + " device : '" + deviceId.getId() + "'");
                }
                device = deviceTypePluginDAOManager.getDeviceDAO().getDevice(deviceId.getId());
            } catch (DeviceTypeMgtPluginException e) {
                throw new DeviceManagementException(
                        "Error occurred while fetching the " + deviceType + " device: '" + deviceId.getId() + "'", e);
            }
            return device;
        }
        return null;
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return claimable;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceIdentifier, String currentUser,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        return false;
    }

    @Override
    public License getLicense(String languageCode) throws LicenseManagementException {
        return licenseManager.getLicense(deviceType, languageCode);
    }

    @Override
    public void addLicense(License license) throws LicenseManagementException {
        licenseManager.addLicense(deviceType, license);
    }

    @Override
    public boolean requireDeviceAuthorization() {
        return requiredDeviceTypeAuthorization;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device)
            throws DeviceManagementException {
        if (propertiesExist) {
            boolean status;
            Device existingDevice = this.getDevice(deviceIdentifier);
            existingDevice.setProperties(device.getProperties());

            try {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "updating the details of " + deviceType + " device : " + device.getDeviceIdentifier());
                }
                deviceTypePluginDAOManager.getDeviceTypeDAOHandler().beginTransaction();
                status = deviceTypePluginDAOManager.getDeviceDAO().updateDevice(existingDevice);
                deviceTypePluginDAOManager.getDeviceTypeDAOHandler().commitTransaction();
            } catch (DeviceTypeMgtPluginException e) {
                try {
                    deviceTypePluginDAOManager.getDeviceTypeDAOHandler().rollbackTransaction();
                } catch (DeviceTypeMgtPluginException e1) {
                    log.warn("Error occurred while roll back the update device info transaction : '" +
                            device.toString() + "'", e1);
                }
                throw new DeviceManagementException(
                        "Error occurred while updating the " + deviceType + " device: '" +
                                device.getDeviceIdentifier() + "'", e);
            }
            return status;
        }
        return true;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        if (propertiesExist) {
            List<Device> devices = null;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Fetching the details of all " + deviceType + " devices");
                }
                devices = deviceTypePluginDAOManager.getDeviceDAO().getAllDevices();
            } catch (DeviceTypeMgtPluginException e) {
                throw new DeviceManagementException("Error occurred while fetching all " + deviceType + " devices", e);
            }
            return devices;
        }
        return null;
    }

}
