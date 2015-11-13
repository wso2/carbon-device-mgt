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

package org.wso2.carbon.device.mgt.common.api.config.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.api.config.server.datasource.ControlQueue;
import org.wso2.carbon.device.mgt.common.api.config.server.datasource.DataStore;
import org.wso2.carbon.device.mgt.common.api.config.server.datasource.DeviceCloudConfig;
import org.wso2.carbon.device.mgt.common.api.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.common.api.util.iotdevice.util.IotDeviceManagementUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.List;

/**
 * Class responsible for the iot device manager configuration initialization.
 */
public class DeviceCloudConfigManager {
    private static final Log log = LogFactory.getLog(DeviceCloudConfigManager.class);

    private static final String IOT_DEVICE_CONFIG_XML_NAME = "devicecloud-config.xml";
	private static final String IOT_DC_ROOT_DIRECTORY = "iot";
	private final String XMLCONFIGS_FILE_LOCATION =
			CarbonUtils.getCarbonConfigDirPath() + File.separator +
                    IOT_DC_ROOT_DIRECTORY + File.separator + IOT_DEVICE_CONFIG_XML_NAME;

    private static final String IOT_DEVICE_CONFIG_XSD_NAME = "devicecloud-config.xsd";
    private final String XSDCONFIGS_FILE_LOCATION =
            CarbonUtils.getCarbonConfigDirPath() + File.separator +
                    IOT_DC_ROOT_DIRECTORY + File.separator + IOT_DEVICE_CONFIG_XSD_NAME;

    private DeviceCloudConfig currentDeviceCloudConfig;
    private static DeviceCloudConfigManager
            deviceConfigurationManager = new DeviceCloudConfigManager();

    private DeviceCloudConfigManager() {
    }

    public static DeviceCloudConfigManager getInstance()  {
        return deviceConfigurationManager;
    }

    public void initConfig() throws DeviceControllerException {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(new File(XSDCONFIGS_FILE_LOCATION));

            File deviceCloudMgtConfig = new File(XMLCONFIGS_FILE_LOCATION);
            Document doc = IotDeviceManagementUtil.convertToDocument(deviceCloudMgtConfig);
            JAXBContext deviceCloudContext = JAXBContext.newInstance(DeviceCloudConfig.class);
            Unmarshaller unmarshaller = deviceCloudContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(new IotConfigValidationEventHandler());
            this.currentDeviceCloudConfig = (DeviceCloudConfig) unmarshaller.unmarshal(doc);
        } catch (Exception e) {
            String error = "Error occurred while initializing DeviceController configurations";
            log.error(error);
            throw new DeviceControllerException(error, e);
        }
    }

    public DeviceCloudConfig getDeviceCloudMgtConfig() {
        return currentDeviceCloudConfig;
    }

    public DataStore getDataStore(String name){
        List<DataStore>  dataStores= currentDeviceCloudConfig.getDataStores().getDataStore();
        if(dataStores!=null) {
            for (DataStore dataStore : dataStores) {
                if (dataStore.getName().equals(name)) {
                    return dataStore;

                }


            }
        }
        return null;
    }

    public ControlQueue getControlQueue(String name){
        List<ControlQueue> controlQueues= currentDeviceCloudConfig.getControlQueues().getControlQueue();
        if(controlQueues!=null) {
            for (ControlQueue controlQueue : controlQueues) {
                if (controlQueue.getName().equals(name)) {
                    return controlQueue;

                }
            }
        }
        return null;
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
