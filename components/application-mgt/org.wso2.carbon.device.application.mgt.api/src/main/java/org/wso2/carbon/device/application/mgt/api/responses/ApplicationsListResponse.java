/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.beanutils.BeanUtils;
import org.wso2.carbon.device.application.mgt.api.dto.StoreApplication;
import org.wso2.carbon.device.application.mgt.core.dto.Application;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ApplicationsListResponse {

    @ApiModelProperty(value = "List of applications types returned")
    @JsonProperty("applications")
    @XmlElement
    private List<StoreApplication> applications;

    public ApplicationsListResponse(List<org.wso2.carbon.device.application.mgt.core.dto.StoreApplication> applications)
            throws InvocationTargetException, IllegalAccessException {
        this.applications = new ArrayList<>();
        for(org.wso2.carbon.device.application.mgt.core.dto.StoreApplication applicationDTO : applications){
            StoreApplication application = new StoreApplication();
           BeanUtils.copyProperties(application, applicationDTO);
            this.applications.add(application);
        }
    }

    public List<StoreApplication> getApplications() {
        return applications;
    }

    public void setApplications(List<StoreApplication> applications) {
        this.applications = applications;
    }

}
