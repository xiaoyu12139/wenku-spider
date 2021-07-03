package com.xiaoyu.spider;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.WebDriver;

import com.alibaba.fastjson.JSONObject;
import com.xiaoyu.model.DocInfoType;
import com.xiaoyu.ui.panel.Mid;

public interface FetchTxt {
	CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore()).build();
	Mid mid = Mid.getInstance();
	void run(WebDriver driver, String html, DocInfoType docInfoType);
	void download(String html) throws Exception;
}
