/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.impl;

import org.wso2.carbon.device.application.mgt.common.Visibility;
import org.wso2.carbon.device.application.mgt.common.services.VisibilityManager;

/**
 * This is the defaut implementation for the visibility manager.
 */
public class VisibilityManagerImpl implements VisibilityManager {
    @Override
    public void addVisibilityMapping(String applicationId, Visibility visibility) {

    }

    @Override
    public Visibility getVisibility(String applicationId) {
        return null;
    }

    @Override
    public void updateVisibilityMapping(String applicationId, Visibility visibility) {

    }

    @Override
    public void removeVisibilityMapping(String applicationId) {

    }
}
