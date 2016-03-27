package org.wso2.carbon.device.mgt.etc.util;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.etc.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.etc.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.etc.util.cdmdevice.util.IotDeviceManagementUtil;
import org.wso2.carbon.device.mgt.etc.config.server.DeviceCloudConfigManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ZipUtil {

	public ZipArchive downloadSketch(String owner, String tenantDomain, String deviceType,
	                                 String deviceId, String deviceName, String token,
	                                 String refreshToken)
			throws DeviceManagementException {

		if (owner == null || deviceType == null) {
			throw new DeviceManagementException("Invalid parameters for `owner` or `deviceType`");
		}

		String sep = File.separator;
		String sketchFolder = "repository" + sep + "resources" + sep + "sketches";
		String archivesPath = CarbonUtils.getCarbonHome() + sep + sketchFolder + sep + "archives"
				+ sep + deviceId;
		String templateSketchPath = sketchFolder + sep + deviceType;

//		String iotServerIP = System.getProperty("bind.address");
		String iotServerIP = System.getProperty("server.host");
		String httpsServerPort = System.getProperty("httpsPort");
		String httpServerPort = System.getProperty("httpPort");

		String httpsServerEP = "https://" + iotServerIP + ":" + httpsServerPort;
		String httpServerEP = "http://" + iotServerIP + ":" + httpServerPort;

		String apimHost =
				DeviceCloudConfigManager.getInstance().getDeviceCloudMgtConfig().getApiManager()
						.getServerURL();

//		int indexOfChar = apimIP.lastIndexOf(File.separator);
//		if (indexOfChar != -1) {
//			apimIP = apimIP.substring((indexOfChar + 1), apimIP.length());
//		}

		String apimGatewayPort =
				DeviceCloudConfigManager.getInstance().getDeviceCloudMgtConfig().getApiManager()
						.getGatewayPort();

		String apimEndpoint = apimHost + ":" + apimGatewayPort;

		String mqttEndpoint = MqttConfig.getInstance().getMqttQueueEndpoint();
//		indexOfChar = mqttEndpoint.lastIndexOf(File.separator);
//		if (indexOfChar != -1) {
//			mqttEndpoint = mqttEndpoint.substring((indexOfChar + 1), mqttEndpoint.length());
//		}

		String xmppEndpoint = XmppConfig.getInstance().getXmppEndpoint();
//		indexOfChar = xmppEndpoint.lastIndexOf(File.separator);
//		if (indexOfChar != -1) {
//			xmppEndpoint = xmppEndpoint.substring((indexOfChar + 1), xmppEndpoint.length());
//		}

		int indexOfChar = xmppEndpoint.lastIndexOf(":");
		if (indexOfChar != -1) {
			xmppEndpoint = xmppEndpoint.substring(0, indexOfChar);
		}

		xmppEndpoint = xmppEndpoint + ":" + XmppConfig.getInstance().getSERVER_CONNECTION_PORT();

		Map<String, String> contextParams = new HashMap<String, String>();
		contextParams.put("DEVICE_OWNER", owner);
		contextParams.put("DEVICE_ID", deviceId);
		contextParams.put("DEVICE_NAME", deviceName);
		contextParams.put("HTTPS_EP", httpsServerEP);
		contextParams.put("HTTP_EP", httpServerEP);
		contextParams.put("APIM_EP", apimEndpoint);
		contextParams.put("MQTT_EP", mqttEndpoint);
		contextParams.put("XMPP_EP", xmppEndpoint);
		contextParams.put("DEVICE_TOKEN", token);
		contextParams.put("DEVICE_REFRESH_TOKEN", refreshToken);

		ZipArchive zipFile;
		try {
			zipFile = IotDeviceManagementUtil.getSketchArchive(archivesPath, templateSketchPath,
			                                                   contextParams);
		} catch (IOException e) {
			throw new DeviceManagementException("Zip File Creation Failed", e);
		}

		return zipFile;
	}
}
