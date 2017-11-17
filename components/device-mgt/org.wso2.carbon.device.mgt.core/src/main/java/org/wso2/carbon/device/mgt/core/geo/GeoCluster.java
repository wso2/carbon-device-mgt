package org.wso2.carbon.device.mgt.core.geo;

import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;

public class GeoCluster {
    private GeoCoordinate coordinates;
    private long count;
    private String geohashPrefix;
    private String deviceId;

    public GeoCluster(GeoCoordinate coordinates,long count,String geohashPrefix){
        this.coordinates=coordinates;
        this.count=count;
        this.geohashPrefix=geohashPrefix;
    }

    public long getCount() {
        return count;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
