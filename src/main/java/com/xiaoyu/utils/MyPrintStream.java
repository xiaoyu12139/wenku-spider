package com.xiaoyu.utils;

import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class MyPrintStream extends PrintStream {

	private JTextArea text;
	private StringBuffer sb = new StringBuffer();

	public MyPrintStream(OutputStream out, JTextArea text) {
		super(out);
		this.text = text;
	}

	public void write(byte[] buf, int off, int len) {
		final String message = new String(buf, off, len);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sb.append(message);
				text.setText(sb.toString());
				text.paintImmediately(text.getBounds());
			}
		});
		super.write(buf, off, len);
	}

}
