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
		log.info("当前有抓取任务尚未完成，耐心等待完成后在进行抓取");
			// 在启动程序时，就需要校验cookie是否合法，并且这时需要启动浏览器，后面的抓取操作
			// 只需要新建标签页
			// 每次抓取前，校验cookie是否合法，不合法就在弹出窗口，当前cookie过去，请在弹出的浏览器页面登录
			// 登录完成后回到gui中点击确认
			// 点击确认就代表在页面登录过了，进行校验是否成功登录。否的话，再次弹出刚才的窗口
			// 点击取消就关闭浏览器
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
