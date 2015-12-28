package cn.tianhong.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class HttpUtil {
	
	
	
	private static final String DEFAULT_CHARSET = Constants.CHARSET_UTF8;
	private static final String METHOD_POST = "POST";
	private static final String METHOD_GET = "GET";
	
	private static final int CONNECT_TIMEOUT = 3000 ;
	private static final int READ_TIMEOUT = 10000 ;
	

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
 

	 
	public static String doPost(String urlStr, String content)  throws IOException{
		URL url = null;
		HttpURLConnection connection = null;
		DataOutputStream out = null ;
		try {
			url = new URL(urlStr);

			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod(METHOD_POST);
//			 connection.setRequestProperty("Content-type",
//			 "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept-Charset", DEFAULT_CHARSET);
			connection.setRequestProperty("User-Agent", "top-sdk-java");
			connection.setUseCaches(false);
			connection.connect();

			out = new DataOutputStream(
					connection.getOutputStream());
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
			if(out != null){
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