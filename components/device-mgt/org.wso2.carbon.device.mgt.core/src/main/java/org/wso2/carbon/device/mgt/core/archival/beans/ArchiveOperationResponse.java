/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.device.mgt.core.archival.beans;

import java.sql.Timestamp;

public class ArchiveOperationResponse {


    private int id;
    private int enrolmentId;
    private int operationId;
    private int enOpMapId;
    private Object operationResponse;
    private Timestamp receivedTimeStamp;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEnrolmentId() {
        return enrolmentId;
    }

    public void setEnrolmentId(int enrolmentId) {
        this.enrolmentId = enrolmentId;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public int getEnOpMapId() {
        return enOpMapId;
    }

    public void setEnOpMapId(int enOpMapId) {
        this.enOpMapId = enOpMapId;
    }

    public Object getOperationResponse() {
        return operationResponse;
    }

    public void setOperationResponse(Object operationResponse) {
        this.operationResponse = operationResponse;
    }

    public Timestamp getReceivedTimeStamp() {
        return receivedTimeStamp;
    }

    public void setReceivedTimeStamp(Timestamp receivedTimeStamp) {
        this.receivedTimeStamp = receivedTimeStamp;
    }
}

