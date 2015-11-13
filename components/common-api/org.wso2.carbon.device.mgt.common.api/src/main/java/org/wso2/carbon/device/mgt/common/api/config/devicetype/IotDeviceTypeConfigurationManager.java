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

package org.wso2.carbon.device.mgt.common.api.config.devicetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.api.util.iotdevice.util.IotDeviceManagementUtil;
import org.wso2.carbon.device.mgt.common.api.apimgt.ApisAppClient;
import org.wso2.carbon.device.mgt.common.api.config.devicetype.datasource.IoTDeviceTypeConfigManager;
import org.wso2.carbon.device.mgt.common.api.config.devicetype.datasource.IotDeviceTypeConfig;
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
public class IotDeviceTypeConfigurationManager {
	private static final Log log = LogFactory.getLog(IotDeviceTypeConfigurationManager.class);

	private static final String IOT_DEVICE_CONFIG_XML_NAME = "iot-config.xml";
	private static final String IOT_DEVICE_CONFIG_XSD_NAME = "iot-config.xsd";
	private static final String IOT_DC_ROOT_DIRECTORY = "iot";
	private IoTDeviceTypeConfigManager currentIoTDeviceTypeConfig;
	private static IotDeviceTypeConfigurationManager
			iotDeviceConfigManager = new IotDeviceTypeConfigurationManager();

	private final String iotDeviceMgtConfigXMLPath = CarbonUtils.getCarbonConfigDirPath()
			+ File.separator +
			IOT_DC_ROOT_DIRECTORY + File.separator + IOT_DEVICE_CONFIG_XML_NAME;

	private final String iotDeviceMgtConfigXSDPath = CarbonUtils.getCarbonConfigDirPath()
			+ File.separator +
			IOT_DC_ROOT_DIRECTORY + File.separator + IOT_DEVICE_CONFIG_XSD_NAME;

	HashMap<String,IotDeviceTypeConfig> iotDeviceTypeConfigMap = new HashMap<String,IotDeviceTypeConfig>();

	public static IotDeviceTypeConfigurationManager getInstance() {
		return iotDeviceConfigManager;
	}

	public synchronized void initConfig() throws DeviceManagementException {


		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new File(iotDeviceMgtConfigXSDPath));

			File iotDeviceMgtConfig = new File(iotDeviceMgtConfigXMLPath);
			Document doc = IotDeviceManagementUtil.convertToDocument(iotDeviceMgtConfig);
			JAXBContext iotDeviceMgmtContext = JAXBContext.newInstance(IoTDeviceTypeConfigManager.class);
			Unmarshaller unmarshaller = iotDeviceMgmtContext.createUnmarshaller();
			unmarshaller.setSchema(schema);
			unmarshaller.setEventHandler(new IotConfigValidationEventHandler());
			this.currentIoTDeviceTypeConfig = (IoTDeviceTypeConfigManager) unmarshaller.unmarshal(doc);

			List<IotDeviceTypeConfig> iotDeviceTypeConfigList=currentIoTDeviceTypeConfig.getIotDeviceTypeConfig();
			for(IotDeviceTypeConfig iotDeviceTypeConfig:iotDeviceTypeConfigList){
				String applicationName=iotDeviceTypeConfig.getApiApplicationName();

				if(applicationName==null||applicationName.isEmpty()){
					iotDeviceTypeConfig.setApiApplicationName(iotDeviceTypeConfig.getType());
				}
				iotDeviceTypeConfigMap.put(iotDeviceTypeConfig.getType(), iotDeviceTypeConfig);


			}
			ApisAppClient.getInstance().setBase64EncodedConsumerKeyAndSecret(iotDeviceTypeConfigList);


		} catch (Exception e) {
			String error = "Error occurred while initializing device configurations";
			log.error(error);
		}
	}

	public IoTDeviceTypeConfigManager getIotDeviceManagementConfig() {
		return currentIoTDeviceTypeConfig;
	}


	public Map<String,IotDeviceTypeConfig> getIotDeviceTypeConfigMap(){



		return Collections.unmodifiableMap(iotDeviceTypeConfigMap);
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
