package com.xiaoyu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.openqa.selenium.Cookie;

import com.alibaba.fastjson.JSON;

public class CookieUtil {
	//获取本地cookie
	//将登录后的cookie存储到本地，覆盖原来的cookie
	
	public static Cookie getCookie(){
		try {
			File file = new File(System.getProperty("user.dir") + "\\conf\\json.txt");
			if(!file.exists()) 
				file.createNewFile();
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String temp = null;
			String res = "";
			while((temp = r.readLine()) != null) {
				res += temp;
			}
			r.close();
			return JSON.parseObject(res, Cookie.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setCookie(Cookie cookie) {
		try {
			String target = JSON.toJSONString(cookie);
			File file = new File(System.getProperty("user.dir") + "\\conf\\json.txt");
			if(!file.exists()) 
				file.createNewFile();
			Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			w.write(target);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
