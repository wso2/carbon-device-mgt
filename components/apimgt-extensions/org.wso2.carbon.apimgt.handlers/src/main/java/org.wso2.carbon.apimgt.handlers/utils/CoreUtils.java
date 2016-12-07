/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.handlers.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class CoreUtils {
    private static final Log log = LogFactory.getLog(CoreUtils.class);
    private static String host = "localhost";
    private static int httpsPort = 9443;
    private static String username = "admin";
    private static String password = "admin";

    /**
     * Reading configurations from api-filter-config.xml file
     *
     * @return ArrayList of api contexts
     */
    public static ArrayList<String> readApiFilterList() {
        ArrayList<String> apiList = new ArrayList<String>();
        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        String apiFilterConfigPath = carbonConfigDirPath + File.separator +
                AuthConstants.AUTH_CONFIGURATION_FILE_NAME;
        File configFile = new File(apiFilterConfigPath);

        try {
            String configContent = FileUtils.readFileToString(configFile);
            OMElement configElement = AXIOMUtil.stringToOM(configContent);
            Iterator beans = configElement.getChildrenWithName(
                    new QName("http://www.springframework.org/schema/beans", "bean"));

            while (beans.hasNext()) {
                OMElement bean = (OMElement) beans.next();
                String beanId = bean.getAttributeValue(new QName(null, "id"));
                if (beanId.equals(AuthConstants.API_FILTER_CONFIG_ELEMENT)) {
                    Iterator beanProps = bean.getChildrenWithName(
                            new QName("http://www.springframework.org/schema/beans", "property"));

                    while (beanProps.hasNext()) {
                        OMElement beanProp = (OMElement) beanProps.next();
                        String beanName = beanProp.getAttributeValue(new QName(null, "name"));
                        if (AuthConstants.API_LIST_PROPERTY.equals(beanName)) {
                            Iterator apiListSet = ((OMElement) beanProp.getChildrenWithLocalName("set").next())
                                    .getChildrenWithLocalName("value");
                            while (apiListSet.hasNext()) {
                                String apiContext = ((OMElement) apiListSet.next()).getText();
                                apiList.add(apiContext);
                                CoreUtils.debugLog(log, "Adding security to api: ", apiContext);
                            }
                        } else if (AuthConstants.HOST.equals(beanName)) {
                            String value = beanProp.getAttributeValue(new QName(null, "value"));
                            host = value;
                        } else if (AuthConstants.HTTPS_PORT.equals(beanName)) {
                            String value = beanProp.getAttributeValue(new QName(null, "value"));
                            if (value != null && !value.trim().equals("")) {
                                httpsPort = Integer.parseInt(value);
                            }
                        } else if (AuthConstants.USERNAME.equals(beanName)) {
                            String value = beanProp.getAttributeValue(new QName(null, "value"));
                            username = value;
                        } else if (AuthConstants.PASSWORD.equals(beanName)) {
                            String value = beanProp.getAttributeValue(new QName(null, "value"));
                            password = value;
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error in reading api filter settings", e);
        } catch (XMLStreamException e) {
            log.error("Error in reading api filter settings", e);
        }
        return apiList;
    }

    /**
     * Universal debug log function
     *
     * @param logger Log object specific to the class
     * @param message initial debug log message
     * @param vars optional strings to be appended for the log
     */
    public static void debugLog(Log logger, String message, Object ... vars) {
        if(logger.isDebugEnabled()) {
            if (vars.length < 1) {
                logger.debug(message);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(message);
            for (Object var : vars) {
                stringBuilder.append(var.toString());
            }
            logger.debug(stringBuilder.toString());
        }
    }

    public static String getHost() {
        return host;
    }

    public static int getHttpsPort() {
        return httpsPort;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }
}
