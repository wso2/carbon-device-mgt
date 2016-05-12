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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

@XmlRootElement
@ApiModel(value = "Operation", description = "This class carries all information related to a operations that can be " +
                                             "applied on a device.")
public class Operation implements Serializable {

	public enum Type {
		CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY
	}

    public enum Status {
        IN_PROGRESS, PENDING, COMPLETED, ERROR, REPEATED
    }

    public enum Control {
        REPEAT, NO_REPEAT, PAUSE_SEQUENCE, STOP_SEQUENCE
    }

    @ApiModelProperty(name = "code", value = "The code of the operation that you carried out. For example the code of" +
                                             "  the operation carried out to device info operation is DEVICE_INFO.",
                      required = true)
    private String code;
    @ApiModelProperty(name = "properties", value = "Properties  of an operation containing meta information.",
                      required = true)
    private Properties properties;
    @ApiModelProperty(name = "type", value = "The operation type that was carried out on the device. " +
                                             "The operations types can be one of the following: COMMAND, PROFILE",
                      required = true)
    private Type type;
    @ApiModelProperty(name = "id", value = "The operations carried out on a device is recorded in a database table. " +
                                           "The ID of the operation in the database table is given as the ID " +
                                           "in the output.",
                      required = true)
    private int id;
    @ApiModelProperty(name = "status", value = "The status of the operation that has been carried out on a device. The" +
                                           " operation status can be any one of the following:\n" +
                                           "IN-PROGRESS - The operation is processing on the EMM server" +
                                           " side and has not yet been delivered to the device.\n" +
                                           "PENDING - The operation is delivered to the device but the response " +
                                           "from the device is pending.\n" +
                                           "COMPLETED - The operation is delivered to the device and the server has " +
                                           "received a response back from the device.\n" +
                                           "ERROR - An error has occurred while carrying out the operation.",
                      required = true)
    private Status status;
    @ApiModelProperty(name = "control", value = "How the operation should be executed.", required = true)
    private Control control;
    @ApiModelProperty(name = "receivedTimeStamp", value = "The time WSO2 EMM received the response from the device.",
                      required = true)
    private String receivedTimeStamp;
    @ApiModelProperty(name = "createdTimeStamp", value = "The time when the operation was requested to be carried out.",
                      required = true)
    private String createdTimeStamp;
    @ApiModelProperty(name = "isEnabled", value = "If the assigned value is true it indicates that a policy is " +
                                                  "enforced on the device. If the assigned value is false it indicates" +
                                                  " that a policy is not enforced on a device.", required = true)
    private boolean isEnabled;
    @ApiModelProperty(name = "payLoad", value = "Payload of the operation to be sent to the device", required = true)
    private Object payLoad;
    @ApiModelProperty(name = "operationResponse", value = "Response received from the device", required = true)
    private String operationResponse;
    @ApiModelProperty(name = "activityId", value = "The identifier used to identify the operation uniquely.",
                      required = true)
    private String activityId;
    private List<OperationResponse> responses;

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
        if (operationResponse != null ? !operationResponse
                .equals(operation.operationResponse) : operation.operationResponse != null) {
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

        if(control != operation.control){
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
        result = 31 * result + (operationResponse != null ? operationResponse.hashCode() : 0);
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

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
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

    public String getOperationResponse() {
        return operationResponse;
    }

    public void setOperationResponse(String operationResponse) {
        this.operationResponse = operationResponse;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public List<OperationResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<OperationResponse> responses) {
        this.responses = responses;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "code='" + code + '\'' +
                ", type=" + type +
                ", id=" + id +
                ", status=" + status +
                ", control=" + control +
                ", receivedTimeStamp='" + receivedTimeStamp + '\'' +
                ", createdTimeStamp='" + createdTimeStamp + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }

}