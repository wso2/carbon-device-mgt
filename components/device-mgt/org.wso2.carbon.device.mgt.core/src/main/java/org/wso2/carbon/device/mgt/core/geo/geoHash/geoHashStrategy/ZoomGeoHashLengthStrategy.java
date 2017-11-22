package org.wso2.carbon.device.mgt.core.geo.geoHash.geoHashStrategy;

import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;

/**A class that will decide the geoHashLength based on the zoom level and
* the boundaries of the map**/

public class ZoomGeoHashLengthStrategy implements GeoHashLengthStrategy{

    private int minGeohashLength = 1;
    private int maxGeohashLength = 16;
    private int minZoom = 1;
    private int maxZoom = 17;

    @Override
    public int getGeohashLength(GeoCoordinate southWest, GeoCoordinate northEast, int zoom) {
        double a = minGeohashLength / Math.exp(minZoom / (maxZoom - minZoom) * Math.log(maxGeohashLength / minGeohashLength));
        double b = Math.log(maxGeohashLength / minGeohashLength) / (maxZoom - minZoom);
        return (int) Math.max(minGeohashLength, Math.min(a * Math.exp(b * zoom), maxGeohashLength));
    }

    public void setMinGeohashLength(int minGeohashLength) {
        this.minGeohashLength = minGeohashLength;
    }

    public void setMaxGeohashLength(int maxGeohashLength) {
        this.maxGeohashLength = maxGeohashLength;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public int getMinGeohashLength() {
        return minGeohashLength;
    }

    public int getMaxGeohashLength() {
        return maxGeohashLength;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }
}
