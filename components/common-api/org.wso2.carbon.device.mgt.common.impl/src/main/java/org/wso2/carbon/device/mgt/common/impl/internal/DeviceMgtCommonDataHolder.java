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

package org.wso2.carbon.device.mgt.common.impl.internal;

import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;

public class DeviceMgtCommonDataHolder {

	private static DeviceMgtCommonDataHolder thisInstance = new DeviceMgtCommonDataHolder();
	String trustStoreLocaiton;
	String trustStorePassword;

    private static DataBridgeReceiverService dataBridgeReceiverService;

	private DeviceMgtCommonDataHolder() {

	}

	public void initialize(){
		setTrustStore();
	}

	public static DeviceMgtCommonDataHolder getInstance() {
		return thisInstance;
	}

	private  void setTrustStore(){
		this.trustStoreLocaiton = ServerConfiguration.getInstance().getFirstProperty("Security.TrustStore.Location");
		this.trustStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.TrustStore.Password");
	}

	public String getTrustStoreLocation(){
		return trustStoreLocaiton;
	}

	public String getTrustStorePassword(){
		return trustStorePassword;
	}

    public static DataBridgeReceiverService getDataBridgeReceiverService() {
        return dataBridgeReceiverService;
    }

    public static void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        DeviceMgtCommonDataHolder.dataBridgeReceiverService = dataBridgeReceiverService;
    }
}
