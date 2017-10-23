package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceOrganizationDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceOrganizationDAOException;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceOrganizationDAOImpl;

import java.util.ArrayList;
import java.util.List;

public class DeviceOrganizationProviderServiceImpl implements DeviceOrganizationProviderService {

    private static final Log log = LogFactory.getLog(DeviceOrganizationProviderServiceImpl.class);

    DeviceOrganizationDAOImpl deviceOrganizationDAOimpl = new DeviceOrganizationDAOImpl();


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
            throw new DeviceOrganizationException(msg,e);
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
            throw new DeviceOrganizationException(msg,e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
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
            throw new DeviceOrganizationException(msg,e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
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
            throw new DeviceOrganizationException(msg,e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return devicesInOrganization;
        }
    }

    @Override
    public List<String> getChildrenIdsByParentId(String parentId) {
        List<String> childrenIds = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            childrenIds = deviceOrganizationDAOimpl.getChildrenIdsByParentId(parentId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceOrganizationDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error while getting children of Device " + parentId + " on organization";
            log.error(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return childrenIds;
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
        for (DeviceOrganizationMetadataHolder tempHolder: tempDevicesInOrganization) {
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
            String msg = "Error while getting devices in organization";
            log.error(msg, e);
        }
        List<DeviceOrganizationVisNode> nodes = new ArrayList<>();
        for (DeviceOrganizationMetadataHolder tempHolder: tempDevicesInOrganization) {
            String tempId = tempHolder.getDeviceId();
            String tempLabel = tempHolder.getDeviceName();
            int tempSize = 21;
            String tempColor = "#0000ff";
            if (tempHolder.getDeviceId().equals("server")) {
                tempColor = "#ffa500";
                tempSize = 70;
            }
            if (!tempHolder.getDeviceId().equals("server") && tempHolder.getIsGateway() == 1) {
                tempSize = 33;
                tempColor = "#ff0000";
            }
            if (tempHolder.getState() == 1 || tempHolder.getState() == 0) {
                tempColor = "#d3d3d3";
            }
            nodes.add(new DeviceOrganizationVisNode(tempId,tempLabel,tempSize,tempColor));
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
        for (DeviceOrganizationMetadataHolder tempHolder: tempDevicesInOrganization) {
            String child = tempHolder.getDeviceId();
            String parent = tempHolder.getParent();
            if (parent == "" || parent == " ") {
                continue;
            } else {
                edges.add(new DeviceOrganizationVisEdge(parent, child));
            }
        }
        return edges;
    }

    @Override
    public String updateDeviceOrganizationName(String deviceId, String newDeviceName) {
        String updatedName = null;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            updatedName = deviceOrganizationDAOimpl.updateDeviceOrganizationName(deviceId, newDeviceName);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
        } catch (DeviceOrganizationDAOException e) {
            String msg = "Error while updating device name";
            log.error(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return updatedName;
        }
    }

    @Override
    public String updateDeviceOrganizationParent(String deviceId, String newParent) {
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
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            return updatedParent;
        }
    }

    @Override
    public void removeDeviceOrganization(String deviceId) {
        //not implemented
    }


}
