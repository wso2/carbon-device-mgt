/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.api.services.PlatformManagementAPI;
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class PlatformManagementAPIImpl implements PlatformManagementAPI {

    private static final String ALL_STATUS = "ALL";
    private static final String ENABLED_STATUS = "ENABLED";
    private static final String DISABLED_STATUS = "DISABLED";

    private static Log log = LogFactory.getLog(PlatformManagementAPIImpl.class);

    @Override
    public Response getPlatforms(@QueryParam("status") String status) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        try {
            List<Platform> platforms = APIUtil.getPlatformManager().getPlatforms(tenantDomain);
            List<Platform> results;
            if (status != null) {
                if (status.contentEquals(ALL_STATUS)) {
                    results = platforms;
                } else if (status.contentEquals(ENABLED_STATUS)) {
                    results = new ArrayList<>();
                    for (Platform platform : platforms) {
                        if (platform.isEnabled()) {
                            results.add(platform);
                        }
                    }
                } else if (status.contentEquals(DISABLED_STATUS)) {
                    results = new ArrayList<>();
                    for (Platform platform : platforms) {
                        if (!platform.isEnabled()) {
                            results.add(platform);
                        }
                    }
                } else {
                    results = platforms;
                }
            } else {
                results = platforms;
            }
            return Response.status(Response.Status.OK).entity(results).build();
        } catch (PlatformManagementException e) {
            log.error("Error while getting the platforms for tenant - " + tenantDomain, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
