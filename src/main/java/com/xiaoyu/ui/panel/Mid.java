package com.xiaoyu.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Mid extends JPanel{
	
	public static Mid mid = new Mid();
	
	public JLabel title = new JLabel("����:    java���ģʽ - �ٶ��Ŀ�");//docInfo2019 > title
	public JLabel downProgress = new JLabel("���ؽ���:    1/20");
	public JLabel type = new JLabel("�ĵ�����:    word");//docInfo2019 > typeName
	public JLabel createTime = new JLabel("����ʱ��:    2020/12/09");//docInfo2019 > create_time_show
	public JLabel tags = new JLabel("��ǩ�����ģʽ��java��Ƶȵ�");
	
	public Mid() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {0, 0};
		layout.columnWeights = new double[] {0.5, 1.0};
		layout.rowHeights = new int[] {0, 0, 0};
		layout.rowWeights = new double[] {0.34, 0.34, 1};
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		layout.setConstraints(title, c);
		add(title);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 0;
		layout.setConstraints(downProgress, c);
		add(downProgress);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		layout.setConstraints(type, c);
		add(type);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 1;
		layout.setConstraints(createTime, c);
		add(createTime);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		layout.setConstraints(tags, c);
		add(tags);
		
		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
	}
	
	public static Mid getInstance() {
		return mid;
	}
}
