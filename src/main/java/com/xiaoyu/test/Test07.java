package com.xiaoyu.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.WindowConstants;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test07 {
	static JWindow f;
	public static Logger log = LoggerFactory.getLogger(Test07.class);
	public static void main(String[] args) {
		String str = "标题:  SQL数据库教程";
		System.out.println(str.substring(5));
	}
}
