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


package org.wso2.carbon.device.mgt.common.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Condition", description = "Contains the advance search parameters.")
public class Condition {

    @ApiModelProperty(name = "key", value = "Provide the operation code. You can assign the following operation " +
                                                   "codes:\n" +
                                                   "DEVICE_MODEL : The model of the device.\n" +
                                                   "VENDOR : The name of the device vendor.\n" +
                                                   "OS_VERSION : The version of the device operating system.\n" +
                                                   "BATTERY_LEVEL : The current level of the device battery.\n" +
                                                   "INTERNAL_TOTAL_MEMORY : The total capacity of the internal memory" +
                                                   " available in the device.\n" +
                                                   "INTERNAL_AVAILABLE_MEMORY : The internal memory in the device " +
                                                   "that is available.\n" +
                                                   "EXTERNAL_TOTAL_MEMORY : The total capacity of the external memory " +
                                                   "available in the device.\n" +
                                                   "EXTERNAL_AVAILABLE_MEMORY : The external memory in the device" +
                                                   " that is available.\n" +
                                                   "CONNECTION_TYPE : Define if the device is connected to the GPRS " +
                                                   "or Wi-Fi settings.\n" +
                                                   "SSID : The name of the Wifi network that the device is " +
                                                   "connected to.\n" +
                                                   "CPU_USAGE : The current CPU usage of the mobile device.\n" +
                                                   "TOTAL_RAM_MEMORY : The total capacity of the random access " +
                                                   "memory available in the device.\n" +
                                                   "AVAILABLE_RAM_MEMORY : The random access memory capacity " +
                                                   "in the device that is available.\n" +
                                                   "PLUGGED_IN : Define true if the device is plugged in for charging " +
                                                   "or define false if the device is not plugged in for charging.",
                      required = true)
    private String key;
    @ApiModelProperty(name = "value", value = "Define the value for the key you provide.\n" +
                                              "Example: If you provide the key as VERSION, you can provide the " +
                                              "value as 5.1, which indicates the version of the mobile device you" +
                                              " are searching.",
                      required = true)
    private String value;
    @ApiModelProperty(name = "operator", value = "Define the search condition between the key and the value you " +
                                                 "provide. The following values can be used to define the search " +
                                                 "condition:\n" +
                                                 "= : Searches for devices where the key is equal to the value " +
                                                 "provided.\n" +
                                                 "=! : Searches for devices where the key is not equal to the " +
                                                 "value provided.\n" +
                                                 "<= : Searches for devices where the key is greater than or equal" +
                                                 " to the value provide.\n" +
                                                 ">= : Searches for devices where the key is less than or equal to" +
                                                 " the value provided.\n" +
                                                 "> : Searches for devices where the key is greater than the value" +
                                                 " provided.\n" +
                                                 "< : Searches for devices where the key is less than the value " +
                                                 "provided.\n" +
                                                 "Example: If you wish to get the devises that have the version " +
                                                 "as 5.1, you need to use the = operator..",
                      required = true)
    public String operator;

    @ApiModelProperty(name = "state", value = "There can be many search options as shown in the sample JSON " +
                                                   "definition. The field that connects the independent search " +
                                                   "options, is known as state.\n" +
                                                   "The following values can be assigned to state.\n" +
                                                   "AND : Defines if you want the search result to match all the " +
                                                   "search conditions provided.\n" +
                                                   "OR : Defines if you want the search result to match either of" +
                                                   " the search conditions provided.",
                      required = true)
    private State state;

    public enum State {
        AND, OR;
    };

    public State getState(){
        return state;
    }

    public void setState(State state){
        this.state = state;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}

