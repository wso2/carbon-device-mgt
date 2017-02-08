/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.jwt.client.extension.constant;

/**
 * This holds the constants related JWT client component.
 */
public class JWTConstants {
	public static final String OAUTH_EXPIRES_IN = "expires_in";
	public static final String OAUTH_TOKEN_TYPE = "token_type";
    public static final String OAUTH_TOKEN_SCOPE = "scope";
	public static final String JWT_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
	public static final String GRANT_TYPE_PARAM_NAME = "grant_type";
	public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
	public static final String REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME = "refresh_token";
	public static final String ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME = "access_token";
	public static final String JWT_PARAM_NAME = "assertion";
	public static final String SCOPE_PARAM_NAME = "scope";
	public static final String DEFAULT_JWT_CLIENT = "default-jwt-client";
}

