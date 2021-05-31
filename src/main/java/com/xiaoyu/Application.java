package com.xiaoyu;

import java.awt.EventQueue;

import com.xiaoyu.ui.LaunchFrame;
import com.xiaoyu.ui.MainFrame;
import com.xiaoyu.ui.ReadyWindow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
	
	  static { System.setProperty("logback.configurationFile", System.getProperty("user.dir") + "\\conf\\logback.xml");}

	private LaunchFrame launchFrame;
	private MainFrame mainFrame;
	private Boolean flag = null;
	private ReadyWindow readyWin;

	public void run() throws InterruptedException {
		launchFrame = new LaunchFrame();
		synchronized (LaunchFrame.class) {
			log.info("wait LaunchFrame");
			LaunchFrame.class.wait();
		}
		log.info("already runChrome");
		new Thread(() -> {
			readyWin = new ReadyWindow(launchFrame);
			flag = readyWin.run();
		}).start();
		int time = 0;
		log.info("�������������ã�timeout Ϊ 10����");
		while(time < 1200) {
			if (flag != null && flag) {
				log.info("chrome������������");
				EventQueue.invokeLater(() -> {
					mainFrame = new MainFrame();
				});
				break;
			}
			if(flag != null && !flag) {
				log.info("�����������ʧ�ܣ�����ԭ��");
				System.exit(0);
			}
			Thread.currentThread().sleep(500);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		Application app = new Application();
		app.run();
	}
}
