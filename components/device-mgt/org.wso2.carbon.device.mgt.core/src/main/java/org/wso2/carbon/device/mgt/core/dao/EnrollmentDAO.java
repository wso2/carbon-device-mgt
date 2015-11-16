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
package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;

import java.util.List;

public interface EnrollmentDAO {

    int addEnrollment(int deviceId, EnrolmentInfo enrolmentInfo, int tenantId) throws DeviceManagementDAOException;

    int updateEnrollment(int deviceId, EnrolmentInfo enrolmentInfo,
                         int tenantId) throws DeviceManagementDAOException;

    int updateEnrollment(EnrolmentInfo enrolmentInfo) throws DeviceManagementDAOException;

    int removeEnrollment(int deviceId, String currentOwner, int tenantId) throws DeviceManagementDAOException;

    boolean setStatus(int deviceId, String currentOwner, Status status,
                      int tenantId) throws DeviceManagementDAOException;

    Status getStatus(int deviceId, String currentOwner, int tenantId) throws DeviceManagementDAOException;

    EnrolmentInfo getEnrollment(int deviceId, String currentUser, int tenantId) throws DeviceManagementDAOException;

    List<EnrolmentInfo> getEnrollmentsOfUser(int deviceId, String user, int tenantId) throws
                                                                                             DeviceManagementDAOException;

}
