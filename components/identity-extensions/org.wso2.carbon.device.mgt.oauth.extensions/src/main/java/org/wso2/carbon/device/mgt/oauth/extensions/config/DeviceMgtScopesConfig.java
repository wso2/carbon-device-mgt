
package org.wso2.carbon.device.mgt.oauth.extensions.config;

import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.oauth.extensions.OAuthExtUtils;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the configuration that are needed for scopes to permission map.
 */
public class DeviceMgtScopesConfig {

    private static DeviceMgtScopesConfig config = new DeviceMgtScopesConfig();
    private static Map<String, String[]> actionPermissionMap = new HashMap<>();

    private static final String DEVICE_MGT_SCOPES_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "device-mgt-scopes.xml";

    private DeviceMgtScopesConfig() {
    }

    public static DeviceMgtScopesConfig getInstance() {
        return config;
    }

    public static void init() throws DeviceMgtScopesConfigurationFailedException {
        try {
            File deviceMgtConfig = new File(DEVICE_MGT_SCOPES_CONFIG_PATH);
            Document doc = OAuthExtUtils.convertToDocument(deviceMgtConfig);

            /* Un-marshaling DeviceMGtScope configuration */
            JAXBContext ctx = JAXBContext.newInstance(DeviceMgtScopes.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            DeviceMgtScopes deviceMgtScopes = (DeviceMgtScopes) unmarshaller.unmarshal(doc);
            if (deviceMgtScopes != null) {
                for (Action action : deviceMgtScopes.getAction()) {
                    Permissions permissions = action.getPermissions();
                    if (permissions != null) {
                        String permission[] = new String[permissions.getPermission().size()];
                        int i = 0;
                        for (String perm : permissions.getPermission()) {
                            permission[i] = perm;
                            i++;
                        }
                        actionPermissionMap.put(action.getName(), permission);
                    }
                }
            }
        } catch (JAXBException e) {
            throw new DeviceMgtScopesConfigurationFailedException("Error occurred while un-marshalling Device Scope" +
                                                                          " Config", e);
        }
    }

    public Map<String, String[]> getDeviceMgtScopePermissionMap() {
        return actionPermissionMap;
    }

}
