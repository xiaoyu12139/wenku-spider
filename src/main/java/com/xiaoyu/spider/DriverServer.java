package com.xiaoyu.spider;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriverServer extends WebSocketServer {
	
	private String res = null;

	public DriverServer(int port) {
		super(new InetSocketAddress(port));
	}

	/**
	 * 返回浏览器的版本
	 * 
	 * @return
	 */
	public String server() {
		log.info("创建服务端socket,开启并监听8888端口");
		start();
		int index = 0;
		while(true) {
			if(res != null) {
				log.info("抓取到浏览器返回的socket信息");
				return res;
			}
			try {
				log.info("没有抓取到浏览器返回的socket信息，" + (++index) + "秒");
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		DriverServer s = new DriverServer(8888);
		String res = s.server();
		s.stop();
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		log.info("连接建立");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		log.info("连接关闭");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		log.info("接受到消息：" + message);
		res = message;
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {

	}

	@Override
	public void onStart() {
		log.info("服务端开启成功");
	}

}
