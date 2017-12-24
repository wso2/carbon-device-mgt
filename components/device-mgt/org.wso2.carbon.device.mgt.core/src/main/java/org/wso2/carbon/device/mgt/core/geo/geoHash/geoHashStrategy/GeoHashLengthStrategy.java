package org.wso2.carbon.device.mgt.core.geo.geoHash.geoHashStrategy;

import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;

/**
 * This interface is to decide a length for the geohash prefix
 * which will be used to group the clusters based on geohash
 */
public interface GeoHashLengthStrategy {
    int getGeohashLength(GeoCoordinate southWest, GeoCoordinate northEast, int zoom);
}
