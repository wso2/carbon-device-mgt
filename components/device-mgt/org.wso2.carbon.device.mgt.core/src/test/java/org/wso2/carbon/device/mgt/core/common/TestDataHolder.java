package org.wso2.carbon.device.mgt.core.common;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.Date;
import java.util.Properties;

public class TestDataHolder {

    public static Device initialTestDevice;
    public static DeviceType initialTestDeviceType;
    public static String TEST_DEVICE_TYPE = "Test";
    public static Integer SUPER_TENANT_ID = -1234;
    public static ThreadLocal<Integer> tenant = new ThreadLocal<Integer>();

    public static Device generateDummyDeviceData(String deviceType){

        Device device = new Device();
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(new Date().getTime());
        enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
        enrolmentInfo.setOwner("admin");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.CREATED);
        device.setEnrolmentInfo(enrolmentInfo);
        device.setDescription("Test Description");
        device.setDeviceIdentifier("1234");
        device.setType(deviceType);
        return device;

    }

    public static DeviceType generateDeviceTypeData(String devTypeName){
        DeviceType deviceType = new DeviceType();
        deviceType.setName(devTypeName);
        return deviceType;
    }

    public static Application generateApplicationDummyData(String appIdentifier){

        Application application = new Application();
        Properties properties = new Properties();
        properties.setProperty("test1","testVal");

        application.setName("SimpleCalculator");
        application.setCategory("TestCategory");
        application.setApplicationIdentifier(appIdentifier);
        application.setType("TestType");
        application.setVersion("1.0.0");
        application.setImageUrl("http://test.org/image/");
        application.setLocationUrl("http://test.org/location/");
        application.setAppProperties(properties);

        return application;
    }
}
