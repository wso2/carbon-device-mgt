package org.wso2.carbon.device.mgt.core.geo.geoHash;

public class GeoCoordinate {
    private double latitude;
    private double longitude;

    public GeoCoordinate(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
