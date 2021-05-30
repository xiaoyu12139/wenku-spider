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
	 * ����������İ汾
	 * 
	 * @return
	 */
	public String server() {
		log.info("���������socket,����������8888�˿�");
		start();
		int index = 0;
		while(true) {
			if(res != null) {
				log.info("ץȡ����������ص�socket��Ϣ");
				return res;
			}
			try {
				log.info("û��ץȡ����������ص�socket��Ϣ��" + (++index) + "��");
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
		log.info("���ӽ���");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		log.info("���ӹر�");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		log.info("���ܵ���Ϣ��" + message);
		res = message;
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {

	}

	@Override
	public void onStart() {
		log.info("����˿����ɹ�");
	}

}
