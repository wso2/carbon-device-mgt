package org.wso2.carbon.device.mgt.common.api.util;

import java.io.File;

public class ZipArchive {
	private File zipFile = null;
	private String fileName = null;
	private String deviceId = null;

	public ZipArchive(String fileName, File zipFile) {
		this.fileName = fileName;
		this.zipFile = zipFile;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public File getZipFile() {
		return zipFile;
	}

	public String getFileName() {
		return fileName;
	}
}
