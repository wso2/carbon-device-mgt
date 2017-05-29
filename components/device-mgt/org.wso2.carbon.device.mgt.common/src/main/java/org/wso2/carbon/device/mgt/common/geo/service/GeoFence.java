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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class GeoFence.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "fence")
public class GeoFence {

    /** The geoJson. */
    @XmlElement(required = false, name = "geoJson")
    private String geoJson;

    /** The queryName. */
    @XmlElement(required = false, name = "queryName")
    private String queryName;

    /** The areaName. */
    @XmlElement(required = false, name = "areaName")
    private String areaName;

    /** The createdTime. */
    @XmlElement(required = false, nillable = true, name = "createdTime")
    private long createdTime;

    /** The stationaryTime. */
    @XmlElement(required = false, name = "stationaryTime")
    private String stationaryTime;

    /** The fluctuationRadius. */
    @XmlElement(required = false, name = "fluctuationRadius")
    private String fluctuationRadius;

    public String getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(String geoJson) {
        this.geoJson = geoJson;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public void setStationaryTime(String stationaryTime) {
        this.stationaryTime = stationaryTime;
    }

    public String getStationaryTime() {
        return stationaryTime;
    }

    public void setFluctuationRadius(String fluctuationRadius) {
        this.fluctuationRadius = fluctuationRadius;
    }

    public String getFluctuationRadius() {
        return fluctuationRadius;
    }

    @Override
    public String toString() {
        return "{\"geoJson\": " + geoJson +
                ",\"queryName\": " + queryName +
                ",\"areaName\":" + areaName +
                ",\"createdTime\":" + createdTime +
                "}";
    }
}