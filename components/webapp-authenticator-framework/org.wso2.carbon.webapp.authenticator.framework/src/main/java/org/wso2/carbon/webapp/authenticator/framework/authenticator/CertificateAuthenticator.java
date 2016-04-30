package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.scep.SCEPException;
import org.wso2.carbon.device.mgt.core.scep.SCEPManager;
import org.wso2.carbon.device.mgt.core.scep.TenantedDeviceWrapper;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationException;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.Utils.Utils;

import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * This authenticator authenticates HTTP requests using certificates.
 */
public class CertificateAuthenticator implements WebappAuthenticator {

    private static final Log log = LogFactory.getLog(CertificateAuthenticator.class);
    private static final String CERTIFICATE_AUTHENTICATOR = "CertificateAuth";
    private static final String MUTUAL_AUTH_HEADER = "mutual-auth-header";
    private static final String CERTIFICATE_VERIFICATION_HEADER = "certificate-verification-header";
    private static final String CLIENT_CERTIFICATE_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    @Override
    public void init() {

    }

    @Override
    public boolean canHandle(Request request) {
        if (request.getHeader(CERTIFICATE_VERIFICATION_HEADER) != null || request.getHeader(MUTUAL_AUTH_HEADER) !=
                                                                         null) {
            return true;
        }
        return false;
    }

    @Override
    public AuthenticationInfo authenticate(Request request, Response response) {

        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.isEmpty()) {
            authenticationInfo.setStatus(Status.CONTINUE);
        }

        String certVerificationHeader = request.getContext().findParameter(CERTIFICATE_VERIFICATION_HEADER);
        try {

            if (request.getHeader(MUTUAL_AUTH_HEADER) != null) {
                X509Certificate[] clientCertificate = (X509Certificate[]) request.
                                                                        getAttribute(CLIENT_CERTIFICATE_ATTRIBUTE);
                if (clientCertificate != null && clientCertificate[0] != null) {
                    CertificateResponse certificateResponse = AuthenticatorFrameworkDataHolder.getInstance().
                            getCertificateManagementService().verifyPEMSignature(clientCertificate[0]);
                    if (certificateResponse == null) {
                        authenticationInfo.setStatus(Status.FAILURE);
                        authenticationInfo.setMessage("Certificate sent doesn't match any certificate in the store." +
                                                      " Unauthorized access attempt.");
                    } else if (certificateResponse.getCommonName() != null && !certificateResponse.getCommonName().
                            isEmpty()) {
                        authenticationInfo.setTenantId(certificateResponse.getTenantId());
                        authenticationInfo.setStatus(Status.CONTINUE);
                        authenticationInfo.setUsername(certificateResponse.getCommonName());
                        try {
                            authenticationInfo.setTenantDomain(Utils.
                                                                            getTenantDomain(
                                                                                    certificateResponse.getTenantId()));
                        } catch (AuthenticationException e) {
                            authenticationInfo.setStatus(Status.FAILURE);
                            authenticationInfo.setMessage("Could not identify tenant domain.");
                        }
                    } else {
                        authenticationInfo.setStatus(Status.FAILURE);
                        authenticationInfo.setMessage("A matching certificate is found, " +
                                                      "but the serial number is missing in the database.");
                    }

                } else {
                    authenticationInfo.setStatus(Status.FAILURE);
                    authenticationInfo.setMessage("No client certificate is present");
                }
            } else if (request.getHeader(CERTIFICATE_VERIFICATION_HEADER) != null) {

                String certHeader = request.getHeader(certVerificationHeader);
                if (certHeader != null &&
                    AuthenticatorFrameworkDataHolder.getInstance().getCertificateManagementService().
                            verifySignature(certHeader)) {
                    AuthenticatorFrameworkDataHolder.getInstance().getCertificateManagementService().
                            extractCertificateFromSignature(certHeader);
                    X509Certificate certificate =
                            AuthenticatorFrameworkDataHolder.getInstance().getCertificateManagementService().
                                    extractCertificateFromSignature(certHeader);
                    String challengeToken = AuthenticatorFrameworkDataHolder.getInstance().
                            getCertificateManagementService().extractChallengeToken(certificate);

                    if (challengeToken != null) {
                        challengeToken = challengeToken.substring(challengeToken.indexOf("(") + 1).trim();
                        SCEPManager scepManager = AuthenticatorFrameworkDataHolder.getInstance().getScepManager();
                        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                        deviceIdentifier.setId(challengeToken);
                        deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
                        TenantedDeviceWrapper tenantedDeviceWrapper = scepManager.getValidatedDevice(deviceIdentifier);
                        authenticationInfo.setTenantDomain(tenantedDeviceWrapper.getTenantDomain());
                        authenticationInfo.setTenantId(tenantedDeviceWrapper.getTenantId());

                        if (tenantedDeviceWrapper.getDevice() != null &&
                            tenantedDeviceWrapper.getDevice().getEnrolmentInfo() != null) {

                            EnrolmentInfo enrolmentInfo = tenantedDeviceWrapper.getDevice().getEnrolmentInfo();
                            authenticationInfo.setUsername(enrolmentInfo.getOwner());
                        }

                        authenticationInfo.setStatus(Status.CONTINUE);
                    }
                }
            }
        } catch (KeystoreException e) {
            log.error("KeystoreException occurred ", e);
        } catch (SCEPException e) {
            log.error("SCEPException occurred ", e);
        }
        return authenticationInfo;
    }

    @Override
    public String getName() {
        return CERTIFICATE_AUTHENTICATOR;
    }

    @Override
    public void setProperties(Properties properties) {

    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

}
