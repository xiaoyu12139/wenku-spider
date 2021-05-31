package com.xiaoyu.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test07 {
	static JWindow f;
	public static Logger log = LoggerFactory.getLogger(Test07.class);
	public static void main(String[] args) {
		f = new JWindow();
		JButton b = new JButton("yes");
		b.setPreferredSize(new Dimension(20, 20));
		b.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Test07.f.setVisible(false);
				try {
					Thread.currentThread().sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				Test07.f.setVisible(true);
			}
		});
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(b);
		f.add(f.add(b));
//		f.setSize(200, 200);
		f.setPreferredSize(new Dimension(200, 200));
		f.pack();
		f.setVisible(true);
	}
}
