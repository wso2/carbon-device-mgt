package org.wso2.carbon.device.mgt.core.geo.geoHash.geoHashStrategy;

import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;

public interface GeoHashLengthStrategy {
    int getGeohashLength(GeoCoordinate southWest, GeoCoordinate northEast, int zoom);
}
