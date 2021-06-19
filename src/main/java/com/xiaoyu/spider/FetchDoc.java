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
	//key - 页数 	value - 当前页的数据模型
	Map<Integer, PageModel> docModel = new HashMap<>();
	
	CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore()).build();
	
	//操作数据到本地word文件
	XWPFDocument document = new XWPFDocument();
	
	Mid mid = Mid.getInstance();
	
	/**
	 * 获取该url下的文档每一页的PageModel存到对应页数的docModel里面
	 */
	void initModel(JSONObject json);
	
	/**
	 * 重组当前页，并下载当前页
	 */
	void parseAndDownPage(XWPFDocument document, PageModel pageModel, int pageIndex);
	
	/**
	 * 解析完成后开始下载文档
	 */
	void download();
	
	void run(WebDriver driver, JSONObject json);

}
