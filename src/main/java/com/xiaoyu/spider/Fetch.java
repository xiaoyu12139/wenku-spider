package com.xiaoyu.spider;

public interface Fetch {
	/**
	 * 初始化url的页面
	 * 
	 * 1.请求页面
	 * 2.读取本地cookie,检验cookie
	 * 3.cookie过器重新开启浏览器，并获取到用户输入的密码，进行登录
	 * 4.登录成功后，将该登录成功的页面的cookie记录到本地，关闭当前临时浏览器
	 * 5.重新读取本地cookie，并刷新当前页面
	 */
	void initPage();
	
}
