package com.xiaoyu.spider;

public interface Fetch {
	/**
	 * ��ʼ��url��ҳ��
	 * 
	 * 1.����ҳ��
	 * 2.��ȡ����cookie,����cookie
	 * 3.cookie�������¿��������������ȡ���û���������룬���е�¼
	 * 4.��¼�ɹ��󣬽��õ�¼�ɹ���ҳ���cookie��¼�����أ��رյ�ǰ��ʱ�����
	 * 5.���¶�ȡ����cookie����ˢ�µ�ǰҳ��
	 */
	void initPage();
	
}
