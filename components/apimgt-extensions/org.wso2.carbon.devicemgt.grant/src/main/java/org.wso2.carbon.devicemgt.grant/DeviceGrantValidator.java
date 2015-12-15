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

package org.wso2.carbon.devicemgt.grant;

import org.apache.oltu.oauth2.common.validators.AbstractValidator;
import javax.servlet.http.HttpServletRequest;

/**
 * Validator class for Device Grant Type.
 * Picks out the device id and the user name from the request and adds
 * same to request parameter array.
 */
public class DeviceGrantValidator extends AbstractValidator<HttpServletRequest> {

    public DeviceGrantValidator(){
        requiredParams.add(OauthGrantConstants.DEVICE_ID);
        requiredParams.add(OauthGrantConstants.DEVICE_TYPE);
        requiredParams.add(OauthGrantConstants.USER_NAME);
        requiredParams.add(OauthGrantConstants.SCOPE);
    }
}
