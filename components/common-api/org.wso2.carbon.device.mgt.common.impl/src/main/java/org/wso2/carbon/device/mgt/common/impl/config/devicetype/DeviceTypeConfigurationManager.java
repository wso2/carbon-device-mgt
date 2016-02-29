/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.impl.config.devicetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.impl.util.cdmdevice.util.IotDeviceManagementUtil;
import org.wso2.carbon.device.mgt.common.impl.apimgt.ApisAppClient;
import org.wso2.carbon.device.mgt.common.impl.config.devicetype.datasource.DeviceTypeConfigManager;
import org.wso2.carbon.device.mgt.common.impl.config.devicetype.datasource.DeviceTypeConfig;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for the iot device manager configuration initialization.
 */
public class DeviceTypeConfigurationManager {
	private static final Log log = LogFactory.getLog(DeviceTypeConfigurationManager.class);

	private static final String DEVICE_TYPE_CONFIG_XML_NAME = "devicetype-config.xml";
	private static final String DEVICE_TYPE_CONFIG_XSD_NAME = "devicetype-config.xsd";
	private DeviceTypeConfigManager currentDeviceTypeConfig;
	private static DeviceTypeConfigurationManager
			deviceConfigManager = new DeviceTypeConfigurationManager();

	private final String deviceMgtConfigXMLPath = CarbonUtils.getCarbonConfigDirPath()
			+ File.separator + DEVICE_TYPE_CONFIG_XML_NAME;

	private final String deviceMgtConfigXSDPath = CarbonUtils.getCarbonConfigDirPath()
			+ File.separator + DEVICE_TYPE_CONFIG_XSD_NAME;

	private HashMap<String,DeviceTypeConfig> deviceTypeConfigMap = new HashMap<>();

	public static DeviceTypeConfigurationManager getInstance() {
		return deviceConfigManager;
	}

	public synchronized void initConfig() throws DeviceManagementException {
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new File(deviceMgtConfigXSDPath));

			File iotDeviceMgtConfig = new File(deviceMgtConfigXMLPath);
			Document doc = IotDeviceManagementUtil.convertToDocument(iotDeviceMgtConfig);
			JAXBContext iotDeviceMgmtContext = JAXBContext.newInstance(DeviceTypeConfigManager.class);
			Unmarshaller unmarshaller = iotDeviceMgmtContext.createUnmarshaller();
			unmarshaller.setSchema(schema);
			unmarshaller.setEventHandler(new IotConfigValidationEventHandler());
			this.currentDeviceTypeConfig = (DeviceTypeConfigManager) unmarshaller.unmarshal(doc);

			List<DeviceTypeConfig> iotDeviceTypeConfigList= currentDeviceTypeConfig.getDeviceTypeConfigs();
			for(DeviceTypeConfig iotDeviceTypeConfig:iotDeviceTypeConfigList){
				String applicationName=iotDeviceTypeConfig.getApiApplicationName();

				if(applicationName==null||applicationName.isEmpty()){
					iotDeviceTypeConfig.setApiApplicationName(iotDeviceTypeConfig.getType());
				}
				deviceTypeConfigMap.put(iotDeviceTypeConfig.getType(), iotDeviceTypeConfig);
			}
			ApisAppClient.getInstance().setBase64EncodedConsumerKeyAndSecret(iotDeviceTypeConfigList);
		} catch (Exception e) {
			String error = "Error occurred while initializing device configurations";
			log.error(error, e);
		}
	}

	public DeviceTypeConfigManager getDeviceManagementConfig() {
		return currentDeviceTypeConfig;
	}


	public Map<String,DeviceTypeConfig> getDeviceTypeConfigMap(){
		return Collections.unmodifiableMap(deviceTypeConfigMap);
	}


	private class IotConfigValidationEventHandler implements ValidationEventHandler {
		@Override
		public boolean handleEvent(ValidationEvent event) {
			String error= "\nEVENT" +"\nSEVERITY:  " + event.getSeverity()
					+ "\nMESSAGE:  " + event.getMessage()
					+"\nLINKED EXCEPTION:  " + event.getLinkedException()
					+"\nLOCATOR"
					+"\n    LINE NUMBER:  " + event.getLocator().getLineNumber()
					+"\n    COLUMN NUMBER:  " + event.getLocator().getColumnNumber()
					+"\n    OFFSET:  " + event.getLocator().getOffset()
					+"\n    OBJECT:  " + event.getLocator().getObject()
					+"\n    NODE:  " + event.getLocator().getNode()
					+"\n    URL:  " + event.getLocator().getURL();


			log.error(error);
			return true;
		}
	}

}
