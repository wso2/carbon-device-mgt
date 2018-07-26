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
import org.wso2.carbon.device.mgt.core.archival.beans.*;
import org.wso2.carbon.device.mgt.core.archival.dao.*;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArchivalServiceImpl implements ArchivalService {
    private static Log log = LogFactory.getLog(ArchivalServiceImpl.class);

    private ArchivalDAO archivalDAO;
    private DataDeletionDAO dataDeletionDAO;

    private static int ITERATION_COUNT =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getBatchSize();

    private String[] NOT_IN_PROGRESS_OPS = new String[]{"COMPLETED", "ERROR", "REPEATED"};
    private String[] NOT_PENDING_OPS = new String[]{"COMPLETED", "ERROR", "REPEATED", "IN_PROGRESS"};
    private String[] NOT_PENDING_IN_PROGRESS_OPS = new String[]{"COMPLETED", "ERROR", "REPEATED"};

    public ArchivalServiceImpl() {
        this.archivalDAO = ArchivalSourceDAOFactory.getDataPurgingDAO();
        this.dataDeletionDAO = ArchivalDestinationDAOFactory.getDataDeletionDAO();
    }

    @Override
    public void archiveTransactionalRecords() throws ArchivalException {
        List<Integer> allOperations;
        List<Integer> pendingAndIPOperations;
        try {
            ArchivalSourceDAOFactory.openConnection();
            ArchivalDestinationDAOFactory.openConnection();

            if (log.isDebugEnabled()) {
                log.debug("Fetching All Operations");
            }
            allOperations = archivalDAO.getAllOperations();

            if (log.isDebugEnabled()) {
                log.debug("Fetching All Pending Operations");
            }
            pendingAndIPOperations = archivalDAO.getPendingAndInProgressOperations();

        } catch (ArchivalDAOException e) {
//            rollbackTransactions();
            String msg = "Rollback the get all operations and get all pending operations";
            log.error(msg, e);
            throw new ArchivalException(msg, e);
        } catch (SQLException e) {
            String msg = "An error occurred while connecting to the archival database";
            log.error(msg, e);
            throw new ArchivalException(msg, e);
        } finally {
            ArchivalSourceDAOFactory.closeConnection();
            ArchivalDestinationDAOFactory.closeConnection();
        }

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
            log.debug(batchSize + " is the batch size");
        }

        for (int i = 1; i <= batches; i++) {
            int startIdx = batchSize * (i - 1);
            int endIdx = batchSize * i;
            if (i == batches) {
                endIdx = startIdx + (total % batchSize);
            }
            if (log.isDebugEnabled()) {
                log.debug("\n\n############ Iterating over batch " + i + "[" +
                        startIdx + "," + endIdx + "] #######");
            }
            List<Integer> subList = candidates.subList(startIdx, endIdx);

            if (log.isDebugEnabled()) {
                log.debug("SubList size is: " + subList.size());
                if (subList.size() > 0) {
                    log.debug("First Element is: " + subList.get(0));
                    log.debug("Last Element is: " + subList.get(subList.size() - 1));
                }
            }

            if (log.isDebugEnabled()) {
                for (Integer val : subList) {
                    if (log.isDebugEnabled()) {
                        log.debug("Sub List Element: " + val);
                    }
                }
            }

            try {
                beginTransactions();
                prepareTempTable(subList);
                commitTransactions();
            } catch (Exception e) {
                rollbackTransactions();
                String msg = "Error occurred while preparing the operations.";
                log.error(msg, e);
                throw new ArchivalException(msg, e);
            } finally {
                ArchivalSourceDAOFactory.closeConnection();
                ArchivalDestinationDAOFactory.closeConnection();
            }

            List<ArchiveOperationResponse> operationResponses = null;
            List<ArchiveNotification> notification = null;
            List<ArchiveCommandOperation> commandOperations = null;
            List<ArchiveProfileOperation> profileOperations = null;
            List<ArchiveEnrolmentOperationMap> enrollmentMapping = null;
            List<ArchiveOperation> operations = null;

            try {
                openConnection();
                operationResponses = archivalDAO.selectOperationResponses();
                notification = archivalDAO.selectNotifications();
                commandOperations = archivalDAO.selectCommandOperations();
                profileOperations = archivalDAO.selectProfileOperations();
                enrollmentMapping = archivalDAO.selectEnrolmentMappings();
                operations = archivalDAO.selectOperations();

            } catch (Exception e) {
                String msg = "Error occurred while retrieving data.";
                log.error(msg, e);
                throw new ArchivalException(msg, e);
            } finally {
                closeConnection();
            }

            try {
                beginTransactions();

                //Purge the largest table, DM_DEVICE_OPERATION_RESPONSE
                if (log.isDebugEnabled()) {
                    log.debug("## Archiving operation responses");
                }
                archivalDAO.moveOperationResponses(operationResponses);

                //Purge the notifications table, DM_NOTIFICATION
                if (log.isDebugEnabled()) {
                    log.debug("## Archiving notifications");
                }
                archivalDAO.moveNotifications(notification);

                //Purge the command operations table, DM_COMMAND_OPERATION
                if (log.isDebugEnabled()) {
                    log.debug("## Archiving command operations");
                }
                archivalDAO.moveCommandOperations(commandOperations);

                //Purge the profile operation table, DM_PROFILE_OPERATION
                if (log.isDebugEnabled()) {
                    log.debug("## Archiving profile operations");
                }
                archivalDAO.moveProfileOperations(profileOperations);

                //Purge the enrolment mappings table, DM_ENROLMENT_OP_MAPPING
                if (log.isDebugEnabled()) {
                    log.debug("## Archiving enrolment mappings");
                }
                archivalDAO.moveEnrolmentMappings(enrollmentMapping);

                //Finally, purge the operations table, DM_OPERATION
                if (log.isDebugEnabled()) {
                    log.debug("## Archiving operations");
                }
                archivalDAO.moveOperations(operations);
                commitTransactions();
                if (log.isDebugEnabled()) {
                    log.debug("End of Iteration : " + i);
                }
            } catch (ArchivalDAOException e) {
                rollbackTransactions();
                String msg = "Error occurred while trying to archive data to the six tables";
                log.error(msg, e);
                throw new ArchivalException(msg, e);
            } finally {
                ArchivalSourceDAOFactory.closeConnection();
                ArchivalDestinationDAOFactory.closeConnection();
            }

        }
    }

    private void prepareTempTable(List<Integer> subList) throws ArchivalDAOException {
        //Clean up the DM_ARCHIVED_OPERATIONS table
        if (log.isDebugEnabled()) {
            log.debug("## Truncating the temporary table");
        }
        archivalDAO.truncateOperationIDsForArchival();
        if (log.isDebugEnabled()) {
            log.debug("## Inserting into the temporary table");
        }
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

    private void openConnection() throws ArchivalException {
        try {
            ArchivalSourceDAOFactory.openConnection();
        } catch (SQLException e) {
            String msg = "An error occurred during opening connection";
            log.error(msg, e);
            throw new ArchivalException(msg, e);
        }

    }

    private void closeConnection() throws ArchivalException {
        try {
            ArchivalSourceDAOFactory.closeConnection();
        } catch (Exception e) {
            String msg = "An error occurred during opening connection";
            log.error(msg, e);
            throw new ArchivalException(msg, e);
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
