/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.core.archival.dao;

import org.wso2.carbon.device.mgt.core.archival.beans.*;

import java.sql.ResultSet;
import java.util.List;

/**
 * Operations to move data from DM database to archival database
 */
public interface ArchivalDAO {

    int DEFAULT_BATCH_SIZE = 1000;

    List<Integer> getAllOperations() throws ArchivalDAOException;

    List<Integer> getPendingAndInProgressOperations() throws ArchivalDAOException;

    void copyOperationIDsForArchival(List<Integer> operationIds) throws ArchivalDAOException;

    List<ArchiveOperationResponse> selectOperationResponses() throws ArchivalDAOException;

    void moveOperationResponses(List<ArchiveOperationResponse> rs) throws ArchivalDAOException;

    List<ArchiveNotification> selectNotifications() throws ArchivalDAOException;

    void moveNotifications(List<ArchiveNotification> rs) throws ArchivalDAOException;

    List<ArchiveCommandOperation> selectCommandOperations() throws ArchivalDAOException;

    void moveCommandOperations(List<ArchiveCommandOperation> rs) throws ArchivalDAOException;

    List<ArchiveProfileOperation> selectProfileOperations() throws ArchivalDAOException;

    void moveProfileOperations(List<ArchiveProfileOperation> rs) throws ArchivalDAOException;

    List<ArchiveEnrolmentOperationMap> selectEnrolmentMappings() throws ArchivalDAOException;

    void moveEnrolmentMappings(List<ArchiveEnrolmentOperationMap> rs) throws ArchivalDAOException;

    List<ArchiveOperation> selectOperations() throws ArchivalDAOException;

    void moveOperations(List<ArchiveOperation> rs) throws ArchivalDAOException;

    void truncateOperationIDsForArchival() throws ArchivalDAOException;

}
