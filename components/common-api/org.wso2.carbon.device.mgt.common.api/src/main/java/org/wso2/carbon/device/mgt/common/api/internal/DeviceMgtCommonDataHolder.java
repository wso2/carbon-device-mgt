package org.wso2.carbon.device.mgt.common.api.internal;

import org.wso2.carbon.base.ServerConfiguration;

public class DeviceMgtCommonDataHolder {

	private static DeviceMgtCommonDataHolder thisInstance = new DeviceMgtCommonDataHolder();
	String trustStoreLocaiton;
	String trustStorePassword;
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
}
