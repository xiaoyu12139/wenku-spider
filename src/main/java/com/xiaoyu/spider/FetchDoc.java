package com.xiaoyu.spider;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openqa.selenium.WebDriver;

import com.alibaba.fastjson.JSONObject;
import com.xiaoyu.model.PageModel;
import com.xiaoyu.ui.panel.Mid;

public interface FetchDoc {
	//key - ҳ�� 	value - ��ǰҳ������ģ��
	Map<Integer, PageModel> docModel = new HashMap<>();
	
	CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore()).build();
	
	//�������ݵ�����word�ļ�
	XWPFDocument document = new XWPFDocument();
	
	Mid mid = Mid.getInstance();
	
	/**
	 * ��ȡ��url�µ��ĵ�ÿһҳ��PageModel�浽��Ӧҳ����docModel����
	 */
	void initModel(JSONObject json);
	
	/**
	 * ���鵱ǰҳ�������ص�ǰҳ
	 */
	void parseAndDownPage(XWPFDocument document, PageModel pageModel, int pageIndex);
	
	/**
	 * ������ɺ�ʼ�����ĵ�
	 */
	void download();
	
	void run(WebDriver driver, JSONObject json);

}
