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

package org.wso2.carbon.device.mgt.common.impl.apimgt;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.common.impl.config.devicetype.DeviceTypeConfigurationManager;
import org.wso2.carbon.device.mgt.common.impl.config.devicetype.datasource.DeviceTypeConfig;
import org.wso2.carbon.device.mgt.common.impl.exception.DeviceMgtCommonsException;
import org.wso2.carbon.device.mgt.common.impl.config.server.DeviceCloudConfigManager;
import org.wso2.carbon.device.mgt.common.impl.config.server.datasource.ApiManagerConfig;
import org.wso2.carbon.device.mgt.common.impl.util.IoTUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ApisAppClient {

	private static ConcurrentHashMap<String, String> deviceTypeToApiAppMap = new ConcurrentHashMap<>();
	private static ApisAppClient instance =null;

	private String loginEndpoint;
	private String subscriptionListEndpoint;
	private static Log log = LogFactory.getLog(ApisAppClient.class);
	private boolean isEnabled;

	public static ApisAppClient getInstance(){

		if(instance==null){
			instance= new ApisAppClient();
		}
		return instance;
	}

	private ApisAppClient() {
		ApiManagerConfig apiManagerConfig =DeviceCloudConfigManager.getInstance().getDeviceCloudMgtConfig().getApiManager();
		String serverUrl=apiManagerConfig.getServerURL();
		String serverPort=apiManagerConfig.getServerPort();
		isEnabled = apiManagerConfig.isEnabled();

		String loginURL = serverUrl+":"+serverPort+apiManagerConfig.getLoginURL();
		loginEndpoint= loginURL+"?action=login&username="+apiManagerConfig.getUsername()
				+"&password="+apiManagerConfig.getPassword();

		String subscriptionListUrl=serverUrl+":"+serverPort+apiManagerConfig.getSubscriptionListURL();
		subscriptionListEndpoint=subscriptionListUrl+"?action=getAllSubscriptions";
	}

	public String getBase64EncodedConsumerKeyAndSecret(String deviceType) {
		if(!isEnabled) return null;
		String consumerKeyAndSecret = deviceTypeToApiAppMap.get(deviceType);
		if(consumerKeyAndSecret == null){
			ArrayList<DeviceTypeConfig> iotDeviceTypeConfigs = new ArrayList<>();
			DeviceTypeConfig DeviceTypeConfig = DeviceTypeConfigurationManager.getInstance().getDeviceTypeConfigMap().get(deviceType);
			if(DeviceTypeConfig != null) {
				iotDeviceTypeConfigs.add(DeviceTypeConfig);
				setBase64EncodedConsumerKeyAndSecret(iotDeviceTypeConfigs);
				consumerKeyAndSecret = deviceTypeToApiAppMap.get(deviceType);
				if(consumerKeyAndSecret==null){
					log.warn("There is no API application for the device type " + deviceType);
				}
			}
		}
		return  consumerKeyAndSecret;
	}

	public void setBase64EncodedConsumerKeyAndSecret(List<DeviceTypeConfig> iotDeviceTypeConfigList) {
		if(!isEnabled) return;

		URL loginURL = null;
		try {
			loginURL = new URL(loginEndpoint);
		} catch (MalformedURLException e) {
			String errMsg = "Malformed URL " + loginEndpoint;
			log.error(errMsg);
			return;
		}
		HttpClient httpClient = null;
		try {
			httpClient = IoTUtil.getHttpClient(loginURL.getPort(), loginURL.getProtocol());
		} catch (Exception e) {
			log.error("Error on getting a http client for port :" + loginURL.getPort() + " protocol :"
							  + loginURL.getProtocol());
			return;
		}

		HttpPost postMethod = new HttpPost(loginEndpoint);
		JSONObject apiJsonResponse;
		try {
			HttpResponse httpResponse = httpClient.execute(postMethod);
			String response = IoTUtil.getResponseString(httpResponse);
			if(log.isDebugEnabled()) {
				log.debug(response);
			}
			JSONObject jsonObject = new JSONObject(response);


			boolean apiError = jsonObject.getBoolean("error");
			if(!apiError){
				String cookie = httpResponse.getHeaders("Set-Cookie")[0].getValue().split(";")[0];
				HttpGet getMethod=new HttpGet(subscriptionListEndpoint);
				getMethod.setHeader("cookie", cookie);
				httpResponse = httpClient.execute(getMethod);
				response = IoTUtil.getResponseString(httpResponse);


				if(log.isDebugEnabled()) {
					log.debug(response);
				}
				apiJsonResponse = new JSONObject(response);
				apiError=apiJsonResponse.getBoolean("error");
				if(apiError){
					log.error("invalid subscription endpoint "+subscriptionListEndpoint);
					return;
				}
			}else{
				log.error("invalid access for login endpoint " +loginEndpoint);
				return;
			}

		} catch (IOException | JSONException | DeviceMgtCommonsException e) {
			log.warn("Trying to connect to the Api manager");
			return;
		}


        try {
            JSONArray jsonSubscriptions = apiJsonResponse.getJSONObject("subscriptions").getJSONArray("applications");

            HashMap<String, String> subscriptionMap = new HashMap<>();
            for (int n = 0; n < jsonSubscriptions.length(); n++) {

                JSONObject object = jsonSubscriptions.getJSONObject(n);
                String appName = object.getString("name");
                String prodConsumerKey = object.getString("prodConsumerKey");
                String prodConsumerSecret = object.getString("prodConsumerSecret");
                subscriptionMap.put(appName, new String(Base64.encodeBase64(
						(prodConsumerKey + ":" + prodConsumerSecret).getBytes())));
            }

            for (DeviceTypeConfig iotDeviceTypeConfig : iotDeviceTypeConfigList) {
                String deviceType = iotDeviceTypeConfig.getType();
                String deviceTypeApiApplicationName = iotDeviceTypeConfig.getApiApplicationName();
                String base64EncodedString = subscriptionMap.get(deviceTypeApiApplicationName);
                if (base64EncodedString != null && base64EncodedString.length() != 0) {
                    deviceTypeToApiAppMap.put(deviceType, base64EncodedString);
                }
            }

        } catch (JSONException e) {
            log.error("Json exception: " + e.getMessage(), e);
        }


	}
}
