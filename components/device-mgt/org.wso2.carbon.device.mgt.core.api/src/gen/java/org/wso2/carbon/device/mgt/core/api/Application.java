package org.wso2.carbon.device.mgt.core.api;

import org.wso2.msf4j.MicroservicesRunner;

public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner()
                .deploy(new AdminApi())
                .start();
    }
}
