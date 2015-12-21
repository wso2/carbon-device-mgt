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

package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.webapp.publisher.KeyMgtInfo;
import org.wso2.carbon.apimgt.webapp.publisher.KeyMgtInfoUtil;

import java.lang.String;import java.util.List;


public class KeyGenerationUtil {
	private static final Log log = LogFactory.getLog(KeyGenerationUtil.class);

	public static void createApplicationKeys(String deviceType){

		APIConsumer apiConsumer = null;
		try {
			apiConsumer = APIManagerFactory.getInstance().getAPIConsumer("admin");

			List<KeyMgtInfo> allKeyInfo = KeyMgtInfoUtil.getInstance().getAllKeyInfo();

			for(KeyMgtInfo keyMgtInfo : allKeyInfo){
				if(keyMgtInfo.getApplicationName().contains(deviceType)) {
					apiConsumer.requestApprovalForApplicationRegistration(keyMgtInfo.getUserId(),
							keyMgtInfo.getApplicationName(), keyMgtInfo.getTokenType(), keyMgtInfo.getCallbackUrl(),
							keyMgtInfo.getAllowedDomains(), keyMgtInfo.getValidityTime(), keyMgtInfo.getTokenScope(),
							keyMgtInfo.getGroupingId(), keyMgtInfo.getJsonString());
				}
			}

		} catch (APIManagementException e) {
			/*The exception here is not exposed as there is no method in APIM to check
			if a given applicaion has a consumer key/secrete already.*/
			log.info("Consumer key/secret already exists for application : "+deviceType);
		}

	}

}
