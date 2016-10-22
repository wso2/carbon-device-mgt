package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.dao;

import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Attribute;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.DeviceDefinition;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeDeployerFileException;

import java.util.ArrayList;
import java.util.List;

public class DeviceDAODefinition {

    private String deviceTableName;
    private String primarkey;

    public List<String> getColumnNames() {
        return columnNames;
    }

    private List<String> columnNames = new ArrayList<>();


    public DeviceDAODefinition(DeviceDefinition deviceDefinition) {
        deviceTableName = deviceDefinition.getTableName();
        primarkey = deviceDefinition.getPrimaryKey();
        List<Attribute> attributes = deviceDefinition.getAttributes().getAttribute();
        if (deviceTableName == null || deviceTableName.isEmpty()) {
            throw new DeviceTypeDeployerFileException("Missing deviceTableName");
        }

        if (primarkey == null || primarkey.isEmpty()) {
            throw new DeviceTypeDeployerFileException("Missing primaryKey ");
        }

        if (attributes == null || attributes.size() == 0) {
            throw new DeviceTypeDeployerFileException("Missing Attributes ");
        }
        for (Attribute attribute : attributes) {
            if (attribute.getValue() == null ||attribute.getValue().isEmpty()) {
                throw new DeviceTypeDeployerFileException("Unsupported attribute format for device definition");
            }
            columnNames.add(attribute.getValue());
        }
    }

    public String getDeviceTableName() {
        return deviceTableName;
    }

    public String getPrimarkey() {
        return primarkey;
    }


}
