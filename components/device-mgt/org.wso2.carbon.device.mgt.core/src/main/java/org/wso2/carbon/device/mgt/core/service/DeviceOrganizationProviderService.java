package org.wso2.carbon.device.mgt.core.service;

import org.wso2.carbon.device.mgt.common.*;

import java.util.List;
import java.util.Map;

/**
 * Interface that implements Device organization services
 */
public interface DeviceOrganizationProviderService {

    /**
     * This method is used to add a new device
     *
     * @param deviceId   unique device identifier
     * @param deviceName identifier name given to device
     * @param parent     parent that device is child to in the network
     * @param pingMins   number of minutes since last ping from device
     * @param state      state of activity of device
     * @param isGateway  can identify if device is a gateway or not
     * @return true if device added successfully
     * @throws DeviceOrganizationException
     */
    boolean addDeviceOrganization(String deviceId, String deviceName, String parent,
                                  int pingMins, int state, int isGateway) throws DeviceOrganizationException;

    /**
     * This method enables to retrieve the device state by entering the device ID
     *
     * @param deviceId unique device identifier
     * @return integer with device state
     * @throws DeviceOrganizationException
     */
    int getDeviceOrganizationStateById(String deviceId) throws DeviceOrganizationException;

    /**
     * This method allows us to retrieve the path of a device based on device ID
     *
     * @param deviceId unique device identifier
     * @return string with device parent ID
     * @throws DeviceOrganizationException
     */
    String getDeviceOrganizationParent(String deviceId) throws DeviceOrganizationException;

    /**
     * This method allows us to check whether any device in the organization is a gateway
     *
     * @param deviceId unique device identifier
     */
    int getDeviceOrganizationIsGateway(String deviceId) throws DeviceOrganizationException;

    /**
     * This method is used to retrieve all Devices in the Organization
     *
     * @return Arraylist with list of Devices in organization
     */
    List<DeviceOrganizationMetadataHolder> getDevicesInOrganization() throws DeviceOrganizationException;

    /**
     * This method is used to retrieve children IDs of a specific parent
     *
     * @param parentId unique device identifier of parent
     * @return String Arraylist with IDs of child devices
     */
    List<DeviceOrganizationMetadataHolder> getChildrenByParentId(String parentId) throws DeviceOrganizationException;

    /**
     * This method is used to get the index of the generated array
     *
     * @param deviceId unique device identifier
     * @return Integer value of index
     */
    int getDeviceIndexInArray(String deviceId);

    /**
     * This method is used to generate the nodes in the visualization
     *
     * @return Arraylist with the visualization related node date
     */
    List<DeviceOrganizationVisNode> generateNodes();

    /**
     * This method is used to generate the edges in the visualization
     *
     * @return Arraylist with the visualization related edge data
     */
    List<DeviceOrganizationVisEdge> generateEdges();

    /**
     * This method is used to update the device organization name
     *
     * @param deviceId   unique device identifier
     * @param deviceName identifier name given to device
     */
    String updateDeviceOrganizationName(String deviceId, String deviceName) throws DeviceOrganizationException;

    /**
     * This method is used to update the device organization path
     *
     * @param deviceId unique device identifier
     * @param parent   parent that device is child to in the network
     */
    String updateDeviceOrganizationParent(String deviceId, String parent) throws DeviceOrganizationException;

    /**
     * This method is used to remove a device
     *
     * @param deviceId unique device identifier
     */
    void removeDeviceOrganization(String deviceId) throws UnsupportedOperationException;

}
