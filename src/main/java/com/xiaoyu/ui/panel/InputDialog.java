package com.xiaoyu.ui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class InputDialog extends JDialog implements ActionListener, FocusListener{
	
	private JTextField username = new JTextField("username", 20);
	private JPasswordField pw = new JPasswordField("password", 20);
	private Box box = Box.createHorizontalBox();
	private JButton yes = new JButton("确定");
	private JButton no = new JButton("取消");
	private Map<String, String> map = new HashMap<>();
	
	public InputDialog() {
		
		super((Frame)null, "login", true);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));
		panel.add(username);
		panel.add(pw);
		
		box.add(yes);
		box.add(no);
		panel.add(box);
		panel.setFocusable(true);
		panel.requestFocus();
		
		username.addFocusListener(this);
		pw.addFocusListener(this);
		yes.addActionListener(this);
		no.addActionListener(this);
		
		setResizable(false);
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setSize(250, 150);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new InputDialog();
	}

	@Override
	public void focusGained(FocusEvent e) {
		JTextField t = (JTextField)e.getSource();
		t.setText("");
	}

	@Override
	public void focusLost(FocusEvent e) {
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton)e.getSource();
		if(b.getText().equals("确定")) {
			map.clear();
			map.put("username", username.getText());
			map.put("pw", new String(pw.getPassword()));
			synchronized (InputDialog.class) {
				InputDialog.class.notifyAll();
			}
			this.dispose();
		}else {
			this.dispose();
		}
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
}
