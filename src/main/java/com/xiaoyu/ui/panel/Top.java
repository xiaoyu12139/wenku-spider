package com.xiaoyu.ui.panel;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xiaoyu.ui.listener.URLListener;

public class Top extends JPanel{
	
	public JTextField url = new JTextField("输入百度文库文章的url……", 55);
	private JLabel label = new JLabel("URL：");
	private JButton enter = new JButton("download");
	
	public Top() {
		setLayout(new FlowLayout());
		
		URLListener l = new URLListener(this);
		enter.addActionListener(l);
		url.addKeyListener(l);
		
		Box box = Box.createHorizontalBox();
		box.setBorder(BorderFactory.createEtchedBorder());
		box.add(Box.createHorizontalStrut(5));
		box.add(label);
		box.add(url);
		box.add(Box.createHorizontalStrut(5));
		box.add(enter);
		box.add(Box.createHorizontalStrut(5));
		add(box);
	}
	
}
