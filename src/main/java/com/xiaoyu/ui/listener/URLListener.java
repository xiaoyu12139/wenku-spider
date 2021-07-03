package com.xiaoyu.ui.listener;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JButton;

import com.xiaoyu.model.DownloadModel;
import com.xiaoyu.spider.Fetch;
import com.xiaoyu.spider.impl.FetchImpl;
import com.xiaoyu.ui.panel.Buttom;
import com.xiaoyu.ui.panel.Top;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class URLListener implements KeyListener, ActionListener {

	private Top top;
	private DownloadModel downloadModel = DownloadModel.getInstance();
	private static boolean isrun = false;
	private Thread thread = null;
	Buttom buttom = Buttom.getInstance();
	
	public URLListener(Top top) {
		this.top = top;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton o = (JButton)e.getSource();
		if(o.getText().equals("download")) {
			log.info("start");
			isrun = true;
			CardLayout cl = top.cl;
			cl.next(top.down);
			thread = execute();
			if(thread != null) {
				log.info("开启一个新的线程进行抓取任务");
			}else {
				cl.next(top.down);
				log.info("停止抓取任务");
			}
		}else {
			isrun = false;
			CardLayout cl = top.cl;
			cl.next(top.down);
			if(thread != null) {
				thread.stop();
				log.info("停止抓取任务线程");
				thread = null;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	public Thread execute() {
		boolean flag = receiveURL();
		if (!flag)
			return null;
		Thread thread = new Thread(() -> {
			try {
				Fetch fetch = new FetchImpl();
				fetch.initPage();
				isrun = false;
				CardLayout cl = top.cl;
				cl.next(top.down);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		thread.start();
		return thread;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 10) {
			if(!isrun) {
				isrun = true;
				CardLayout cl = top.cl;
				cl.next(top.down);
				thread = execute();
			}
			log.info("当前有抓取任务尚未完成，耐心等待完成后在进行抓取");
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	// 接受输入的url并校验url合法性
	public boolean receiveURL() {
		boolean flag = false;
		try {
			log.info("正在校验url合法性……");
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
