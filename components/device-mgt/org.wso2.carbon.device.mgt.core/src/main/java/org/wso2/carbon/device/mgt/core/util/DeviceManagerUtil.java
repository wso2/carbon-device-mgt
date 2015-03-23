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
package org.wso2.carbon.device.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.api.mgt.APIConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public final class DeviceManagerUtil {

    private static final Log log = LogFactory.getLog(DeviceManagerUtil.class);

    enum HTTPMethod {
        GET, POST, DELETE, PUT, OPTIONS
    }

    private static List<HTTPMethod> httpMethods;

    static {
        httpMethods = new ArrayList<HTTPMethod>();
        httpMethods.add(HTTPMethod.GET);
        httpMethods.add(HTTPMethod.POST);
        httpMethods.add(HTTPMethod.DELETE);
        httpMethods.add(HTTPMethod.PUT);
        httpMethods.add(HTTPMethod.OPTIONS);
    }

    public static Document convertToDocument(File file) throws DeviceManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new DeviceManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    /**
     * Resolve data source from the data source definition.
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException("Device Management Repository data source configuration is null and thus, " +
                    "is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    /**
     * Adds a new device type to the database if it does not exists.
     *
     * @param deviceType device type
     * @return status of the operation
     */
    public static boolean registerDeviceType(String deviceType) throws DeviceManagementException {
        boolean status;
        try {
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            DeviceType deviceTypeId = deviceTypeDAO.getDeviceType(deviceType);
            if (deviceTypeId == null) {
                DeviceType dt = new DeviceType();
                dt.setName(deviceType);
                deviceTypeDAO.addDeviceType(dt);
            }
            status = true;
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while registering the device type '" +
                    deviceType + "'", e);
        }
        return status;
    }

    /**
     * Un-registers an existing device type from the device management metadata repository.
     *
     * @param deviceType device type
     * @return status of the operation
     */
    public static boolean unregisterDeviceType(String deviceType) throws DeviceManagementException {
        try {
            DeviceTypeDAO deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
            DeviceType deviceTypeId = deviceTypeDAO.getDeviceType(deviceType);
            if (deviceTypeId == null) {
                DeviceType dt = new DeviceType();
                dt.setName(deviceType);
                deviceTypeDAO.removeDeviceType(deviceType);
            }
            return true;
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while registering the device type '" +
                    deviceType + "'", e);
        }
    }

    public static Map<String, String> convertDevicePropertiesToMap(List<Device.Property> properties) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        for (Device.Property prop : properties) {
            propertiesMap.put(prop.getName(), prop.getValue());
        }
        return propertiesMap;
    }

    public static int getTenantId() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        return ctx.getTenantId();
    }

    public static void publishAPI(APIConfig config) throws DeviceManagementException {
        APIProvider provider = config.getProvider();
        APIIdentifier id = new APIIdentifier(config.getOwner(), config.getName(), config.getVersion());
        API api = new API(id);
        try {
            api.setContext(config.getContext());
            api.setUrl(config.getVersion());
            api.setUriTemplates(getURITemplates(config.getEndpoint(),
                    APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN));
            api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
            api.addAvailableTiers(provider.getTiers());
            api.setEndpointSecured(false);
            api.setStatus(APIStatus.PUBLISHED);
            api.setTransports(config.getTransports());

            provider.addAPI(api);
        } catch (APIManagementException e) {
            throw new DeviceManagementException("Error occurred while registering the API", e);
        }
    }

    public static void removeAPI(APIConfig config) throws DeviceManagementException {
        try {
            APIProvider provider = config.getProvider();
            APIIdentifier id = new APIIdentifier(config.getOwner(), config.getName(), config.getVersion());
            provider.deleteAPI(id);
        } catch (APIManagementException e) {
            throw new DeviceManagementException("Error occurred while removing API", e);
        }
    }

    private static Set<URITemplate> getURITemplates(String endpoint, String authType) {
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        if (APIConstants.AUTH_NO_AUTHENTICATION.equals(authType)) {
            for (HTTPMethod method : httpMethods) {
                URITemplate template = new URITemplate();
                template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                template.setHTTPVerb(method.toString());
                template.setResourceURI(endpoint);
                template.setUriTemplate("/*");
                uriTemplates.add(template);
            }
        } else {
            for (HTTPMethod method : httpMethods) {
                URITemplate template = new URITemplate();
                if (HTTPMethod.OPTIONS.equals(method)) {
                    template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                } else {
                    template.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                }
                template.setHTTPVerb(method.toString());
                template.setResourceURI(endpoint);
                template.setUriTemplate("/*");
                uriTemplates.add(template);
            }
        }
        return uriTemplates;
    }

}
