package com.xiaoyu.spider.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoyu.model.DocInfoType;
import com.xiaoyu.model.DownloadModel;
import com.xiaoyu.spider.Fetch;
import com.xiaoyu.ui.LaunchFrame;
import com.xiaoyu.ui.panel.InputDialog;
import com.xiaoyu.ui.panel.Mid;
import com.xiaoyu.utils.CookieUtil;
import com.xiaoyu.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FetchImpl implements Fetch {

	private DownloadModel downloadModel = DownloadModel.getInstance();
	private Map<String, String> map = null;
	private DocInfoType docInfoType = DocInfoType.docInfo;

	@Override
	public void initPage() {
		try {
			// 请求页面
			requestPage();
			// 读取本地cookie,检验cookie
			boolean overdue = readLocalCookie();
			// cookie过器重新开启浏览器，并获取到用户输入的密码，进行登录
			// 登录成功后，将该登录成功的页面的cookie记录到本地，关闭当前临时浏览器
			// 重新读取本地cookie，并刷新当前页面
			if (!overdue)
				if (!reloadCookie())
					return;
			checkTypeAndInvoke();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void requestPage() {
		log.info("浏览器窗口回调完成，正在请求页面：" + downloadModel.getUrl());
		driver.get(downloadModel.getUrl());
		log.info("页面请求完成。");
	}

	/**
	 * cookie失效返回false
	 * 
	 * @return
	 */
	private boolean readLocalCookie() {
		Cookie cookie = CookieUtil.getCookie();
		if (cookie != null) {
			log.info("读取本地存储的cookie.");
			WebDriver.Options manage = driver.manage();
			manage.addCookie(cookie);
			log.info("正在刷新页面。");
			driver.navigate().refresh();
			log.info("页面刷新完成。");
		}
		String pageSource = driver.getPageSource();
		String pageData = parseHtml(pageSource);
		log.info("抓取到pageData:" + pageData);
		log.info("开始校验 cookie.");
		return checkCookie(pageData);
	}

	private boolean reloadCookie() {
		String pageSource = driver.getPageSource();
		String pageData = parseHtml(pageSource);
		log.info("cookie 过期，请重新登录。");
		InputDialog in = new InputDialog();
		log.info("等待用户输入");
		map = in.getMap();
		log.info("密码：" + map.get("pw"));
		log.info("账号密码输入完成。");
		if (login(driver)) {
			log.info("登录成功，正在序列化cookie到本地文件。");
			WebDriver.Options manage = driver.manage();
			Cookie c = manage.getCookieNamed("BDUSS");
			CookieUtil.setCookie(c);
			log.info("登录成功，准备开始抓取。");
			pageData = parseHtml(driver.getPageSource());
			log.info("当前pageData:" + pageData);
		} else {
			log.info("登录失败，退出本次抓取。");
			return false;
		}
		return true;
	}

	private void checkTypeAndInvoke() throws FileNotFoundException {
		String pageSource = driver.getPageSource();
		String pageData = parseHtml(pageSource);
		JSONObject json = JSON.parseObject(pageData);
		checkDocInfoType(json);
		final JSONObject temp = json;
		new Thread(() -> {
			log.info("设置文档信息");
			setExplain(temp);
		}).start();
		log.info("实例化文档抓取器-FetchDoc");
		new FetchDocImpl().run(driver, json, docInfoType);
		log.info("本次抓取完成");
	}

	private void checkDocInfoType(JSONObject json) {
		if (json.getJSONObject("docInfo2019") != null)
			docInfoType = DocInfoType.docInfo2019;
	}

	/**
	 * 设置mid面板中显示的该文档的一些信息
	 */
	private void setExplain(JSONObject json) {
		Mid mid = Mid.getInstance();
		if (docInfoType == DocInfoType.docInfo) {
			JSONObject info = json.getJSONObject("viewBiz").getJSONObject("docInfo");
			mid.title.setText("标题:  " + info.getString("title"));
			mid.type.setText("文档类型:  " + info.getString("fileType"));
			String temp = info.getString("showCreateTime");
			mid.createTime.setText("创建时间:  " + sdfDate(temp));
			JSONArray tags = json.getJSONObject("viewBiz").getJSONArray("tags");
			setMidTags(mid, tags);
		}
		if(docInfoType == DocInfoType.docInfo2019) {
			JSONObject info = json.getJSONObject("docInfo2019").getJSONObject("doc_info");
			mid.title.setText("标题:  " + info.getString("title"));
			String temp = info.getString("create_time");
			mid.createTime.setText("创建时间:  " + sdfDate(temp));
			mid.type.setText("文档类型:  " + info.getString("typeName"));
			JSONArray tags = json.getJSONObject("docInfo2019").getJSONArray("tags");
			setMidTags(mid, tags);
		}
	}

	private String sdfDate(String temp) {
		String res = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (docInfoType == DocInfoType.docInfo) {
			try {
				res = sdf.format(sdf.parse(temp));
			} catch (ParseException e) {
				sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					res = sdf.format(sdf.parse(temp));
				} catch (ParseException e1) {
					return null;
				}
			}
		}
		if (docInfoType == DocInfoType.docInfo2019) {
			Date date = new Date();
			long milliSecond = Long.valueOf(temp);
			date.setTime(milliSecond);
			res = sdf.format(date);
		}
		return res;
	}

	private void setMidTags(Mid mid, JSONArray tags) {
		String res = "";
		for (int i = 0; i < tags.size(); i++) {
			JSONObject o = (JSONObject) tags.get(i);
			res += o.getString("tag");
			if (i == tags.size() - 1)
				break;
			res += "、";
		}
		mid.tags.setText("标签:  " + res);

	}

	private JSONObject getDocInfo(JSONObject json) {
		JSONObject tmp = json.getJSONObject("docInfo2019");
		if (tmp == null) {
			tmp = json.getJSONObject("docInfo");
		}
		return tmp;
	}

	private boolean login(WebDriver driver) {
		if (!loginPage(driver, false)) {
			log.info("检测到可能需要验证，正在重新调用chrome获取cookie");
			return headChromelogin(driver);
		}
		return false;
	}

	public boolean loginPage(WebDriver driver, boolean head) {
		// 回调窗口，获取输入的username,password
		WebElement nologin = driver.findElement(By.cssSelector("div[class~='user-icon-wrap']"));
		nologin.click();
		log.info("开始模拟登录。");
		WebElement qq = waitLoad(driver, "li[class='bd-acc-qzone']");
		qq.click();
		String main = driver.getWindowHandle();
		Set<String> all = driver.getWindowHandles();
		for (String temp : all) {
			if (driver.switchTo().window(temp).getTitle().contains("QQ帐号")) {
				driver.switchTo().window(temp);
				break;
			}
		}
		log.info("当前窗口为：" + driver.getTitle());
		driver.switchTo().frame("ptlogin_iframe");
		log.info("定位切换到ptlogin_iframe");
		WebElement loginByPw = waitLoad(driver, "a#switcher_plogin");
		loginByPw.click();
		WebElement uInput = waitLoad(driver, "div#uinArea input#u");
		uInput.sendKeys(map.get("username"));
		WebElement pInput = driver.findElement(By.cssSelector("div#pwdArea input#p"));
		pInput.sendKeys(map.get("pw"));
		WebElement input = driver.findElement(By.cssSelector("input#login_button"));
		input.click();
		log.info("模拟登录流程执行完毕。");
		driver.switchTo().window(main);
		log.info("切换回主窗口，同时判断是否登录状态。");
		int wtime = 10;
		if (head)
			wtime = 120;
		int index = 0;
		while (index < wtime) {
			if (checkCookie(parseHtml(driver.getPageSource()))) {
				try {
					File f = new File(System.getProperty("user.dir") + "\\conf\\json.txt");
					if (!f.exists())
						f.createNewFile();
					WebDriver.Options manage = driver.manage();
					Cookie c = manage.getCookieNamed("BDUSS");
					CookieUtil.setCookie(c);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
			try {
				Thread.sleep(1000);
				log.info("轮循当前登录状态，已经等待" + (++index) + "秒");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.info("登录页面跳转超时，请检查：账户密码是否正确、网络是否稳定、设备是否正常运行");
		return false;
	}

	public boolean headChromelogin(WebDriver mainDriver) {
		boolean flag = launchChrome4Head();
		mainDriver.manage().addCookie(CookieUtil.getCookie());
		mainDriver.navigate().refresh();
		return flag;
	}

	public boolean launchChrome4Head() {
		try {
			File file = new File(System.getProperty("user.dir") + "\\conf\\conf\\chrome-driver");
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String path = r.readLine();
			r.close();
			log.info(path);
			file = new File(path + "\\chromedriver.exe");
			System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
			// BDUSS
			ChromeDriverService service = new ChromeDriverService.Builder().usingDriverExecutable(file)
					.usingAnyFreePort().build();
			service.start();
			ChromeOptions options = new ChromeOptions();
			options.addArguments("disable-infobars");
//			options.addArguments("-headless");
			log.info("配置chrome启动方式为有头模式");
			log.info(StrUtil.chromePath);
			options.setBinary(new File(StrUtil.chromePath));
			log.info("正在回调有头浏览器窗口。");
			WebDriver driver = new ChromeDriver(options);
			log.info("有头chrome浏览器调用完成");
			driver.get(downloadModel.getUrl());
			loginPage(driver, true);
			driver.quit();
			service.stop();
			return true;
		} catch (Exception e) {
			log.info("chrome浏览器调用失败");
			return false;
		}
	}

	/**
	 * 等待该点击元素的加载
	 */
	public WebElement waitLoad(WebDriver driver, String css) {
		WebElement ele = null;
		while (true) {
			try {
				ele = driver.findElement(By.cssSelector(css));
				return ele;
			} catch (NoSuchElementException e) {
				log.error("通过css选择器，没有定位到" + css + ",1秒后在尝试");
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
		}
	}

	// 判断坐上角是否有登录标志符
	private boolean checkCookie(String pageData) {
		try {
			driver.findElement(By.cssSelector("div[class~='login']"));
			return true;
		} catch (Exception e) {
			System.out.println("不存在此元素");
			return false;
		}
	}

	public String parseHtml(String html) {
		Document doc = Jsoup.parse(html);
		Elements scripts = doc.select("script");
		for (Element e : scripts) {
			String text = e.data();
			if (!text.equals("")) {
				Pattern p = Pattern.compile("var pageData\\s*=\\s*(.*);");
				Matcher m = p.matcher(text);
				if (m.find())
					return m.group(1);
			}
		}
		return "";
	}

}
