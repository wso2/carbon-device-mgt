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

import java.util.List;

/**
 * Operations to move data from DM database to archival database
 */
public interface ArchivalDAO {

    int DEFAULT_BATCH_SIZE = 1000;

    List<Integer> getAllOperations() throws ArchivalDAOException;

    List<Integer> getPendingAndInProgressOperations() throws ArchivalDAOException;

    void copyOperationIDsForArchival(List<Integer> operationIds) throws ArchivalDAOException;

    void moveOperationResponses() throws ArchivalDAOException;

    void moveNotifications() throws ArchivalDAOException;

    void moveCommandOperations() throws ArchivalDAOException;

    void moveProfileOperations() throws ArchivalDAOException;

    void moveEnrolmentMappings() throws ArchivalDAOException;

    void moveOperations() throws ArchivalDAOException;

    void truncateOperationIDsForArchival() throws ArchivalDAOException;

}
