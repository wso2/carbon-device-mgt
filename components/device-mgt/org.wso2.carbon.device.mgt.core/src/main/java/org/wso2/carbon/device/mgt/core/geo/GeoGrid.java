package org.wso2.carbon.device.mgt.core.geo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.device.details.mgt.impl.DeviceInformationManagerImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GeoGrid {
    private static Log log = LogFactory.getLog(GeoRectangle.class);
    DeviceInformationManager deviceInformationManagerService = new DeviceInformationManagerImpl();
    private int horizontalDivisions;
    private int verticalDivisions;
    private double minLat;
    private double maxLat;
    private double minLong;
    private double maxLong;
    private double latDistance;
    private double longitudeDistance;
    private double latIncrement;
    private double longIncrement;
    private ArrayList<GeoRectangle> geoRectangles = new ArrayList<>();

    public GeoGrid(int horizontalDivisions, int verticalDivisions, double minLat, double maxLat, double minLong,
                   double maxLong) {
        this.horizontalDivisions = horizontalDivisions;
        this.verticalDivisions = verticalDivisions;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLong = minLong;
        this.maxLong = maxLong;
        this.latDistance = maxLat - minLat;
        this.longitudeDistance = maxLong - minLong;
        this.latIncrement = this.latDistance / this.horizontalDivisions;
        this.longIncrement = this.longitudeDistance / this.verticalDivisions;
        this.createGeoRectangles();
    }

    private void createGeoRectangles() {
        double minRectangleLat;
        double maxRectangleLat;
        double minRectangleLong;
        double maxRectangleLong;

        for (int i = 0; i < verticalDivisions; i++) {
            minRectangleLong = this.minLong + i * longIncrement;
            if (i + 1 == verticalDivisions) {
                maxRectangleLong = this.maxLong;
            } else {
                maxRectangleLong = this.minLong + (i + 1) * longIncrement;
            }

            for (int m = 0; m < horizontalDivisions; m++) {
                minRectangleLat = this.minLat + m * latIncrement;
                maxRectangleLat = this.minLat + (m + 1) * latIncrement;
                geoRectangles.add(new GeoRectangle(minRectangleLat, maxRectangleLat, minRectangleLong, maxRectangleLong));
            }
        }

    }

    public ArrayList<GeoRectangle> getGeoRectangles() {
        return geoRectangles;
    }

    public ArrayList<Device> getDevicesInGeoGrid(List<Device> devices) {
        ArrayList<Device> devicesInGeoGrid = new ArrayList<>();
        for (Device device : devices) {
            DeviceLocation location = null;
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            try {
                location = deviceInformationManagerService.getDeviceLocation(deviceIdentifier);
            } catch (DeviceDetailsMgtException e) {
                String msg = "Exception occurred while retrieving device location." + deviceIdentifier;
                log.error(msg, e);
                return devicesInGeoGrid;
            }
            double locationLat = location.getLatitude();
            double locationLong = location.getLongitude();
            if (locationLat >= minLat && locationLat < maxLat && locationLong >= minLong && locationLong < maxLong) {
                devicesInGeoGrid.add(device);

            }
        }
        return devicesInGeoGrid;
    }

    public ArrayList<GeoRectangle> placeDevicesInGeoRectangles(ArrayList<Device> devicesInGeoGrid) {
        ArrayList<Device> remainingDevicesInGeoGrid = devicesInGeoGrid;
        for (GeoRectangle geoRectangle : geoRectangles) {
            Iterator<Device> remainingDevicesIterator = remainingDevicesInGeoGrid.iterator();
            while (remainingDevicesIterator.hasNext()) {
                Device currentDevice = remainingDevicesIterator.next();
                if (geoRectangle.isDeviceInGeoRectangle(currentDevice)) {
                    geoRectangle.addDevice(currentDevice);
                }
            }

        }
        return geoRectangles;
    }

}
