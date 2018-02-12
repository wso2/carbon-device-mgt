package org.wso2.carbon.device.mgt.core.geo;

import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;

public class GeoCluster {
    private GeoCoordinate coordinates;
    private GeoCoordinate southWestBound;
    private GeoCoordinate northEastBound;
    private long count;
    private String geohashPrefix;
    private String deviceIdentification;
    private String deviceType;



    public GeoCluster(GeoCoordinate coordinates, GeoCoordinate southWestBound, GeoCoordinate northEastBound, long count,
                      String geohashPrefix, String deviceIdentification, String deviceType){
        this.coordinates=coordinates;
        this.southWestBound=southWestBound;
        this.northEastBound=northEastBound;
        this.count=count;
        this.geohashPrefix=geohashPrefix;
        this.deviceIdentification=deviceIdentification;
        this.deviceType=deviceType;

    }

    public String getGeohashPrefix() {
        return geohashPrefix;
    }

    public long getCount() {
        return count;
    }

    public GeoCoordinate getCoordinates() {
        return coordinates;
    }

    public GeoCoordinate getSouthWestBound() {
        return southWestBound;
    }

    public GeoCoordinate getNorthEastBound() {
        return northEastBound;
    }

    public String getDeviceIdentification() {
        return deviceIdentification;
    }

    public String getDeviceType() { return deviceType;
    }
}
