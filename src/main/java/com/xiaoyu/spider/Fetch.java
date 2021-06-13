package com.xiaoyu.spider;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.WebDriver;

import com.xiaoyu.ui.LaunchFrame;

public interface Fetch {
	//���������
	WebDriver driver = LaunchFrame.driver;
	CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore()).build();
	/**
	 * ��ʼ��url��ҳ��
	 * 
	 * 1.����ҳ��
	 * 2.��ȡ����cookie,����cookie
	 * 3.cookie�������¿��������������ȡ���û���������룬���е�¼
	 * 4.��¼�ɹ��󣬽��õ�¼�ɹ���ҳ���cookie��¼�����أ��رյ�ǰ��ʱ�����
	 * 5.���¶�ȡ����cookie����ˢ�µ�ǰҳ��
	 */
	void initPage();
	
}
