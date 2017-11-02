package org.wso2.carbon.device.mgt.core;
import org.wso2.msf4j.MicroservicesRunner;

public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner()
                .deploy(new DevicesApi())
                .start();
    }
}
