package org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans;

/**
 * Created by hasunie on 5/26/16.
 */
public class EnrollmentCertificate {
    String serial;
    String pem;
    int tenantId;

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getPem() {
        return pem;
    }

    public void setPem(String pem) {
        this.pem = pem;
    }
}
