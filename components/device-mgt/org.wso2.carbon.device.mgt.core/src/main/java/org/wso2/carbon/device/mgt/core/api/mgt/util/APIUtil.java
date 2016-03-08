package org.wso2.carbon.device.mgt.core.api.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import javax.net.ssl.HostnameVerifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class APIUtil {

	private static final Log log = LogFactory.getLog(APIUtil.class);

	/**
	 * Return a http client instance
	 * @param port - server port
	 * @param protocol- service endpoint protocol http/https
	 * @return
	 */
	public static HttpClient getHttpClient(int port, String protocol) throws IOException{
		SchemeRegistry registry = new SchemeRegistry();
		if ("https".equals(protocol)) {
			// Setup the HTTPS settings to accept any certificate.
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			if (port >= 0) {
				registry.register(new Scheme(protocol, port, socketFactory));
			} else {
				registry.register(new Scheme(protocol, 443, socketFactory));
			}
		} else if ("http".equals(protocol)) {
			if (port >= 0) {
				registry.register(new Scheme(protocol, port, PlainSocketFactory.getSocketFactory()));
			} else {
				registry.register(new Scheme(protocol, 80, PlainSocketFactory.getSocketFactory()));
			}
		}
		HttpParams params = new BasicHttpParams();
		PoolingClientConnectionManager poolingClientConnectionManager = new PoolingClientConnectionManager(registry);
		HttpClient client = new DefaultHttpClient(poolingClientConnectionManager, params);
		return client;
	}

	public static String getResponseString(HttpResponse httpResponse) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			String readLine;
			String response = "";
			while (((readLine = br.readLine()) != null)) {
				response += readLine;
			}
			return response;
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
