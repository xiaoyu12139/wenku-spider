package com.xiaoyu.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.xiaoyu.ui.panel.MainPanel;
import com.xiaoyu.utils.StrUtil;

public class MainFrame extends JFrame{
	
	private MainPanel mainPanel = new MainPanel();
	private JMenuBar menuBar = new JMenuBar();
	
	public MainFrame() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle(StrUtil.TITLE);
		setIconImage(new ImageIcon(StrUtil.LOGOPATH).getImage());
		SwingUtilities.updateComponentTreeUI(mainPanel);
		setLayout(new BorderLayout());
		JMenu file = new JMenu("file");
		JMenu help = new JMenu("help");
		JMenuItem item1 = new JMenuItem("item1");
		JMenuItem item2 = new JMenuItem("item2");
		file.add(item1);
		help.add(item2);
		menuBar.add(file);
		menuBar.add(help);
		setJMenuBar(menuBar);
		getContentPane().add(mainPanel);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				WebDriver driver = LaunchFrame.driver;
				ChromeDriverService service = LaunchFrame.service;
				if(driver != null) 
					driver.quit();
				if(service != null)
					service.stop();
				try {
					String cmd = "tasklist | findstr chromedriver.exe";
					Runtime runtime = Runtime.getRuntime();
					Process exec = runtime.exec("cmd /c " + cmd);
					InputStream in = exec.getInputStream();
					BufferedReader r = new BufferedReader(new InputStreamReader(in));
					String temp = null;
					while((temp = r.readLine()) != null) {
						String pid = temp.split("\\s+")[1];
						String kill = "taskkill /pid " + pid + " /f";
						runtime.exec("cmd /c " + kill);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		setSize(600, 400);
		setResizable(false);
//		setAlwaysOnTop(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new MainFrame();
	}
}
