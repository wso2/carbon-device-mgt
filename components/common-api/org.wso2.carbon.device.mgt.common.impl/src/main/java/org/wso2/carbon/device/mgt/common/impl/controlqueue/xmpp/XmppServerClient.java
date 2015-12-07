/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.impl.controlqueue.xmpp;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.device.mgt.common.impl.controlqueue.ControlQueueConnector;
import org.wso2.carbon.device.mgt.common.impl.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.common.impl.exception.DeviceMgtCommonsException;
import org.wso2.carbon.device.mgt.common.impl.util.IoTUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class XmppServerClient implements ControlQueueConnector {

	private static final Log log = LogFactory.getLog(XmppServerClient.class);

	private static final String XMPP_SERVER_API_CONTEXT = "/plugins/restapi/v1";
	private static final String USERS_API = "/users";
	private static final String GROUPS_API = "/groups";
	private static final String APPLICATION_JSON_MT = "application/json";

	private String xmppEndpoint;
	private String xmppUsername;
	private String xmppPassword;
	private boolean xmppEnabled = false;

	public XmppServerClient() {
	}

	@Override
	public void initControlQueue() throws DeviceControllerException {
		xmppEndpoint = XmppConfig.getInstance().getXmppEndpoint();
		xmppUsername = XmppConfig.getInstance().getXmppUsername();
		xmppPassword = XmppConfig.getInstance().getXmppPassword();
		xmppEnabled = XmppConfig.getInstance().isEnabled();
	}

	@Override
	public void enqueueControls(HashMap<String, String> deviceControls)
			throws DeviceControllerException {
		if (!xmppEnabled) {
			log.warn("XMPP <Enabled> set to false in 'device-mgt-config.xml'");
		}
	}

	public boolean createXMPPAccount(XmppAccount newUserAccount) throws DeviceControllerException {
		if (xmppEnabled) {
			String xmppUsersAPIEndpoint = xmppEndpoint + XMPP_SERVER_API_CONTEXT + USERS_API;
			if (log.isDebugEnabled()) {
				log.debug("The API Endpoint URL of the XMPP Server is set to: " +
								  xmppUsersAPIEndpoint);
			}

			String encodedString = xmppUsername + ":" + xmppPassword;
			encodedString = new String(Base64.encodeBase64(encodedString.getBytes(StandardCharsets.UTF_8)));
			String authorizationHeader = "Basic " + encodedString;
 			String jsonRequest ="{\n" +
					"    \"username\": \""+newUserAccount.getUsername()+"\"," +
					"    \"password\": \""+newUserAccount.getPassword()+"\"," +
					"    \"name\": \""+newUserAccount.getAccountName()+"\"," +
					"    \"email\": \""+newUserAccount.getEmail()+"\"," +
					"    \"properties\": {" +
					"        \"property\": [" +
					"            {" +
					"                \"@key\": \"console.rows_per_page\"," +
					"                \"@value\": \"user-summary=8\"" +
					"            }," +
					"            {" +
					"                \"@key\": \"console.order\"," +
					"                \"@value\": \"session-summary=1\"" +
					"            }" +
					"        ]" +
					"    }" +
					"}";
			StringEntity requestEntity;
			try {
				requestEntity = new StringEntity(jsonRequest,"application/json","UTF-8");
			} catch (UnsupportedEncodingException e) {
				return false;
			}

			URL xmppUserApiUrl;
			try {
				xmppUserApiUrl = new URL(xmppUsersAPIEndpoint);
			} catch (MalformedURLException e) {
				String errMsg = "Malformed URL + " + xmppUsersAPIEndpoint;
				log.error(errMsg);
				throw new DeviceControllerException(errMsg);
			}
			HttpClient httpClient;
			try {
				httpClient = IoTUtil.getHttpClient(xmppUserApiUrl.getPort(), xmppUserApiUrl.getProtocol());
			} catch (Exception e) {
				log.error("Error on getting a http client for port :" + xmppUserApiUrl.getPort() + " protocol :"
								  + xmppUserApiUrl.getProtocol());
				return false;
			}
			HttpPost httpPost = new HttpPost(xmppUsersAPIEndpoint);

			httpPost.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
			httpPost.setEntity(requestEntity);



			try {
				HttpResponse httpResponse = httpClient.execute(httpPost);

				if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
					String response = IoTUtil.getResponseString(httpResponse);
					String errorMsg = "XMPP Server returned status: '" + httpResponse.getStatusLine().getStatusCode() +
									"' for account creation with error:\n" + response;
					log.error(errorMsg);
					throw new DeviceControllerException(errorMsg);
				} else {
					EntityUtils.consume(httpResponse.getEntity());
					return true;
				}
			} catch (IOException | DeviceMgtCommonsException e) {
				String errorMsg =
						"Error occurred whilst trying a 'POST' at : " + xmppUsersAPIEndpoint + " error: " + e.getMessage();
				log.error(errorMsg);
				throw new DeviceControllerException(errorMsg, e);
			}

		} else {
			log.warn("XMPP <Enabled> set to false in 'device-mgt-config.xml'");
			return false;
		}
	}
}
