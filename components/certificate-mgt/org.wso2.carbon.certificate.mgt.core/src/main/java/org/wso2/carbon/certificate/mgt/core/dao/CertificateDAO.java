/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.certificate.mgt.core.dao;

import java.io.ByteArrayInputStream;

/**
 * This class represents the key operations associated with persisting certificate related
 * information.
 */
public interface CertificateDAO {

    /**
     * This can be used to store a certificate in the database, where it will be stored against the serial number
     * of the certificate.
     * @param byteArrayInputStream  Holds the certificate.
     * @param serialNumber          Serial number of the certificate.
     * @throws CertificateManagementDAOException
     */
    void addCertificate(ByteArrayInputStream byteArrayInputStream, String serialNumber
    ) throws CertificateManagementDAOException;

    /**
     * Usage is to obtain a certificate stored in the database by providing the serial number.
     * @param serialNumber  Serial number of the certificate.
     * @return              representation of the certificate.
     * @throws CertificateManagementDAOException
     */
    byte[] retrieveCertificate(String serialNumber
    ) throws CertificateManagementDAOException;

}
