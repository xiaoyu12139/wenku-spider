package com.xiaoyu.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Test06 {
	public static void main(String[] args) {
		try {
			String cmd = "tasklist | findstr chromedriver.exe";
			Runtime runtime = Runtime.getRuntime();
			Process exec = runtime.exec("cmd /c " + cmd);
			InputStream in = exec.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			String res = "";
			String temp = null;
			while((temp = r.readLine()) != null) {
				String pid = temp.split("\\s+")[1];
				String kill = "taskkill /pid " + pid + " /f";
				runtime.exec("cmd /c " + kill);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
