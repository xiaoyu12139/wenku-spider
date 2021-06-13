package com.xiaoyu.test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xiaoyu.utils.StrUtil;

public class Test05 {
	public static void main(String[] args) throws IOException {
		String tmp = "_0_";
		Pattern p = Pattern.compile("_([0-9]*)_");
		Matcher m = p.matcher(tmp);
		if (m.find())
			tmp = m.group(1);
		System.out.println(tmp);
	}
}
