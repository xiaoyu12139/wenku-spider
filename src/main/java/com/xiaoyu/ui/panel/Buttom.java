package com.xiaoyu.ui.panel;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.xiaoyu.utils.MyPrintStream;

public class Buttom extends JPanel {

	private JTextArea console = new JTextArea("打印输出信息……");

	public Buttom() {
		setLayout(new BorderLayout());
		MyPrintStream mps = new MyPrintStream(System.out, console);
		System.setOut(mps);
		System.setErr(mps);
		JScrollPane sp = new JScrollPane(console);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		add(sp);
	}
}
