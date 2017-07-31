/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.operation.mgt.dao;

import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationEnrolmentMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMapping;

import java.util.List;
import java.util.Map;

public interface OperationMappingDAO {

    void addOperationMapping(int operationId, Integer deviceId, boolean isScheduled) throws OperationManagementDAOException;

    void removeOperationMapping(int operationId, Integer deviceId) throws OperationManagementDAOException;

    void updateOperationMapping(int operationId, Integer deviceId, Operation.PushNotificationStatus pushNotificationStatus) throws
            OperationManagementDAOException;
    void updateOperationMapping(List<OperationMapping> operationMappingList) throws
            OperationManagementDAOException;

    /**
     * This method returns first pending/repeated operation available for each active enrolment of given device-type
     * where the operation was created after the given timestamp.
     *
     * @param minDuration - Upper limit of Operation created time
     * @param maxDuration - Lower limit of Operation created time
     * @param deviceTypeId - Device Type Id of required devices
     * @return List<OperationEnrolmentMapping> - List of OperationEnrolmentMapping objects containing required data
     * @throws OperationManagementDAOException
     */
    List<OperationEnrolmentMapping> getFirstPendingOperationMappingsForActiveEnrolments(long minDuration,
                                                                                        long maxDuration, int deviceTypeId)
            throws OperationManagementDAOException;

    /**
     * This method returns the timestamp of last completed Operation for each active enrolment of given device-type
     * where the operation was completed after the given timestamp.
     *
     * @param timeStamp - Timestamp of considered time-interval
     * @param deviceTypeId - Device Type of required devices
     * @return List<OperationEnrolmentMapping> - List of OperationEnrolmentMapping objects containing required data
     * @throws OperationManagementDAOException
     */
    Map<Integer, Long> getLastConnectedTimeForActiveEnrolments(long timeStamp, int deviceTypeId)
            throws OperationManagementDAOException;

}
