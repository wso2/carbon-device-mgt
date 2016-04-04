package org.wso2.carbon.certificate.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class TenantUtil {

    private static final Log log = LogFactory.getLog(TenantUtil.class);

    public static int getTenanntId(String tenantDomain) throws CertificateManagementException {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
}
