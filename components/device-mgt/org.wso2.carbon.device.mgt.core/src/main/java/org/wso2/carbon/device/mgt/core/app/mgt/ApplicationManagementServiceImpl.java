/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
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
*/
package org.wso2.carbon.device.mgt.core.app.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;

import java.util.List;

public class ApplicationManagementServiceImpl implements ApplicationManager {

    private static final Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);
    @Override
    public Application[] getApplications(String domain, int pageNumber,
                                         int size) throws ApplicationManagementException {
        return DeviceManagementDataHolder.getInstance().getAppManager().getApplications(domain, pageNumber, size);
    }

    @Override
    public void updateApplicationStatus(
            DeviceIdentifier deviceId, Application application, String status) throws ApplicationManagementException {
        DeviceManagementDataHolder.getInstance().getAppManager().updateApplicationStatus(deviceId, application, status);

    }

    @Override
    public String getApplicationStatus(DeviceIdentifier deviceId,
                                       Application application) throws ApplicationManagementException {
        return null;
    }

    @Override
    public void installApplication(Operation operation, List<DeviceIdentifier> deviceIdentifiers)
            throws ApplicationManagementException {
        try {
            DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().addOperation(operation,
                    deviceIdentifiers);
        } catch (OperationManagementException opMgtEx) {
            String errorMsg = "Error occurred when add operations at install application";
            log.error(errorMsg, opMgtEx);
            throw new ApplicationManagementException();
        }
        DeviceManagementDataHolder.getInstance().getAppManager().installApplication(operation, deviceIdentifiers);
    }

}
