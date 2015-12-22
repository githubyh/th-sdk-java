package cn.tianhong.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class TestApi {
     protected static String  URL =   "http://test.openapi.rainbowcn.net" ;
     protected static String  GETMERCHANT_URL =  URL + "/open-rs/product/findMerchantProduct" ;

     protected static String appkey = "acbb3a7ee4858f32";
     protected static String secret = "c5ffde9121051f9a73b8ad2e85bca118124cc649";
     
     //构造参数
     public static String testGetMerchantParam(){ 
    	 Map<String, String> apiparamsMap = new TreeMap<String,String>();
         apiparamsMap.put("format", "json");
         apiparamsMap.put("app_key",appkey);
         apiparamsMap.put("v", "1.0");
         String timestamp =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
         apiparamsMap.put("timestamp",timestamp);
         apiparamsMap.put("page_number", "1");
         apiparamsMap.put("page_size", "3");
         apiparamsMap.put("start_date", "2014-07-02 00:00:00");
         apiparamsMap.put("end_date", "2014-07-05 00:00:00");
         
         return buildQuery(apiparamsMap);
     }
     
     
     
   //构造拼接
	public static String buildQuery(Map<String, String> apiparamsMap) {
		String sign = OpenApiClient.md5Signature(apiparamsMap,secret);
         apiparamsMap.put("sign", sign);
         StringBuilder param = new StringBuilder();
         for (Iterator<Map.Entry<String, String>> it = apiparamsMap.entrySet()
         .iterator(); it.hasNext();) {
             Map.Entry<String, String> e = it.next();
             param.append("&").append(e.getKey()).append("=").append(OpenApiClient.getEncodeString(e.getValue()));
         }
         return param.toString().substring(1);
	}
      
      
     
     public static void main(String[] args) {
         
		try {
			String result = HttpUtil.doPost(GETMERCHANT_URL,testGetMerchantParam());
			System.out.print(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
     }
     
      
}