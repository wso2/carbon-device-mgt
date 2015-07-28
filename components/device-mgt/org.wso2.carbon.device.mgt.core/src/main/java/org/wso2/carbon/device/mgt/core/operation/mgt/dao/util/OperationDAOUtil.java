/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.operation.mgt.dao.util;

import org.wso2.carbon.device.mgt.core.dto.operation.mgt.*;

public class OperationDAOUtil {

    public static Operation convertOperation(org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation) {

        Operation dtoOperation = null;

        if (operation.getType().equals(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Type.COMMAND)) {
            dtoOperation = new CommandOperation();
        } else if (operation.getType().equals(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Type.PROFILE)) {
            dtoOperation = new ProfileOperation();
        }else if(operation.getType().equals(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Type.POLICY)){
            dtoOperation = new PolicyOperation();
        }else if(operation.getType().equals(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Type.CONFIG)) {
            dtoOperation = new ConfigOperation();
        }else{
            dtoOperation = new Operation();
        }

        dtoOperation.setEnabled(operation.isEnabled());
        dtoOperation.setCode(operation.getCode());

        if (operation.getType() != null){
            dtoOperation.setType(Operation.Type.valueOf(operation.getType().toString()));
        }else{
            dtoOperation.setType(null);
        }

        dtoOperation.setCreatedTimeStamp(operation.getCreatedTimeStamp());

        if (operation.getStatus() != null){
            dtoOperation.setStatus(Operation.Status.valueOf(operation.getStatus().toString()));
        }else{
            dtoOperation.setStatus(null);
        }

        dtoOperation.setId(operation.getId());
        dtoOperation.setPayLoad(operation.getPayLoad());
        dtoOperation.setReceivedTimeStamp(operation.getReceivedTimeStamp());
        dtoOperation.setProperties(operation.getProperties());

        return dtoOperation;
    }

    public static org.wso2.carbon.device.mgt.common.operation.mgt.Operation convertOperation(Operation dtoOperation){

        org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation = null;
        
        if (dtoOperation.getType().equals(Operation.Type.COMMAND)){
            operation = new org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation();
        }else if(dtoOperation.getType().equals(Operation.Type.PROFILE)){
            operation = new org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation();
        }else{
            operation = new org.wso2.carbon.device.mgt.common.operation.mgt.Operation();
        }
        operation.setEnabled(dtoOperation.isEnabled());
        operation.setCode(dtoOperation.getCode());

        if(dtoOperation.getType() != null) {
            operation.setType(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Type.valueOf(dtoOperation
                    .getType().toString()));
        }

        operation.setCreatedTimeStamp(dtoOperation.getCreatedTimeStamp());

        if(dtoOperation.getStatus() != null) {
            operation.setStatus(org.wso2.carbon.device.mgt.common.operation.mgt.Operation.Status.valueOf(dtoOperation
                    .getStatus().toString()));
        }

        operation.setId(dtoOperation.getId());
        operation.setPayLoad(dtoOperation.getPayLoad());
        operation.setReceivedTimeStamp(dtoOperation.getReceivedTimeStamp());
        operation.setEnabled(dtoOperation.isEnabled());
        operation.setProperties(dtoOperation.getProperties());

        return operation;
    }
}
