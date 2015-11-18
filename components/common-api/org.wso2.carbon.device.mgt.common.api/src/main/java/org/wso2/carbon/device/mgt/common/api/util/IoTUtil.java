package org.wso2.carbon.device.mgt.common.api.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.device.mgt.common.api.exception.DeviceMgtCommonsException;
import org.wso2.carbon.device.mgt.common.api.internal.DeviceMgtCommonDataHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;


public class IoTUtil {

	private static final Log log = LogFactory.getLog(IoTUtil.class);

	/**
	 * Return a http client instance
	 * @param port - server port
	 * @param protocol- service endpoint protocol http/https
	 * @return
	 */
	public static HttpClient getHttpClient(int port, String protocol)
			throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
				   KeyManagementException {
		SchemeRegistry registry = new SchemeRegistry();

		if ("https".equals(protocol)) {
			System.setProperty("javax.net.ssl.trustStrore", DeviceMgtCommonDataHolder.getInstance().getTrustStoreLocation());
			System.setProperty("javax.net.ssl.trustStorePassword", DeviceMgtCommonDataHolder.getInstance().getTrustStorePassword());

			if (port >= 0) {
				registry.register(new Scheme("https", port, SSLSocketFactory.getSocketFactory()));
			} else {
				registry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
			}
		} else if ("http".equals(protocol)) {
			if (port >= 0) {
				registry.register(new Scheme("http", port, PlainSocketFactory.getSocketFactory()));
			} else {
				registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			}
		}
		HttpParams params = new BasicHttpParams();
		PoolingClientConnectionManager tcm = new PoolingClientConnectionManager(registry);
		HttpClient client = new DefaultHttpClient(tcm, params);
		return client;
	}

	public static String getResponseString(HttpResponse httpResponse) throws
	                                                                  DeviceMgtCommonsException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			String readLine;
			String response = "";
			while (((readLine = br.readLine()) != null)) {
				response += readLine;
			}
			return response;
		} catch (IOException e) {
			throw new DeviceMgtCommonsException("Error while reading the response from the remote. "
														+ e.getMessage(), e);
		} finally {
			EntityUtils.consumeQuietly(httpResponse.getEntity());
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.warn("Error while closing the connection! " + e.getMessage());
				}
			}
		}
	}

}
