package org.wso2.carbon.certificate.mgt.core.common;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DataSourceConfig")
public class DataSourceConfig {

    private String url;
    private String driverClassName;
    private String user;
    private String password;

    @Override public String toString() {
        return "DataSourceConfig[" +
                " Url ='" + url + '\'' +
                ", DriverClassName ='" + driverClassName + '\'' +
                ", UserName ='" + user + '\'' +
                ", Password ='" + password + '\'' +
                "]";
    }

    @XmlElement(name = "Url", nillable = false)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name = "DriverClassName", nillable = false)
    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @XmlElement(name = "User", nillable = false)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlElement(name = "Password", nillable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
