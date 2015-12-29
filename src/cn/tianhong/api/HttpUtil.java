package cn.tianhong.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.xml.internal.ws.util.StringUtils;

public class HttpUtil {

	private static final String DEFAULT_CHARSET = Constants.CHARSET_UTF8;
	private static final String METHOD_POST = "POST";
	private static final String METHOD_GET = "GET";

	private static final int CONNECT_TIMEOUT = 3000;
	private static final int READ_TIMEOUT = 10000;

	private static final Set<String> aliDomains = new HashSet();
	private static final boolean ignoreSSLCheck = true;

	static {
		aliDomains.add("*.tianhong.cn");
	}

	/**
	 * 新的md5签名，首尾放secret�?
	 * 
	 * @param secret
	 *            分配给您的APP_SECRET
	 */
	public static String md5Signature(TreeMap<String, String> params,
			String secret) {
		String result = null;
		StringBuffer orgin = getBeforeSign(params, new StringBuffer(secret));
		if (orgin == null)
			return result;
		orgin.append(secret);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			result = byte2hex(md.digest(orgin.toString().getBytes("utf-8")));
		} catch (Exception e) {
			throw new java.lang.RuntimeException("sign error !");
		}
		return result;
	}

	/**
	 * 二行制转字符
	 */
	private static String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer();
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs.append("0").append(stmp);
			else
				hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}

	/**
	 * 添加参数的封装方�?
	 */
	private static StringBuffer getBeforeSign(TreeMap<String, String> params,
			StringBuffer orgin) {
		if (params == null)
			return null;
		Map<String, String> treeMap = new TreeMap<String, String>();
		treeMap.putAll(params);
		Iterator<String> iter = treeMap.keySet().iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			orgin.append(name).append(params.get(name));
		}
		return orgin;
	}

	public static class TrustAllTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}
	}

	public static class VerisignTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			X509Certificate aliCert = null;
			for (X509Certificate cert : chain) {
				cert.checkValidity();
				try {
					String dn = cert.getSubjectX500Principal().getName();
					LdapName ldapDN = new LdapName(dn);
					for (Rdn rdn : ldapDN.getRdns())
						if (("CN".equals(rdn.getType()))
								&& (aliDomains.contains(rdn.getValue()))) {
							aliCert = cert;
							break;
						}
				} catch (Exception e) {
					throw new CertificateException(e);
				}
			}
		}
	}

	private static HttpURLConnection getConnection(URL url, String method,
			String ctype, Map<String, String> headerMap, boolean useHttpDns)
			throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if ((conn instanceof HttpsURLConnection)) {
			HttpsURLConnection connHttps = (HttpsURLConnection) conn;
			if (ignoreSSLCheck && useHttpDns)
				try {
					SSLContext ctx = SSLContext.getInstance("TLS");
					ctx.init(null,
							new TrustManager[] { new TrustAllTrustManager() },
							new SecureRandom());
					connHttps.setSSLSocketFactory(ctx.getSocketFactory());
					connHttps.setHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
				} catch (Exception e) {
					throw new IOException(e);
				}
			else {
				try {
					SSLContext ctx = SSLContext.getInstance("TLS");
					ctx.init(null,
							new TrustManager[] { new VerisignTrustManager() },
							new SecureRandom());
					connHttps.setSSLSocketFactory(ctx.getSocketFactory());
					connHttps.setHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
			conn = connHttps;
		}

		conn.setRequestMethod(method);
		conn.setDoInput(true);
		conn.setDoOutput(true);
//		conn.setRequestProperty("Accept", "text/xml,text/javascript,application/x-www-form-urlencoded");
		if (useHttpDns)
			conn.setRequestProperty("User-Agent", "top-sdk-java-httpdns");
		else {
			conn.setRequestProperty("User-Agent", "top-sdk-java");
		}
		conn.setRequestProperty("Content-Type", ctype);
		if (headerMap != null) {
			for (Map.Entry entry : headerMap.entrySet()) {
				conn.setRequestProperty((String) entry.getKey(),
						(String) entry.getValue());
			}
		}
		return conn;
	}

	/*public static String doPost(String url, Map<String, String> params,
			String charset, int connectTimeout, int readTimeout)
			throws IOException {
		return doPost(url, params, charset, connectTimeout, readTimeout, null,
				false);
	}*/

	public static String doPost(String url, String ctype, byte[] content,
			boolean useHttpDns) throws IOException {
		return _doPost(url, ctype, content, null, useHttpDns);
	}

	private static String _doPost(String url, String ctype, byte[] content,
			Map<String, String> headerMap, boolean useHttpDns)
			throws IOException {
		HttpURLConnection conn = null;
		OutputStream out = null;
		String rsp = null;
		try {
			try {
				conn = getConnection(new URL(url), "POST", ctype, headerMap,
						useHttpDns);
				conn.setConnectTimeout(CONNECT_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);

			} catch (IOException e) {
				// Map map = getParamsFromUrl(url);
				// TaobaoLogger.logCommError(e, url, (String)map.get("app_key"),
				// (String)map.get("method"), content);
				e.printStackTrace();
			}
			try {
				out = conn.getOutputStream();
				out.write(content);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream(),
								DEFAULT_CHARSET));
				StringBuffer buffer = new StringBuffer();
				String line = "";
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}
				rsp = buffer.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			if (out != null) {
				out.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}

		return rsp;
	}

	public static String doPost(String urlStr, String content)
			throws IOException {
		URL url = null;
		HttpURLConnection connection = null;
		DataOutputStream out = null;
		try {
			url = new URL(urlStr);

			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod(METHOD_POST);
			// connection.setRequestProperty("Content-type",
			// "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept-Charset", DEFAULT_CHARSET);
			connection.setRequestProperty("User-Agent", "top-sdk-java");
			connection.setUseCaches(false);
			connection.connect();

			out = new DataOutputStream(connection.getOutputStream());
			out.write(content.getBytes(DEFAULT_CHARSET));

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), DEFAULT_CHARSET));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			return buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}
}