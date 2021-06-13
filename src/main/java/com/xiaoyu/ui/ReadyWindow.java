package com.xiaoyu.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;

import com.xiaoyu.utils.MyPrintStream;
import com.xiaoyu.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadyWindow extends JWindow{
	
	private LaunchFrame launch;
	private JTextArea out = new JTextArea("启动信息……");
	
	public ReadyWindow(LaunchFrame launch) {
		this.launch = launch;
		Icon icon = new ImageIcon(StrUtil.RUNIMG);
		JLabel img = new JLabel();
		img.setIcon(icon);
		JPanel panel = new JPanel();
		MyPrintStream print = new MyPrintStream(System.out, out);
		System.setOut(print);
		System.setErr(print);
		log.info("启动界面");
		JScrollPane sp = new JScrollPane(out);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { 0 };
		layout.columnWeights = new double[] { 1.0 };
		layout.rowHeights = new int[] { 0, 0 };
		layout.rowWeights = new double[] { 0.6, 1};
		panel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		layout.setConstraints(img, c);
		panel.add(img);

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		layout.setConstraints(sp, c);
		panel.add(sp);
		
		panel.setSize(400, 300);

//		setAlwaysOnTop(true);
		setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.CENTER);
//		setSize(400, 300);
		setPreferredSize(new Dimension(400, 300));
		pack();
		setLocationRelativeTo(null);
//		setVisible(true);
	}
	
	public boolean run() {
		boolean flag = launch.runChrome();
		if(!flag) {
			try {
				Thread.currentThread().sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		dispose();
		return flag;
	}
}