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

package org.wso2.carbon.device.mgt.core.archival;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.archival.dao.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArchivalServiceImpl implements ArchivalService {
    private static Log log = LogFactory.getLog(ArchivalServiceImpl.class);

    private ArchivalDAO archivalDAO;
    private DataDeletionDAO dataDeletionDAO;

    private static int ITERATION_COUNT = 10000;

    private String[] NOT_IN_PROGRESS_OPS = new String[]{"COMPLETED", "ERROR", "REPEATED"};
    private String[] NOT_PENDING_OPS = new String[]{"COMPLETED", "ERROR", "REPEATED", "IN_PROGRESS"};
    private String[] NOT_PENDING_IN_PROGRESS_OPS = new String[]{"COMPLETED", "ERROR", "REPEATED"};

    public ArchivalServiceImpl() {
        this.archivalDAO = ArchivalSourceDAOFactory.getDataPurgingDAO();
        this.dataDeletionDAO = ArchivalDestinationDAOFactory.getDataDeletionDAO();
    }

    @Override
    public void archiveTransactionalRecords() throws ArchivalException {
        try {
            ArchivalSourceDAOFactory.openConnection();
            ArchivalDestinationDAOFactory.openConnection();

            List<Integer> allOperations = archivalDAO.getAllOperations();
            List<Integer> pendingAndIPOperations = archivalDAO.getPendingAndInProgressOperations();

            log.info(allOperations.size() + " All Operations. " + pendingAndIPOperations.size() +
                    " P&IP Operations");
            //Get the diff of operations
            Set<Integer> setA = new HashSet<>(allOperations);
            Set<Integer> setB = new HashSet<>(pendingAndIPOperations);
            setA.removeAll(setB);

            List<Integer> candidates = new ArrayList<>();
            candidates.addAll(setA);

            int total = candidates.size();
            int batches = calculateNumberOfBatches(total);
            int batchSize = ITERATION_COUNT;
            if (log.isDebugEnabled()) {
                log.debug(total + " Operations ready for archiving. " + batches + " iterations to be done.");
            }

            beginTransactions();
            for (int i = 1; i <= batches; i++) {
                int startIdx = batchSize * (i - 1);
                int endIdx = batchSize * i;
                if (i == batches) {
                    endIdx = startIdx + (total % batchSize);
                }
                if(log.isDebugEnabled()) {
                    log.debug("\n\n############ Iterating over batch " + i + "[" +
                            startIdx + "," + endIdx + "] #######");
                }
                List<Integer> subList = candidates.subList(startIdx, endIdx);
                prepareTempTable(subList);

                //Purge the largest table, DM_DEVICE_OPERATION_RESPONSE
                if (log.isDebugEnabled()) {
                    log.debug("## Purging operation responses");
                }
                archivalDAO.moveOperationResponses();

                //Purge the notifications table, DM_NOTIFICATION
                if (log.isDebugEnabled()) {
                    log.debug("## Purging notifications");
                }
                archivalDAO.moveNotifications();

                //Purge the command operations table, DM_COMMAND_OPERATION
                if (log.isDebugEnabled()) {
                    log.debug("## Purging command operations");
                }
                archivalDAO.moveCommandOperations();

                //Purge the profile operation table, DM_PROFILE_OPERATION
                if (log.isDebugEnabled()) {
                    log.debug("## Purging profile operations");
                }
                archivalDAO.moveProfileOperations();

                //Purge the enrolment mappings table, DM_ENROLMENT_OP_MAPPING
                if (log.isDebugEnabled()) {
                    log.debug("## Purging enrolment mappings");
                }
                archivalDAO.moveEnrolmentMappings();

                //Finally, purge the operations table, DM_OPERATION
                if (log.isDebugEnabled()) {
                    log.debug("## Purging operations");
                }
                archivalDAO.moveOperations();
            }
            commitTransactions();
        } catch (ArchivalDAOException e) {
            rollbackTransactions();
            throw new ArchivalException("An error occurred while data archival", e);
        } catch (SQLException e) {
            throw new ArchivalException("An error occurred while connecting to the archival database.", e);
        } finally {
            ArchivalSourceDAOFactory.closeConnection();
            ArchivalDestinationDAOFactory.closeConnection();
        }
    }

    private void prepareTempTable(List<Integer> subList) throws ArchivalDAOException {
        //Clean up the DM_ARCHIVED_OPERATIONS table
        if (log.isDebugEnabled()) {
            log.debug("## Truncating the temporary table");
        }
        archivalDAO.truncateOperationIDsForArchival();
        archivalDAO.copyOperationIDsForArchival(subList);
    }

    private void beginTransactions() throws ArchivalException {
        try {
            ArchivalSourceDAOFactory.beginTransaction();
            ArchivalDestinationDAOFactory.beginTransaction();
        } catch (TransactionManagementException e) {
            log.error("An error occurred during starting transactions", e);
            throw new ArchivalException("An error occurred during starting transactions", e);
        }
    }

    private void commitTransactions() {
        ArchivalSourceDAOFactory.commitTransaction();
        ArchivalDestinationDAOFactory.commitTransaction();
    }

    private void rollbackTransactions() {
        ArchivalSourceDAOFactory.rollbackTransaction();
        ArchivalDestinationDAOFactory.rollbackTransaction();
    }

    private int calculateNumberOfBatches(int total) {
        int batches = 0;
        int batchSize = ITERATION_COUNT;
        if ((total % batchSize) > 0) {
            batches = (total / batchSize) + 1;
        } else {
            batches = total / batchSize;
        }
        return batches;
    }

    @Override
    public void deleteArchivedRecords() throws ArchivalException {
        try {
            ArchivalDestinationDAOFactory.openConnection();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting operation responses");
            }
            dataDeletionDAO.deleteOperationResponses();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting notifications ");
            }
            dataDeletionDAO.deleteNotifications();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting command operations");
            }
            dataDeletionDAO.deleteCommandOperations();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting profile operations ");
            }
            dataDeletionDAO.deleteProfileOperations();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting enrolment mappings ");
            }
            dataDeletionDAO.deleteEnrolmentMappings();

            if (log.isDebugEnabled()) {
                log.debug("## Deleting operations ");
            }
            dataDeletionDAO.deleteOperations();
        } catch (SQLException e) {
            throw new ArchivalException("An error occurred while initialising data source for archival", e);
        } catch (ArchivalDAOException e) {
            log.error("An error occurred while executing DataDeletionTask");
        } finally {
            ArchivalDestinationDAOFactory.closeConnection();
        }
    }
}
