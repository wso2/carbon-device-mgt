/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.device.mgt.core.app.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.DeviceManagementPluginRepository;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.app.mgt.oauth.ServiceAuthenticator;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.identity.IdentityConfigurations;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Implements Application Manager interface
 *
 */
public class ApplicationManagerProviderServiceImpl implements ApplicationManager {

    private ConfigurationContext configCtx;
    private ServiceAuthenticator authenticator;
    private String oAuthAdminServiceUrl;
    private DeviceManagementPluginRepository pluginRepository;
    private DeviceDAO deviceDAO;

    private static final String GET_APP_LIST_URL = "store/apis/assets/mobileapp?domain=carbon.super&page=1";

    private static final Log log = LogFactory.getLog(ApplicationManagerProviderServiceImpl.class);

    public ApplicationManagerProviderServiceImpl(AppManagementConfig appManagementConfig,
            DeviceManagementPluginRepository pluginRepository) {

        IdentityConfigurations identityConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig().
                getDeviceManagementConfigRepository().getIdentityConfigurations();
        this.authenticator =
                new ServiceAuthenticator(identityConfig.getAdminUsername(), identityConfig.getAdminPassword());
        this.oAuthAdminServiceUrl =
                identityConfig.getServerUrl() + DeviceManagementConstants.AppManagement.OAUTH_ADMIN_SERVICE;
        try {
            this.configCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        } catch (AxisFault e) {
            throw new IllegalArgumentException("Error occurred while initializing Axis2 Configuration Context. " +
                    "Please check if an appropriate axis2.xml is provided", e);
        }
        this.pluginRepository = pluginRepository;
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
    }

    @Override
    public Application[] getApplications(String domain, int pageNumber, int size)
            throws ApplicationManagementException {
        return new Application[0];
    }

    @Override
    public void updateApplicationStatus(DeviceIdentifier deviceId, Application application,
                                        String status) throws ApplicationManagementException {

    }

    @Override
    public String getApplicationStatus(DeviceIdentifier deviceId,
            Application application) throws ApplicationManagementException {
        return null;
    }

    @Override
    public void installApplication(Operation operation, List<DeviceIdentifier> deviceIds)
            throws ApplicationManagementException {

        for (DeviceIdentifier deviceId : deviceIds) {
            DeviceManagementService dms =
                    this.getPluginRepository().getDeviceManagementService(deviceId.getType());
            dms.installApplication(operation, deviceIds);
        }
    }

    private OAuthConsumerAppDTO getAppInfo() throws ApplicationManagementException {
        OAuthConsumerAppDTO appInfo = null;
        try {
            OAuthAdminServiceStub oAuthAdminServiceStub =
                    new OAuthAdminServiceStub(configCtx, oAuthAdminServiceUrl);
            authenticator.authenticate(oAuthAdminServiceStub._getServiceClient());

            try {
                appInfo = oAuthAdminServiceStub.getOAuthApplicationDataByAppName(
                        DeviceManagementConstants.AppManagement.OAUTH_APPLICATION_NAME);
            }
            //application doesn't exist. Due to the way getOAuthApplicationDataByAppName has been
            //implemented, it throws an AxisFault if the App doesn't exist. Hence the catch.
            catch (AxisFault fault) {
                oAuthAdminServiceStub.registerOAuthApplicationData(this.getRequestDTO());
                appInfo = oAuthAdminServiceStub.getOAuthApplicationDataByAppName(
                        DeviceManagementConstants.AppManagement.OAUTH_APPLICATION_NAME);
            }
        } catch (RemoteException e) {
            handleException("Error occurred while retrieving app info", e);
        } catch (OAuthAdminServiceException e) {
            handleException("Error occurred while invoking OAuth admin service stub", e);
        }
        return appInfo;
    }

    private OAuthConsumerAppDTO getRequestDTO() {
        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(DeviceManagementConstants.AppManagement.OAUTH_APPLICATION_NAME);
        appDTO.setGrantTypes(
                DeviceManagementConstants.AppManagement.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS);
        appDTO.setOAuthVersion(DeviceManagementConstants.AppManagement.OAUTH_VERSION_2);
        return appDTO;
    }

    private void handleException(String msg, Exception e) throws ApplicationManagementException {
        log.error(msg, e);
        throw new ApplicationManagementException(msg, e);
    }

    public DeviceManagementPluginRepository getPluginRepository() {
        return pluginRepository;
    }

}
