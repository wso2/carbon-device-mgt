mvn clean install -Dmaven.test.skip=true -f components/application-mgt/org.wso2.carbon.device.application.mgt.common/pom.xml

mvn clean install -Dmaven.test.skip=true -f components/application-mgt/org.wso2.carbon.device.application.mgt.core/pom.xml

mvn clean install -Dmaven.test.skip=true -f components/application-mgt/org.wso2.carbon.device.application.mgt.api/pom.xml

rm ~/projects/wso2/iot/testing/appm/310appm/wso2iot-3.1.0-SNAPSHOT/patches/patch0101/ -r

mkdir ~/projects/wso2/iot/testing/appm/310appm/wso2iot-3.1.0-SNAPSHOT/patches/patch0101

cp components/application-mgt/org.wso2.carbon.device.application.mgt.common/target/org.wso2.carbon.device.application.mgt.common-*.jar ~/projects/wso2/iot/testing/appm/310appm/wso2iot-3.1.0-SNAPSHOT/patches/patch0101/

cp components/application-mgt/org.wso2.carbon.device.application.mgt.core/target/org.wso2.carbon.device.application.mgt.core-*.jar ~/projects/wso2/iot/testing/appm/310appm/wso2iot-3.1.0-SNAPSHOT/patches/patch0101/

rm ~/projects/wso2/iot/testing/appm/310appm/wso2iot-3.1.0-SNAPSHOT/repository/deployment/server/webapps/api#application-mgt#v1.0.war

rm ~/projects/wso2/iot/testing/appm/310appm/wso2iot-3.1.0-SNAPSHOT/repository/deployment/server/webapps/api#application-mgt#v1.0/ -r

cp components/application-mgt/org.wso2.carbon.device.application.mgt.api/target/api#application-mgt#v1.0.war ~/projects/wso2/iot/testing/appm/310appm/wso2iot-3.1.0-SNAPSHOT/repository/deployment/server/webapps/
