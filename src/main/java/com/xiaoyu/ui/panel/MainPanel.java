package com.xiaoyu.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

public class MainPanel extends JPanel{
	
	private Top top = new Top();
	private Mid mid = Mid.getInstance();
	private Buttom buttom = Buttom.getInstance();

	public MainPanel() {
//		top.setBackground(Color.black);
//		mid.setBackground(Color.blue);
//		buttom.setBackground(Color.green);
		
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {0};
		layout.columnWeights = new double[] {1.0};
		layout.rowHeights = new int[] {0, 0, 0};
		layout.rowWeights = new double[] {0, 0.25, 1.0};
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		layout.setConstraints(top, c);
		add(top);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		layout.setConstraints(mid, c);
		add(mid);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 2;
		layout.setConstraints(buttom, c);
		add(buttom);
	}
	
}
