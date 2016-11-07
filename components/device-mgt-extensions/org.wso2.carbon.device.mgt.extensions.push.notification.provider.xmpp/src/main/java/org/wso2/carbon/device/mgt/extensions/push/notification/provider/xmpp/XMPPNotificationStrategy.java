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
package org.wso2.carbon.device.mgt.extensions.push.notification.provider.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.xmpp.internal.XMPPDataHolder;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.xmpp.internal.util.XMPPAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XMPPNotificationStrategy implements NotificationStrategy {

    private static final String XMPP_CLIENT_JID = "xmpp.client.jid";
    private static final String XMPP_CLIENT_SUBJECT = "xmpp.client.subject";
    public static final String XMPP_CLIENT_MESSAGE_TYPE = "xmpp.client.messageType";
    private String xmppAdapterName;
    private static final Log log = LogFactory.getLog(XMPPNotificationStrategy.class);

    public XMPPNotificationStrategy(PushNotificationConfig config) {

        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        xmppAdapterName = config.getProperty(XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_NAME);
        outputEventAdapterConfiguration.setName(xmppAdapterName);
        outputEventAdapterConfiguration.setType(XMPPAdapterConstants.XMPP_ADAPTER_TYPE);
        outputEventAdapterConfiguration.setMessageFormat(MessageType.TEXT);
        Map<String, String> xmppAdapterProperties = new HashMap<>();
        xmppAdapterProperties.put(XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_HOST, config.getProperty(
                XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_HOST));
        xmppAdapterProperties.put(XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_PORT, config.getProperty(
                XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_PORT));
        xmppAdapterProperties.put(XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_USERNAME, config.getProperty(
                XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_USERNAME));
        xmppAdapterProperties.put(XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_PASSWORD, config.getProperty(
                XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_PASSWORD));
        xmppAdapterProperties.put(XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_JID, config.getProperty(
                XMPPAdapterConstants.XMPP_ADAPTER_PROPERTY_JID));
        outputEventAdapterConfiguration.setStaticProperties(xmppAdapterProperties);
        try {
            XMPPDataHolder.getInstance().getOutputEventAdapterService().create(outputEventAdapterConfiguration);
        } catch (OutputEventAdapterException e) {
            throw new InvalidConfigurationException("Error occurred while initializing MQTT output event adapter", e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        Map<String, String> dynamicProperties = new HashMap<>();
        Properties properties = ctx.getOperation().getProperties();
        dynamicProperties.put("jid", properties.getProperty(XMPP_CLIENT_JID));
        dynamicProperties.put("subject", properties.getProperty(XMPP_CLIENT_SUBJECT));
        dynamicProperties.put("messageType", properties.getProperty(XMPP_CLIENT_MESSAGE_TYPE));
        XMPPDataHolder.getInstance().getOutputEventAdapterService().publish(xmppAdapterName, dynamicProperties,
                                                                            ctx.getOperation().getPayLoad());
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {
        XMPPDataHolder.getInstance().getOutputEventAdapterService().destroy(xmppAdapterName);
    }

}
