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
import java.io.Serializable;
import java.util.Properties;

@XmlRootElement
public class Operation implements Serializable {

	public enum Type {
		CONFIG, MESSAGE, INFO, COMMAND, PROFILE
	}

    public enum Status {
        IN_PROGRESS, PENDING, COMPLETED, ERROR
    }

    private String code;
    private Properties properties;
    private Type type;
    private int id;
    private Status status;
    private String receivedTimeStamp;
    private String createdTimeStamp;
    private boolean isEnabled;
    private Object payLoad;

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Operation operation = (Operation) o;

        if (id != operation.id) {
            return false;
        }
        if (isEnabled != operation.isEnabled) {
            return false;
        }
        if (!code.equals(operation.code)) {
            return false;
        }
        if (createdTimeStamp != null ?
                !createdTimeStamp.equals(operation.createdTimeStamp) :
                operation.createdTimeStamp != null) {
            return false;
        }
        if (payLoad != null ? !payLoad.equals(operation.payLoad) : operation.payLoad != null) {
            return false;
        }
        if (properties != null ? !properties.equals(operation.properties) : operation.properties != null) {
            return false;
        }
        if (!receivedTimeStamp.equals(operation.receivedTimeStamp)) {
            return false;
        }
        if (status != operation.status) {
            return false;
        }
        if (type != operation.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + type.hashCode();
        result = 31 * result + id;
        result = 31 * result + status.hashCode();
        result = 31 * result + receivedTimeStamp.hashCode();
        result = 31 * result + (createdTimeStamp != null ? createdTimeStamp.hashCode() : 0);
        result = 31 * result + (isEnabled ? 1 : 0);
        result = 31 * result + (payLoad != null ? payLoad.hashCode() : 0);
        return result;
    }

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReceivedTimeStamp() {
        return receivedTimeStamp;
    }

    public void setReceivedTimeStamp(String receivedTimeStamp) {
        this.receivedTimeStamp = receivedTimeStamp;
    }

    public String getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(String createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Object getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(Object payLoad) {
        this.payLoad = payLoad;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "code='" + code + '\'' +
                ", type=" + type +
                ", id=" + id +
                ", status=" + status +
                ", receivedTimeStamp='" + receivedTimeStamp + '\'' +
                ", createdTimeStamp='" + createdTimeStamp + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }

}