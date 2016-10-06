/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupList;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.GroupManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.core.Response;
import java.util.List;

public class GroupManagementAdminServiceImpl implements GroupManagementAdminService {

    private static final Log log = LogFactory.getLog(GroupManagementAdminServiceImpl.class);

    @Override
    public Response getGroups(String name, String owner, int offset, int limit) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            GroupPaginationRequest request = new GroupPaginationRequest(offset, limit);
            request.setGroupName(name);
            request.setOwner(owner);
            PaginationResult deviceGroupsResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getGroups(request);
            if (deviceGroupsResult.getData() != null && deviceGroupsResult.getRecordsTotal() > 0) {
                DeviceGroupList deviceGroupList = new DeviceGroupList();
                deviceGroupList.setList(deviceGroupsResult.getData());
                deviceGroupList.setCount(deviceGroupsResult.getRecordsTotal());
                return Response.status(Response.Status.OK).entity(deviceGroupList).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            String msg = "ErrorResponse occurred while retrieving all groups.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getGroupCount() {
        try {
            int count = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroupCount();
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            String msg = "ErrorResponse occurred while retrieving group count.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
