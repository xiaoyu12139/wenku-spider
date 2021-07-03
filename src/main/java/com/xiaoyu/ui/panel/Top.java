package com.xiaoyu.ui.panel;

import java.awt.CardLayout;
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
	private JButton start = new JButton("download");
	private JButton stop = new JButton("  stop  ");
	public JPanel down = new JPanel();
	public CardLayout cl = new CardLayout();
	
	public Top() {
		setLayout(new FlowLayout());
		
		URLListener l = new URLListener(this);
		start.addActionListener(l);
		stop.addActionListener(l);
		url.addKeyListener(l);
		
		Box box = Box.createHorizontalBox();
		box.setBorder(BorderFactory.createEtchedBorder());
		box.add(Box.createHorizontalStrut(5));
		box.add(label);
		box.add(url);
		box.add(Box.createHorizontalStrut(5));
		down.setLayout(cl);
		down.add(start);
		down.add(stop);
		box.add(down);
		box.add(Box.createHorizontalStrut(5));
		add(box);
	}
	
}
