package com.xiaoyu.ui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.HttpURLConnection;
import java.net.URL;

import com.xiaoyu.model.DownloadModel;
import com.xiaoyu.spider.Fetch;
import com.xiaoyu.spider.impl.FetchImpl;
import com.xiaoyu.ui.panel.Top;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class URLListener implements KeyListener, ActionListener {

	private Top top;
	private DownloadModel downloadModel = DownloadModel.getInstance();
	private static boolean isrun = false;

	public URLListener(Top top) {
		this.top = top;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!isrun) {
			isrun = true;
			execute();
			isrun = false;
		}
		log.info("��ǰ��ץȡ������δ��ɣ����ĵȴ���ɺ��ڽ���ץȡ");
			// ����������ʱ������ҪУ��cookie�Ƿ�Ϸ���������ʱ��Ҫ����������������ץȡ����
			// ֻ��Ҫ�½���ǩҳ
			// ÿ��ץȡǰ��У��cookie�Ƿ�Ϸ������Ϸ����ڵ������ڣ���ǰcookie��ȥ�����ڵ����������ҳ���¼
			// ��¼��ɺ�ص�gui�е��ȷ��
			// ���ȷ�Ͼʹ�����ҳ���¼���ˣ�����У���Ƿ�ɹ���¼����Ļ����ٴε����ղŵĴ���
			// ���ȡ���͹ر������
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	public void execute() {
		boolean flag = receiveURL();
		if (!flag)
			return ;
		new Thread(() -> {
			try {
				Fetch fetch = new FetchImpl();
				fetch.initPage();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}).start();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 10) {
			if(!isrun) {
				isrun = true;
				execute();
				isrun = false;
			}
			log.info("��ǰ��ץȡ������δ��ɣ����ĵȴ���ɺ��ڽ���ץȡ");
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	// ���������url��У��url�Ϸ���
	public boolean receiveURL() {
		boolean flag = false;
		try {
			log.info("����У��url�Ϸ��ԡ���");
			String url = top.url.getText();
			for (int i = 0; i < 3; i++) {
				HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
				int state = con.getResponseCode();
				if (state == 200) {
					log.info("url(" + top.url.getText() + ") is available!");
					flag = true;
					break;
				}
			}
			if (flag) {
				removeAll(downloadModel);
				downloadModel.setUrl(url);
			}
			return flag;
		} catch (Exception e) {
			log.info("url(" + top.url.getText() + ")" + " is error. Please reenter.");
			return flag;
		}
	}

	public void removeAll(DownloadModel downloadModel) {
		downloadModel.setCreateTime(null);
		downloadModel.setDownProgress(null);
		downloadModel.setTags(null);
		downloadModel.setTitle(null);
		downloadModel.setType(null);
		downloadModel.setUrl(null);
	}

}
