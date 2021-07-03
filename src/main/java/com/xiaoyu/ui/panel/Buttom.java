package com.xiaoyu.ui.panel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.xiaoyu.utils.MyPrintStream;

import lombok.Data;

public class Buttom extends JPanel {

	private static Buttom buttom = new Buttom();
	public JTextArea console = new JTextArea("打印输出信息……");
	public MyPrintStream mps;

	private Buttom() {
		setLayout(new BorderLayout());
		mps = new MyPrintStream(System.out, console);
		System.setOut(mps);
		System.setErr(mps);
		JScrollPane sp = new JScrollPane(console);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		add(sp);
	}
	
	public void setNull() {
		this.console.setText("");
	}
	
	public static Buttom getInstance() {
		return buttom;
	}
}
