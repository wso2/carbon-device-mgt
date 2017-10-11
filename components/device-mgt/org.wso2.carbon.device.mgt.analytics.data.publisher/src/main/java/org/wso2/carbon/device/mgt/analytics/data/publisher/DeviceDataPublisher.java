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

import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.config.AnalyticsConfiguration;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;

/**
 * This is used to manage data publisher per tenant.
 */
public class DeviceDataPublisher {

    private DataPublisher dataPublisher;
    private static DeviceDataPublisher deviceDataPublisher;

    public static DeviceDataPublisher getInstance() {
        if (deviceDataPublisher == null) {
            synchronized (DeviceDataPublisher.class) {
                if (deviceDataPublisher == null) {
                    deviceDataPublisher = new DeviceDataPublisher();
                }
            }
        }
        return deviceDataPublisher;
    }

    /**
     * this return the data publisher for the tenant.
     *
     * @return instance of data publisher
     * @throws DataPublisherConfigurationException
     *
     */
    public DataPublisher getDataPublisher() throws DataPublisherConfigurationException {
        if (this.dataPublisher == null) {
            synchronized (this) {
                if (this.dataPublisher == null) {
                    AnalyticsConfiguration analyticsConfig = AnalyticsConfiguration.getInstance();
                    if (!analyticsConfig.isEnable()) {
                        return null;
                    }
                    String analyticsServerUrlGroups = analyticsConfig.getReceiverServerUrl();
                    String analyticsServerUsername = analyticsConfig.getAdminUsername();
                    String analyticsServerPassword = analyticsConfig.getAdminPassword();
                    try {
                        this.dataPublisher = new DataPublisher(analyticsServerUrlGroups, analyticsServerUsername,
                                analyticsServerPassword);
                    } catch (DataEndpointAgentConfigurationException e) {
                        throw new DataPublisherConfigurationException("Configuration Exception on data publisher for " +
                                "ReceiverGroup = " + analyticsServerUrlGroups + " for username " + analyticsServerUsername, e);
                    } catch (DataEndpointException e) {
                        throw new DataPublisherConfigurationException("Invalid ReceiverGroup = " + analyticsServerUrlGroups, e);
                    } catch (DataEndpointConfigurationException e) {
                        throw new DataPublisherConfigurationException("Invalid Data endpoint configuration.", e);
                    } catch (DataEndpointAuthenticationException e) {
                        throw new DataPublisherConfigurationException("Authentication Failed for user " +
                                analyticsServerUsername, e);
                    } catch (TransportException e) {
                        throw new DataPublisherConfigurationException("Error occurred while retrieving data publisher", e);
                    }
                } else {
                    return this.dataPublisher;
                }
            }
        }
        return this.dataPublisher;
    }

}
