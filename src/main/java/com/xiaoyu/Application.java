package com.xiaoyu;

import java.awt.EventQueue;

import com.xiaoyu.ui.LaunchFrame;
import com.xiaoyu.ui.MainFrame;
import com.xiaoyu.ui.ReadyWindow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

	static {
		System.setProperty("logback.configurationFile", System.getProperty("user.dir") + "\\conf\\logback.xml");
	}

	private LaunchFrame launchFrame;
	private MainFrame mainFrame;
	private Boolean flag = null;
	public ReadyWindow readyWin;

	public void run() throws InterruptedException {
		launchFrame = new LaunchFrame();
		synchronized (LaunchFrame.class) {
			log.info("wait LaunchFrame");
			LaunchFrame.class.wait();
		}
		log.info("already runChrome");

		EventQueue.invokeLater(() -> {
			synchronized (Application.class) {
				Application.this.readyWin = new ReadyWindow(launchFrame);
				Application.this.readyWin.setVisible(true);
				Application.class.notifyAll();
			}
		});
		synchronized (Application.class) {
			Application.class.wait();
			System.out.println("wait over");
		}
		flag = readyWin.run();
		log.info("�������������ã�timeout Ϊ 10����");
		if (flag != null && flag) {
			log.info("chrome������������");
			EventQueue.invokeLater(() -> {
				mainFrame = new MainFrame();
			});
		}
		if (flag != null && !flag) {
			log.info("�����������ʧ�ܣ�����ԭ��");
			System.exit(0);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		Application app = new Application();
		app.run();
	}
}
