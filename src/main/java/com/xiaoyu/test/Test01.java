package com.xiaoyu.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.openqa.selenium.Cookie;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Test01 {
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Set<Cookie> cookie = new HashSet<Cookie>();
      cookie.add(new Cookie("BDUSS_BFESS", "kNxWFYtNjNjVlZESUw5cnpTTTRYQ1NNbnVkQjFoTDVzOWJ-OFUtaTdleVBsZEJnSUFBQUFBJCQAAAAAAAAAAAEAAACpLuBIstDDzjk3OTc5NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI8IqWCPCKlgb"));
      cookie.add(new Cookie("_click_param_pc_rec_doc_2017_testid", "4"));
      String json = JSON.toJSONString(cookie);
      File file = new File(System.getProperty("user.dir") + "\\json.txt");
      if(!file.exists()) file.createNewFile();
      Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
      w.write(json);
      w.close();
      BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      String temp = "";
      String res = "";
      while((temp = r.readLine()) != null) {
    	  res += temp;
      }
      r.close();
      Set<JSONObject> p = JSON.parseObject(res, Set.class);
      Set<Cookie> cookies = new HashSet<Cookie>();
      for(JSONObject o : p) {
    	  Cookie c = o.toJavaObject(Cookie.class);
    	  cookies.add(c);
      }
      for(Cookie c : cookies) {
    	  System.out.println(c);
      }
//      for(Cookie c : p) {
//    	  System.out.println(c);
//      }
	}
}
