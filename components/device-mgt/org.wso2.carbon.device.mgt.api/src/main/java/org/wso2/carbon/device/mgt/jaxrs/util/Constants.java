/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.util;

/**
 * Holds the constants used by DeviceImpl Management Admin web application.
 */
public class Constants {

	public static final String USER_CLAIM_EMAIL_ADDRESS = "http://wso2.org/claims/emailaddress";
	public static final String USER_CLAIM_FIRST_NAME = "http://wso2.org/claims/givenname";
	public static final String USER_CLAIM_LAST_NAME = "http://wso2.org/claims/lastname";
	public static final String PRIMARY_USER_STORE = "PRIMARY";
	public static final String SCOPE = "scope";

	public final class ErrorMessages {
		private ErrorMessages () { throw new AssertionError(); }

		public static final String STATUS_BAD_REQUEST_MESSAGE_DEFAULT = "Bad Request";

	}

	public final class DeviceConstants {
		private DeviceConstants () { throw new AssertionError(); }

		public static final String APPLICATION_JSON = "application/json";
		public static final String HEADER_CONTENT_TYPE = "Content-Type";
	}

}
