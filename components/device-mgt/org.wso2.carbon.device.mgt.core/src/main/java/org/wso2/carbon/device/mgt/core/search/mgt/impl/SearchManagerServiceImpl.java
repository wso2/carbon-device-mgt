/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.device.mgt.core.search.mgt.impl;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.search.mgt.Processor;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;

import java.util.List;

public class SearchManagerServiceImpl implements SearchManagerService {

    private Processor processor;

    public SearchManagerServiceImpl() {
        processor = new ProcessorImpl();
    }

    @Override
    public List<Device> search(SearchContext searchContext) throws SearchMgtException {
        return processor.execute(searchContext);
    }

    @Override
    public List<Device> getUpdated(long epochTime) throws SearchMgtException {
        return processor.getUpdatedDevices(epochTime);
    }
    @Override
	public List<Device> getPolicyDevice(List<String> roles, List<String> user, List<String> grpId)
			throws SearchMgtException {
		// TODO Auto-generated method stub
		return processor.getPolicyDevice(roles, user, grpId);
	}

}

