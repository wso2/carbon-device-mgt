/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.analytics.data.publisher;

import org.w3c.dom.Document;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataPublisherUtil {

    private DataPublisherUtil(){
    }

    public static Document convertToDocument(File file) throws DataPublisherConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new DataPublisherConfigurationException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    public static ArrayList<String> getEndpointGroups(String urlSet) {
        ArrayList<String> urlGroups = new ArrayList<>();
        Pattern regex = Pattern.compile("\\{.*?\\}");
        Matcher regexMatcher = regex.matcher(urlSet);

        while(regexMatcher.find()) {
            urlGroups.add(regexMatcher.group().replace("{", "").replace("}", ""));
        }

        if (urlGroups.size() == 0) {
            urlGroups.add(urlSet.replace("{", "").replace("}", ""));
        }
        return urlGroups;
    }

    public static String[] getEndpoints(String aURLGroup) throws DataEndpointConfigurationException {
        boolean isLBURL = false;
        boolean isFailOverURL = false;
        if (aURLGroup.contains(",")) {
            isLBURL = true;
        }

        if (aURLGroup.contains("|")) {
            isFailOverURL = true;
        }

        if (isLBURL && isFailOverURL) {
            throw new DataEndpointConfigurationException("Invalid data endpoints URL set provided : " + aURLGroup +
                    ", a URL group can be configured as failover OR load balancing endpoints.");
        } else {
            String[] urls;
            if (isLBURL) {
                urls = aURLGroup.split(",");
            } else if (isFailOverURL) {
                urls = aURLGroup.split("\\|");
            } else {
                urls = new String[]{aURLGroup};
            }
            return urls;
        }
    }

    public static int obtainHashId(String deviceId, int urlGroupsCount) {
        byte[] chars = deviceId.getBytes();
        int sum = 0;
        for (byte b : chars) {
            sum += b;
        }
        return sum % urlGroupsCount;
    }

    @SuppressWarnings("Duplicates")
    public static String replaceProperty(String urlWithPlaceholders) {
        String regex = "\\$\\{(.*?)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matchPattern = pattern.matcher(urlWithPlaceholders);
        while (matchPattern.find()) {
            String sysPropertyName = matchPattern.group(1);
            String sysPropertyValue = System.getProperty(sysPropertyName);
            if (sysPropertyValue != null && !sysPropertyName.isEmpty()) {
                urlWithPlaceholders = urlWithPlaceholders.replaceAll("\\$\\{(" + sysPropertyName + ")\\}", sysPropertyValue);
            }
        }
        return urlWithPlaceholders;
    }

}
