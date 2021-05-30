function getBrowserInfo() {
	var Sys = {};
	var ua = navigator.userAgent.toLowerCase();
	var re = /(msie|firefox|chrome|opera|version).*?([\d.]+)/;
	var m = ua.match(re);
	Sys.browser = m[1].replace(/version/, "'safari");
	Sys.ver = m[2];
	return Sys;
}

function webSocket() {
	if ("WebSocket" in window) {
		console.log("您的浏览器支持WebSocket");
		var ws = new WebSocket("ws://localhost:9999"); //创建WebSocket连接
		ws.onopen = function() {
			//当WebSocket创建成功时，触发onopen事件
			var sys = getBrowserInfo();
			//将消息发送到服务端
			ws.send(sys.browser + ":" + sys.ver);
			console.log(sys.browser + ":" + sys.ver);
			window.close();
		}
		ws.close;
		//...
	} else {
		console.log("您的浏览器不支持WebSocket");
	}
}
webSocket();
