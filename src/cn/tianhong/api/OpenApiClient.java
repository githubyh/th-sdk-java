package cn.tianhong.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpenApiClient  {


	private static final String DEFAULT_CHARSET = Constants.CHARSET_UTF8;

	private String appKey = null;
	private String sign = null;
	private String parameterStr = null;
	
	
	
	
	
	
	/**
	 *  参数的封装 
	 */
	public static StringBuffer getParemeterString(Map<String, String> map, String secret) {
		StringBuffer orgin = new StringBuffer(secret);
		if (null == map)
			return null;
		List<String> arrays = new ArrayList<String>();
		
		for (Iterator<Map.Entry<String, String>> it = map.entrySet()
		         .iterator(); it.hasNext();) {
			 Map.Entry<String, String> entry = it.next();
			String key = entry.getKey().toString();
				if (key.endsWith("sign")) {
					continue;
				}
				 orgin.append(key).append(map.get(key));
		}
		return orgin;
	}

	 
	/**
	 * 签名运算
	 * 
	 * @param parameter
	 * @param secret
	 * @return
	 * @throws Exception
	 * 
	 */
	public static String sign(String parameter, String secret) throws Exception {

		// 对参数+密钥做MD5运算
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e);
		}
		byte[] digest = md.digest((parameter).getBytes(DEFAULT_CHARSET));
		String sign =  byte2hex(digest);
		return sign ;
	}

	/**
	 * 二行制转字符串
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
	 * 新的md5签名，首尾放secret。
	 * 
	 * @param secret
	 *            分配给您的APP_SECRET
	 */
	public static String md5Signature(Map<String, String> params, String secret) {
		String result = null;
		StringBuffer orgin = getParemeterString(params, secret) ;
		if (orgin == null)
			return result;
		orgin.append(secret);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			result = byte2hex(md.digest(orgin.toString().getBytes(DEFAULT_CHARSET)));
		} catch (Exception e) {
			throw new java.lang.RuntimeException("sign error !");
		}
		return result;
	}

 

	/**
	 * 验证签名
	 * 
	 * @param sign
	 * @param parameter
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	public boolean validateSign(String secret) throws Exception {
		return sign != null && parameterStr != null && secret != null
				&& sign.equals(sign(secret + parameterStr + secret, secret));
	}
 
	/**
	 * 转码
	 * 
	 * @param str
	 * @return
	 */
	public static String getEncodeString(String str) {
		try {
			if (null != str) {
				return URLEncoder.encode(str, DEFAULT_CHARSET);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getAppKey() {
		return appKey;
	}

	public String getSign() {
		return sign;
	}

	public String getParameterStr() {
		return parameterStr;
	}

}
