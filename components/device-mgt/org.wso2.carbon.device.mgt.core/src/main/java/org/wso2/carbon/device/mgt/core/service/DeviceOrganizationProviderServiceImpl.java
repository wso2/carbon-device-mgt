/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationMetadataHolder;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationVisNode;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationVisEdge;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationNode;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceOrganizationDAOException;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceOrganizationDAOImpl;

import java.util.List;
import java.util.ArrayList;

public class DeviceOrganizationProviderServiceImpl implements DeviceOrganizationProviderService {

    private static final Log log = LogFactory.getLog(DeviceOrganizationProviderServiceImpl.class);

    DeviceOrganizationDAOImpl deviceOrganizationDAOimpl = new DeviceOrganizationDAOImpl();

    static final String SERVER_ID = "server";

    @Override
    public boolean addDeviceOrganization(String deviceId, String deviceName, String path, int pingMins, int state,
                                         int isGateway) throws DeviceOrganizationException {
        boolean isSuccess = false;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            isSuccess = deviceOrganizationDAOimpl.addDeviceOrganization(deviceId, deviceName, path, pingMins, state,
                    isGateway);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            log.error(e.getMessage());
            throw new DeviceOrganizationException(e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return isSuccess;
        }
    }

    @Override
    public int getDeviceOrganizationStateById(String deviceId) throws DeviceOrganizationException {
        int tempState = -1;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            tempState = deviceOrganizationDAOimpl.getDeviceOrganizationStateById(deviceId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error while getting state of device with ID: " + deviceId + "'";
            log.error(msg, e);
            throw new DeviceOrganizationException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return tempState;
        }
    }

