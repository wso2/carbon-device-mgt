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
package org.wso2.carbon.email.sender.core;

import java.util.*;

public class EmailContext {

    private Set<String> recipients;
    private Properties properties;
    private ContentProviderInfo contentProviderInfo;

    private EmailContext(final ContentProviderInfo contentProviderInfo, final Set<String> recipients, final Properties properties) {
        if (contentProviderInfo == null) {
            throw new IllegalArgumentException("Content provider information cannot be null");
        }
        this.contentProviderInfo = contentProviderInfo;
        if (recipients == null) {
            throw new IllegalArgumentException("Recipient list cannot be null");
        }
        if (recipients.size() == 0) {
            throw new IllegalArgumentException("No recipient is configured. Recipient list should carry at " +
                    "least one recipient");
        }
        this.recipients = recipients;
        if (properties == null) {
            throw new IllegalArgumentException("Email Context property bag cannot be null");
        }
        this.properties = properties;
    }

    private EmailContext(final ContentProviderInfo contentProviderInfo, final String recipient, final Properties properties) {
        if (contentProviderInfo == null) {
            throw new IllegalArgumentException("Content provider information cannot be null");
        }
        this.contentProviderInfo = contentProviderInfo;
        if (recipient == null || recipient.isEmpty()) {
            throw new IllegalArgumentException("Recipient can't be null or empty. Please specify a valid " +
                    "recipient email address");
        }
        this.recipients = new HashSet<String>() {{
            add(recipient);
        }};
        if (properties == null) {
            throw new IllegalArgumentException("Email Context property bag cannot be null");
        }
        this.properties = properties;
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String name) {
        return (String) properties.get(name);
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    public ContentProviderInfo getContentProviderInfo() {
        return contentProviderInfo;
    }

    public static class EmailContextBuilder {

        private Set<String> recipients;
        private ContentProviderInfo contentProviderInfo;
        private Properties properties;

        public EmailContextBuilder(final ContentProviderInfo contentProviderInfo, Set<String> recipients) {
            if (contentProviderInfo == null) {
                throw new IllegalArgumentException("Content provider information cannot be null");
            }
            this.contentProviderInfo = contentProviderInfo;
            if (recipients == null) {
                throw new IllegalArgumentException("Recipient list cannot be null");
            }
            if (recipients.size() == 0) {
                throw new IllegalArgumentException("No recipient is configured. Recipient list should carry at " +
                        "least one recipient");
            }
            this.recipients = recipients;
            this.properties = new Properties();
        }

        public EmailContextBuilder(final ContentProviderInfo contentProviderInfo, final String recipient,
                                   final Properties properties) {
            if (contentProviderInfo == null) {
                throw new IllegalArgumentException("Content provider information cannot be null");
            }
            this.contentProviderInfo = contentProviderInfo;
            if (recipient == null || recipient.isEmpty()) {
                throw new IllegalArgumentException("Recipient can't be null or empty. Please specify a valid " +
                        "recipient email address");
            }
            this.recipients = new HashSet<String>() {{
                add(recipient);
            }};
            if (properties == null) {
                throw new IllegalArgumentException("Email Context property bag cannot be null");
            }
            this.properties = properties;
        }

        public EmailContextBuilder addProperty(String name, String value) {
            properties.setProperty(name, value);
            return this;
        }

        public EmailContext build() {
            return new EmailContext(contentProviderInfo, recipients, properties);
        }

    }

}
