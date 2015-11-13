package org.wso2.carbon.device.mgt.common.api.internal;

import org.wso2.carbon.base.ServerConfiguration;

public class IoTCommonDataHolder {

	private static IoTCommonDataHolder thisInstance = new IoTCommonDataHolder();
	String trustStoreLocaiton;
	String trustStorePassword;
	private IoTCommonDataHolder() {

	}

	public void initialize(){
		setTrustStore();
	}

	public static IoTCommonDataHolder getInstance() {
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
