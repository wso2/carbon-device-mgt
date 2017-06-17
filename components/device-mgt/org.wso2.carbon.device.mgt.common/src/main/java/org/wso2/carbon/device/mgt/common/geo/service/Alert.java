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
package org.wso2.carbon.device.mgt.common.geo.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class Alert Bean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "alert")
public class Alert {

    /**
     * The parse data.
     */
    @XmlElement(required = true, name = "parseData")
    private String parseData;

    /**
     * The execution plan name.
     */
    @XmlElement(required = true, name = "executionPlan")
    private String executionPlan;

    /**
     * The custom name.
     */
    @XmlElement(required = false, nillable = true, name = "customName")
    private String customName;

    /**
     * The query name.
     */
    @XmlElementWrapper(required = true, name = "queryName")
    private String queryName;

    /**
     * The CEP action.
     */
    @XmlElementWrapper(required = true, name = "cepAction")
    private String cepAction;

    /**
     * The device id.
     */
    @XmlElementWrapper(required = true, name = "deviceId")
    private String deviceId;

    /**
     * The stationery time.
     */
    @XmlElementWrapper(required = false, nillable = true, name = "stationeryTime")
    private String stationeryTime;

    /**
     * The fluctuation radius.
     */
    @XmlElementWrapper(required = false, nillable = true, name = "fluctuationRadius")
    private String fluctuationRadius;

    /**
     * The proximity distance.
     */
    @XmlElementWrapper(required = false, nillable = true, name = "proximityDistance")
    private String proximityDistance;

    /**
     * The proximity time.
     */
    @XmlElementWrapper(required = false, nillable = true, name = "proximityTime")
    private String proximityTime;

    public String getParseData() {
        return parseData;
    }

    public void setParseData(String parseData) {
        this.parseData = parseData;
    }

    public String getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(String executionPlan) {
        this.executionPlan = executionPlan;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getCepAction() {
        return cepAction;
    }

    public void setCepAction(String cepAction) {
        this.cepAction = cepAction;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStationeryTime() {
        return stationeryTime;
    }

    public void setStationeryTime(String stationeryTime) {
        this.stationeryTime = stationeryTime;
    }

    public String getFluctuationRadius() {
        return fluctuationRadius;
    }

    public void setFluctuationRadius(String fluctuationRadius) {
        this.fluctuationRadius = fluctuationRadius;
    }

    public String getProximityDistance() {
        return proximityDistance;
    }

    public void setProximityDistance(String proximityDistance) {
        this.proximityDistance = proximityDistance;
    }

    public String getProximityTime() {
        return proximityTime;
    }

    public void setProximityTime(String proximityTime) {
        this.proximityTime = proximityTime;
    }

    @Override
    public String toString() {
        return String.format(
                "{\"queryName\" : %s,\"customName\" : %s,\"cepAction\" : %s,\"deviceId\" : %s }",
                queryName, customName, cepAction, deviceId);
    }
}