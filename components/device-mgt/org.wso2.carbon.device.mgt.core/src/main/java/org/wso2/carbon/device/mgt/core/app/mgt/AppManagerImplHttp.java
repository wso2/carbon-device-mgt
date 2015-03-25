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
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.device.mgt.core.app.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.app.mgt.oauth.ServiceAuthenticator;
import org.wso2.carbon.device.mgt.core.app.mgt.oauth.dto.Credential;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.identity.IdentityConfigurations;
import org.wso2.carbon.device.mgt.core.dto.Application;
import org.wso2.carbon.device.mgt.core.service.AppManager;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

import java.rmi.RemoteException;

/**
 * Implements AppManager interface
 */
public class AppManagerImplHttp implements AppManager {

	private static Log log = LogFactory.getLog(AppManagerImplHttp.class);
	private static AppManagementConfig appManagementConfig;
	private static final String GET_APP_LIST_URL =
			"store/apis/assets/mobileapp?domain=carbon.super&page=1";
	private static String appManagerUrl;
	private static String consumerKey;
	private static String consumerSecret;

	public AppManagerImplHttp(AppManagementConfig appManagementConfig) {
		this.appManagementConfig = appManagementConfig;
		this.appManagerUrl = appManagementConfig.getAppManagerUrl();
		this.consumerKey = appManagementConfig.getConsumerKey();
		this.consumerSecret = appManagementConfig.getConsumerSecret();
	}

	@Override
	public Application[] getApplicationList(String domain, int pageNumber, int size)
			throws AppManagementException {
		return new Application[0];
	}

	@Override public void updateApplicationStatusOnDevice(DeviceIdentifier deviceId,
	                                                      Application application, String status) {

	}

	@Override public String getApplicationStatusOnDevice(DeviceIdentifier deviceId,
	                                                     Application application) {
		return null;
	}

	@Override public Credential getClientCredentials() throws AppManagementException {
		OAuthAdminServiceStub oAuthAdminServiceStub;
		OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
		appDTO.setApplicationName(DeviceManagementConstants.AppManagement.OAUTH_APPLICATION_NAME);
		appDTO.setGrantTypes(
				DeviceManagementConstants.AppManagement.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS);
		appDTO.setOAuthVersion(DeviceManagementConstants.AppManagement.OAUTH_VERSION_2);
		IdentityConfigurations identityConfigurations =
				DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
				                          .getDeviceManagementConfigRepository()
				                          .getIdentityConfigurations();
		String serverUrl = identityConfigurations.getServerUrl();
		String username = identityConfigurations.getAdminUsername();
		String password = identityConfigurations.getAdminPassword();
		String oauthAdminServiceUrl = serverUrl +
		                              DeviceManagementConstants.AppManagement.OAUTH_ADMIN_SERVICE;

		try {
			ConfigurationContext configContext = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(null, null);
			oAuthAdminServiceStub = new OAuthAdminServiceStub(configContext, oauthAdminServiceUrl);

			ServiceAuthenticator authenticator = ServiceAuthenticator.getInstance();
			authenticator.setAccessUsername(username);
			authenticator.setAccessPassword(password);
			authenticator.authenticate(oAuthAdminServiceStub._getServiceClient());

			OAuthConsumerAppDTO createdAppData = null;
			try {
				createdAppData = oAuthAdminServiceStub.getOAuthApplicationDataByAppName(
						DeviceManagementConstants.AppManagement.OAUTH_APPLICATION_NAME);
			}
			//application doesn't exist. Due to the way getOAuthApplicationDataByAppName has been
			//implemented, it throws an AxisFault if the App doesn't exist. Hence the catch.
			catch (AxisFault fault) {
				oAuthAdminServiceStub.registerOAuthApplicationData(appDTO);
				createdAppData = oAuthAdminServiceStub.getOAuthApplicationDataByAppName(
						DeviceManagementConstants.AppManagement.OAUTH_APPLICATION_NAME);
			}

			Credential credential = new Credential();
			credential.setConsumerKey(createdAppData.getOauthConsumerKey());
			credential.setConsumerSecret(createdAppData.getOauthConsumerSecret());
			return credential;
		} catch (RemoteException e) {
			String msg = "Error while registering a new application.";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		} catch (OAuthAdminServiceException e) {
			String msg = "Error while working with oauth admin services stub.";
			log.error(msg, e);
			throw new AppManagementException(msg, e);
		}
	}

}
