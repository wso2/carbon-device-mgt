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
package org.wso2.carbon.device.mgt.core;

public final class DeviceManagementConstants {

    public static final class Common {
        private Common() {
            throw new AssertionError();
        }

        public static final String PROPERTY_SETUP = "setup";
        public static final String DEFAULT_LICENSE_CONFIG_XML_NAME = "license-config.xml";
    }

	public static final class AppManagement {
		private AppManagement() {
			throw new AssertionError();
		}

		public static final String OAUTH_APPLICATION_NAME = "app_management_application";
		public static final String OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
		public final static String OAUTH_VERSION_2 = "oauth-2.0";
		public final static String OAUTH_ADMIN_SERVICE = "/services/OAuthAdminService";
	}

    public static final class EmailNotifications {
        private EmailNotifications() {
            throw new AssertionError();
        }

        public static final String ENROL_NOTIFICATION_TYPE = "enrol";
        public static final String USER_REGISTRATION_NOTIFICATION_TYPE = "userRegistration";
    }
}
