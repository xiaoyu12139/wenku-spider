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
		while(time < 120) {
			if (flag != null && flag) {
				log.info("chrome浏览器启动完成");
				EventQueue.invokeLater(() -> {
					mainFrame = new MainFrame();
				});
				break;
			}
			log.info("监听程序界面调用，" + ++time);
			Thread.currentThread().sleep(500);
		}
		log.info("程序界面启动失败，请检查原因");
	}

	public static void main(String[] args) throws InterruptedException {
		Application app = new Application();
		app.run();
	}
}