    @Override
    public String getDeviceOrganizationParent(String deviceId) throws DeviceOrganizationException {
        String tempParent = null;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            tempParent = deviceOrganizationDAOimpl.getDeviceOrganizationParent(deviceId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while getting parent of device with ID: '" + deviceId + "'";
            log.error(msg, e);
            throw new DeviceOrganizationException(msg, e);
        } catch (TransactionManagementException e) {
            log.error("Error occurred while initiating transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return tempParent;
        }
    }

    @Override
    public int getDeviceOrganizationIsGateway(String deviceId) throws DeviceOrganizationException {
        int tempIsGateway = -1;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            tempIsGateway = deviceOrganizationDAOimpl.getDeviceOrganizationIsGateway(deviceId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error while getting if device with ID '" + deviceId + "' is a gateway";
            log.error(msg, e);
            throw new DeviceOrganizationException(msg, e);
        } catch (TransactionManagementException e) {
            log.error("Error occurred while initiating transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return tempIsGateway;
        }
    }

    @Override
    public List<DeviceOrganizationMetadataHolder> getDevicesInOrganization() throws DeviceOrganizationException {
        List<DeviceOrganizationMetadataHolder> devicesInOrganization = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            devicesInOrganization = deviceOrganizationDAOimpl.getDevicesInOrganization();
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error while getting Devices in Organization";
            log.error(msg, e);
            throw new DeviceOrganizationException(msg, e);
        } catch (TransactionManagementException e) {
            log.error("Error occurred while initiating transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return devicesInOrganization;
        }
    }

    @Override
    public List<DeviceOrganizationMetadataHolder> getChildrenByParentId(String parentId)
            throws DeviceOrganizationException {
        List<DeviceOrganizationMetadataHolder> children = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            children = deviceOrganizationDAOimpl.getChildrenByParentId(parentId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error while getting children of Device " + parentId + " in organization";
            log.error(msg, e);
            throw new DeviceOrganizationException(msg, e);
        } catch (TransactionManagementException e) {
            log.error("Error occurred while initiating transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return children;
        }
    }

    @Override
    public int getDeviceIndexInArray(String deviceId) {
        List<DeviceOrganizationMetadataHolder> tempDevicesInOrganization = new ArrayList<>();
        try {
            tempDevicesInOrganization = this.getDevicesInOrganization();
        } catch (DeviceOrganizationException e) {
            String msg = "Error while getting devices in organization";
            log.error(msg, e);
        }
        int indexInArray;
        for (DeviceOrganizationMetadataHolder tempHolder : tempDevicesInOrganization) {
            if (tempHolder.getDeviceId().equals(deviceId)) {
                indexInArray = tempDevicesInOrganization.indexOf(tempHolder);
                return indexInArray;
            }
        }
        return -1;
    }

    @Override
    public List<DeviceOrganizationVisNode> generateNodes() {
        List<DeviceOrganizationMetadataHolder> tempDevicesInOrganization = new ArrayList<>();
        try {
            tempDevicesInOrganization = this.getDevicesInOrganization();
        } catch (DeviceOrganizationException e) {
            String msg = "Error while getting devices in organization database";
            log.error(msg, e);
        }
        List<DeviceOrganizationVisNode> nodes = new ArrayList<>();
        int serverFlag = 0;
        for (DeviceOrganizationMetadataHolder tempHolder : tempDevicesInOrganization) {
            if (SERVER_ID.equals(tempHolder.getDeviceId())) {
                serverFlag++;
            }
        }
        if (serverFlag == 0) {
            try {
                DeviceManagementDAOFactory.beginTransaction();
                deviceOrganizationDAOimpl.addDeviceOrganization(SERVER_ID, "WSO2 IoT server",
                        "", 0, 2, 1);
                DeviceManagementDAOFactory.commitTransaction();
            } catch (TransactionManagementException e) {
                String msg = "Error occurred while initiating transaction";
                log.error(msg, e);
            } catch (DeviceOrganizationDAOException e) {
                DeviceManagementDAOFactory.rollbackTransaction();
                String msg = "Error occurred while adding server Node to visualization";
                log.error(msg, e);
            } finally {
                DeviceManagementDAOFactory.closeConnection();
            }
        }
        List<DeviceOrganizationMetadataHolder> newTempDevicesInOrganization = new ArrayList<>();
        try {
            newTempDevicesInOrganization = this.getDevicesInOrganization();
        } catch (DeviceOrganizationException e) {
            String msg = "Error while getting devices in organization database";
            log.error(msg, e);
        }
        for (DeviceOrganizationMetadataHolder tempHolder : newTempDevicesInOrganization) {
            String tempId = tempHolder.getDeviceId();
            String tempLabel = tempHolder.getDeviceName();
            int tempSize = 21;
            String tempColor = "#0000ff";
            if (tempHolder.getDeviceId().equals(SERVER_ID)) {
                tempColor = "#ffa500";
                tempSize = 70;
            }
            if (!tempHolder.getDeviceId().equals(SERVER_ID) && tempHolder.getIsGateway() == 1) {
                tempSize = 33;
                tempColor = "#ff0000";
            }
            if (tempHolder.getState() == 1 || tempHolder.getState() == 0) {
                tempColor = "#d3d3d3";
            }
            nodes.add(new DeviceOrganizationVisNode(tempId, tempLabel, tempSize, tempColor));
        }
        return nodes;
    }

    @Override
    public List<DeviceOrganizationVisEdge> generateEdges() {
        List<DeviceOrganizationMetadataHolder> tempDevicesInOrganization = new ArrayList<>();
        try {
            tempDevicesInOrganization = this.getDevicesInOrganization();
        } catch (DeviceOrganizationException e) {
            String msg = "Error while getting devices in organization";
            log.error(msg, e);
        }
        List<DeviceOrganizationVisEdge> edges = new ArrayList<>();
        for (DeviceOrganizationMetadataHolder tempHolder : tempDevicesInOrganization) {
            String child = tempHolder.getDeviceId();
            String parent = tempHolder.getParent();
            if (parent.trim().isEmpty()) {
                continue;
            } else {
                edges.add(new DeviceOrganizationVisEdge(parent, child));
            }
        }
        return edges;
    }

    /**
     * this method transforms an array of type "DeviceOrganizationMetadataHolder" to an array of "DeviceOrganizationNode"
     */
    private List<DeviceOrganizationNode> transformMetadataHolderArray(List<DeviceOrganizationMetadataHolder>
                                                                              deviceDataList) {
        List<DeviceOrganizationNode> nodeDataList = new ArrayList<>();
        for (DeviceOrganizationMetadataHolder node : deviceDataList) {
            nodeDataList.add(new DeviceOrganizationNode(node.getDeviceId(), node.getParent()));
        }
        return nodeDataList;
    }

    /**
     * this method takes arrays with node data and converts them into string arrays
     */
    private List<String> createChildIdList(List<DeviceOrganizationNode> deviceDataList) {
        List<String> childIds = new ArrayList<>();
        for (DeviceOrganizationNode holder : deviceDataList) {
            childIds.add(holder.getId());
        }
        return childIds;
    }

    @Override
    public String updateDeviceOrganizationName(String deviceId, String newDeviceName)
            throws DeviceOrganizationException {
        String updatedName = null;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            updatedName = deviceOrganizationDAOimpl.updateDeviceOrganizationName(deviceId, newDeviceName);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error while updating device name";
            log.error(msg, e);
            throw new DeviceOrganizationException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return updatedName;
        }
    }

    @Override
    public String updateDeviceOrganizationParent(String deviceId, String newParent) throws DeviceOrganizationException {
        String updatedParent = null;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            updatedParent = deviceOrganizationDAOimpl.updateDeviceOrganizationParent(deviceId, newParent);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating device path";
            log.error(msg, e);
            throw new DeviceOrganizationException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return updatedParent;
        }
    }

    @Override
    public void removeDeviceOrganization(String deviceId) throws UnsupportedOperationException{
        /**
         * not implemented
         */
    }

    private boolean serverSetup() {
        boolean isSuccess = false;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            isSuccess = deviceOrganizationDAOimpl.addDeviceOrganization(SERVER_ID, "WSO2 IoT server",
                    "", 0, 2, 1);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding server Node to visualization";
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return isSuccess;
        }
    }
}
