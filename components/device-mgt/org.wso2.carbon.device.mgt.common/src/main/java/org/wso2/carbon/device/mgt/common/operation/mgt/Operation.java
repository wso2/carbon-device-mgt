/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.common.operation.mgt;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;

@XmlRootElement
public class Operation {

	public enum Type {
		CONFIG, MESSAGE, INFO, COMMAND
	}
    public enum OperationStatuses {
        INPROGRESS,PENDING,COMPLETED,ERROR
    }

	private String code;
	private Properties properties;
	private Type type;
    private Long operationId;
    private String payLoad;
    private OperationStatuses operationStates;

	@XmlElement
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XmlElement
	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@XmlElement
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(String payLoad) {
        this.payLoad = payLoad;
    }

    public OperationStatuses getOperationStates() {
        return operationStates;
    }

    public void setOperationStates(OperationStatuses operationStates) {
        this.operationStates = operationStates;
    }
}