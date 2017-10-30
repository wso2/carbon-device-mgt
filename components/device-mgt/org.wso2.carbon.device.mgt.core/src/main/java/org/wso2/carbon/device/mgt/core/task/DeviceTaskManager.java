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


package org.wso2.carbon.device.mgt.core.task;

public interface DeviceTaskManager {

//    /**
//     * This method will get the operation list from configurations.
//     *
//     * @return - list of Task Operations.
//     * @throws DeviceMgtTaskException
//     */
//    List<MonitoringOperation> getOperationList(String deviceType)
//            throws DeviceMgtTaskException;

    /**
     * This method will take the monitoring frequency.
     * @return - integer
     * @throws DeviceMgtTaskException
     */
    int getTaskFrequency() throws DeviceMgtTaskException;

//    /**
//     * This method will return the task clazz from configurations.
//     * @return - Fully qualified class name.
//     * @throws DeviceMgtTaskException
//     */
//    String getTaskImplementedClazz() throws DeviceMgtTaskException;

    /**
     * This method checks wheather task is enabled in config file.
     * @return - return true or false
     * @throws DeviceMgtTaskException
     */
    boolean isTaskEnabled() throws DeviceMgtTaskException;


    /**
     * This method will add the operations to devices
     * @throws DeviceMgtTaskException
     */
    void addOperations() throws DeviceMgtTaskException;


//    /**
//     * This method will return the operation names which should be added in each iterations.
//     * @return
//     * @throws DeviceMgtTaskException
//     */
//    List<String> getValidOperationNames(String deviceType) throws DeviceMgtTaskException;

    /**
     * This method will check wheather give operation is added by the task.
     * @param opName - Operation name
     * @return - true or false
     * @throws DeviceMgtTaskException
     */
    boolean isTaskOperation(String opName);

}
