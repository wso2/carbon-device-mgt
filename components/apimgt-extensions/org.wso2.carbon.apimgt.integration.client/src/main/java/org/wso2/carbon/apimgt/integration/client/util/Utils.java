/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.integration.client.util;

import okhttp3.OkHttpClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    private static final String KEY_STORE_TYPE = "JKS";
    /**
     * Default truststore type of the client
     */
    private static final String TRUST_STORE_TYPE = "JKS";
    /**
     * Default keymanager type of the client
     */
    private static final String KEY_MANAGER_TYPE = "SunX509"; //Default Key Manager Type
    /**
     * Default trustmanager type of the client
     */
    private static final String TRUST_MANAGER_TYPE = "SunX509"; //Default Trust Manager Type

    private static final String SSLV3 = "SSLv3";

    private static final String DEFAULT_HOST = "localhost";

    private static final String DEFAULT_HOST_IP = "127.0.0.1";


    //This method is only used if the mb features are within DAS.
    public static String replaceProperties(String text) {
        String regex = "\\$\\{(.*?)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matchPattern = pattern.matcher(text);
        while (matchPattern.find()) {
            String sysPropertyName = matchPattern.group(1);
            String sysPropertyValue = System.getProperty(sysPropertyName);
            if (sysPropertyValue != null && !sysPropertyName.isEmpty()) {
                text = text.replaceAll("\\$\\{(" + sysPropertyName + ")\\}", sysPropertyValue);
            }
        }
        return text;
    }

    public static OkHttpClient getSSLClient() {

        boolean isIgnoreHostnameVerification = Boolean.parseBoolean(System.getProperty("org.wso2"
                + ".ignoreHostnameVerification"));
        OkHttpClient okHttpClient;
        final String proxyHost = System.getProperty("http.proxyHost");
        final String proxyPort = System.getProperty("http.proxyPort");
        final String nonProxyHostsValue = System.getProperty("http.nonProxyHosts");

        final ProxySelector proxySelector = new ProxySelector() {
            @Override
            public java.util.List<Proxy> select(URI uri) {
                List<Proxy> proxyList = new ArrayList<>();
                String host = uri.getHost();

                if (!StringUtils.isEmpty(host)) {
                    if (host.startsWith(DEFAULT_HOST_IP) || host.startsWith(DEFAULT_HOST) || StringUtils
                            .isEmpty(nonProxyHostsValue) || StringUtils.contains(nonProxyHostsValue, host) ||
                            StringUtils.isEmpty(proxyHost) || StringUtils.isEmpty(proxyPort)) {
                        proxyList.add(Proxy.NO_PROXY);
                    } else {
                        proxyList.add(new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))));
                    }
                } else {
                    log.error("Host is null. Host could not be empty or null");
                }
                return proxyList;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        X509TrustManager trustAllCerts = new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                };
        if(isIgnoreHostnameVerification) {
            okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(getSimpleTrustedSSLSocketFactory(), trustAllCerts)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    }).proxySelector(proxySelector).build();
            return okHttpClient;
        }else {
            SSLSocketFactory trustedSSLSocketFactory = getTrustedSSLSocketFactory();
            okHttpClient = new OkHttpClient.Builder().sslSocketFactory(trustedSSLSocketFactory)
                    .proxySelector(proxySelector).build();
            return okHttpClient;
        }
    }

    private static SSLSocketFactory getSimpleTrustedSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }

    }

    private static SSLSocketFactory getTrustedSSLSocketFactory() {
        try {
            String keyStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Password");
            String keyStoreLocation = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Location");
            String trustStorePassword = ServerConfiguration.getInstance().getFirstProperty(
                    "Security.TrustStore.Password");
            String trustStoreLocation = ServerConfiguration.getInstance().getFirstProperty(
                    "Security.TrustStore.Location");
            KeyStore keyStore = loadKeyStore(keyStoreLocation,keyStorePassword,KEY_STORE_TYPE);
            KeyStore trustStore = loadTrustStore(trustStoreLocation,trustStorePassword);

            return initSSLConnection(keyStore,keyStorePassword,trustStore);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                |CertificateException | IOException | UnrecoverableKeyException e) {
            log.error("Error while creating the SSL socket factory due to "+e.getMessage(),e);
            return null;
        }

    }

    private static SSLSocketFactory initSSLConnection(KeyStore keyStore,String keyStorePassword,KeyStore trustStore) throws NoSuchAlgorithmException, UnrecoverableKeyException,
            KeyStoreException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext for HTTPS communication
        SSLContext sslContext = SSLContext.getInstance(SSLV3);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
        return  sslContext.getSocketFactory();
    }


    private static KeyStore loadKeyStore(String keyStorePath, String ksPassword,String type)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        InputStream fileInputStream = null;
        try {
            char[] keypassChar = ksPassword.toCharArray();
            KeyStore keyStore = KeyStore.getInstance(type);
            fileInputStream = new FileInputStream(keyStorePath);
            keyStore.load(fileInputStream, keypassChar);
            return keyStore;
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    private static KeyStore loadTrustStore(String trustStorePath, String tsPassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        return loadKeyStore(trustStorePath,tsPassword,TRUST_STORE_TYPE);
    }
}