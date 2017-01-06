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
package org.wso2.carbon.device.mgt.extensions.push.notification.provider.xmpp.internal.util;

public final class XMPPAdapterConstants {

    private XMPPAdapterConstants() {
        throw new AssertionError();
    }

    public static final String XMPP_ADAPTER_TYPE = "xmpp";
    public static final String XMPP_ADAPTER_PROPERTY_PASSWORD = "password";
    public static final String XMPP_ADAPTER_PROPERTY_JID = "jid";
    public static final String SUBJECT_PROPERTY_KEY = "subject";
    public static final String MESSAGE_TYPE_PROPERTY_KEY = "messageType";
    public static final String CHAT_PROPERTY_KEY = "chat";
    public static final String XMPP_ADAPTER_PROPERTY_SUBDOMAIN = "subDomain";
    public static final String XMPP_ADAPTER_PROPERTY_HOST = "host";
    public static final String XMPP_ADAPTER_PROPERTY_PORT = "port";
    public static final String XMPP_ADAPTER_PROPERTY_USERNAME = "username";
    public static final String XMPP_ADAPTER_PROPERTY_NAME = "xmpp.adapter.name";

}
