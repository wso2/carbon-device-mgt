package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Feature;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Operation;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.extensions.internal.DeviceTypeExtensionDataHolder;
import org.wso2.carbon.device.mgt.extensions.license.mgt.registry.RegistryBasedLicenseManager;
import org.wso2.carbon.device.mgt.extensions.utils.Utils;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.FileUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.governance.api.util.GovernanceUtils.getGovernanceArtifactConfiguration;

/**
 * This test case contains the tests for {@link HTTPDeviceTypeManagerService}
 */
public class HttpDeviceTypeManagerServiceTest {

    @BeforeTest
    public void setup() throws RegistryException, IOException, SAXException, ParserConfigurationException,
            DeviceTypeConfigurationException, JAXBException {


    }

    private DeviceTypeMetaDefinition sampleDeviceTypeMetaDefinition() {
        DeviceTypeMetaDefinition deviceTypeMetaDefinition = new DeviceTypeMetaDefinition();
        Feature feature = new Feature();
        Operation operation = new Operation();
        operation.setContext("/test");
        operation.setMethod("Get");
        operation.setType("COMMAND");
        feature.setCode("TEST");
        feature.setDescription("This is a test feature");
        feature.setName("TEST");
        deviceTypeMetaDefinition.setClaimable(true);
        deviceTypeMetaDefinition.setDescription("This is a new device type");
//        deviceTypeMetaDefinition.setInitialOperationConfig();
        return deviceTypeMetaDefinition;
    }
}
