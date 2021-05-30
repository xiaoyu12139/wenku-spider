package com.xiaoyu.spider;

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
import com.xiaoyu.model.DownloadModel;
import com.xiaoyu.ui.LaunchFrame;
import com.xiaoyu.ui.panel.InputDialog;
import com.xiaoyu.ui.panel.Mid;
import com.xiaoyu.utils.CookieUtil;
import com.xiaoyu.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Fetch {

	private DownloadModel downloadModel = DownloadModel.getInstance();
	private Map<String, String> map = null;
	private CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore()).build();

	public void run() throws FileNotFoundException {
		WebDriver driver = LaunchFrame.driver;
		log.info("��������ڻص���ɣ���������ҳ�棺" + downloadModel.getUrl());
		driver.get(downloadModel.getUrl());
		log.info("ҳ��������ɡ�");
		Cookie cookie = CookieUtil.getCookie();
		if (cookie != null) {
			log.info("��ȡ���ش洢��cookie.");
			WebDriver.Options manage = driver.manage();
			manage.addCookie(cookie);
			log.info("����ˢ��ҳ�档");
			driver.navigate().refresh();
			log.info("ҳ��ˢ����ɡ�");
		}
		String pageSource = driver.getPageSource();
		String pageData = parseHtml(pageSource);
		log.info("ץȡ��pageData:" + pageData);
		log.info("��ʼУ�� cookie.");
		boolean flag = checkCookie(pageData);
		if (!flag) {
			log.info("cookie ���ڣ������µ�¼��");
			// ȷ���û����µ�¼��Ҳ�����û�����ɹ���¼��
			// ��ȡ��cookie���洢�����أ�����pageData
			// continue
			InputDialog in = new InputDialog();
			log.info("�ȴ��û�����");
//			synchronized (InputDialog.class) {
//				try {
//					InputDialog.class.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
			map = in.getMap();
			log.info("���룺" + map.get("pw"));
			log.info("�˺�����������ɡ�");
			if (login(driver)) {
				log.info("��¼�ɹ����������л�cookie�������ļ���");
				WebDriver.Options manage = driver.manage();
				Cookie c = manage.getCookieNamed("BDUSS");
				CookieUtil.setCookie(c);
				log.info("��¼�ɹ���׼����ʼץȡ��");
				pageData = parseHtml(driver.getPageSource());
				log.info("��ǰpageData:" + pageData);
			} else {
				log.info("��¼ʧ�ܣ��˳�����ץȡ��");
				return;
			}
		} else {
			log.info("cookie У��ͨ��.");
		}
		JSONObject json = JSON.parseObject(pageData);
		final JSONObject temp = json;
		new Thread(() -> {
			log.info("�����ĵ���Ϣ");
			setExplain(temp);
		}).start();
		log.info("ʵ�����ĵ�ץȡ��-FetchDoc");
		new FetchDoc().run(driver, json);
		log.info("����ץȡ���");
	}

	/**
	 * ����mid�������ʾ�ĸ��ĵ���һЩ��Ϣ
	 */
	private void setExplain(JSONObject json) {
		Mid mid = Mid.getInstance();
		mid.title.setText("����:  " + json.getJSONObject("docInfo2019").getJSONObject("doc_info").getString("title"));
		String temp = json.getJSONObject("docInfo2019").getJSONObject("doc_info").getString("create_time");
		Date date = new Date();
		long milliSecond = Long.valueOf(temp);
		date.setTime(milliSecond);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mid.createTime.setText("����ʱ��:  " + sdf.format(date));
//		mid.downProgress.setText(text);
		mid.type.setText("�ĵ�����:  " + json.getJSONObject("docInfo2019").getJSONObject("doc_info").getString("typeName"));
		String res = "";
		JSONArray tags = json.getJSONObject("docInfo2019").getJSONArray("tags");
		for (int i = 0; i < tags.size(); i++) {
			JSONObject o = (JSONObject) tags.get(i);
			res += o.getString("tag");
			if (i == tags.size() - 1)
				break;
			res += "��";
		}
		mid.tags.setText("��ǩ:  " + res);
	}

	private boolean login(WebDriver driver) {
		if(!loginPage(driver, false)) {
			log.info("��⵽������Ҫ��֤���������µ���chrome��ȡcookie");
			return headChromelogin(driver);
		}
		return false;
	}

	public boolean loginPage(WebDriver driver, boolean head) {
		// �ص����ڣ���ȡ�����username,password
		WebElement nologin = driver.findElement(By.cssSelector("div[class~='user-icon-content']"));
		nologin.click();
		log.info("��ʼģ���¼��");
		WebElement qq = waitLoad(driver, "li[class='bd-acc-qzone']");
		qq.click();
		String main = driver.getWindowHandle();
		Set<String> all = driver.getWindowHandles();
		for (String temp : all) {
			if (driver.switchTo().window(temp).getTitle().contains("QQ�ʺ�")) {
				driver.switchTo().window(temp);
				break;
			}
		}
		log.info("��ǰ����Ϊ��" + driver.getTitle());
		driver.switchTo().frame("ptlogin_iframe");
		log.info("��λ�л���ptlogin_iframe");
		WebElement loginByPw = waitLoad(driver, "a#switcher_plogin");
		loginByPw.click();
		WebElement uInput = waitLoad(driver, "div#uinArea input#u");
		uInput.sendKeys(map.get("username"));
		WebElement pInput = driver.findElement(By.cssSelector("div#pwdArea input#p"));
		pInput.sendKeys(map.get("pw"));
		WebElement input = driver.findElement(By.cssSelector("input#login_button"));
		input.click();
		log.info("ģ���¼����ִ����ϡ�");
		driver.switchTo().window(main);
		log.info("�л��������ڣ�ͬʱ�ж��Ƿ��¼״̬��");
		int wtime = 10;
		if(head)
			wtime = 120;
		int index = 0;
		while (index < wtime) {
			if (checkCookie(parseHtml(driver.getPageSource()))) {
				try {
					File f = new File(System.getProperty("user.dir") + "\\conf\\json.txt");
					if(!f.exists()) 
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
				log.info("��ѭ��ǰ��¼״̬���Ѿ��ȴ�" + (++index) + "��");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.info("��¼ҳ����ת��ʱ�����飺�˻������Ƿ���ȷ�������Ƿ��ȶ����豸�Ƿ���������");
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
			ChromeDriverService service = new ChromeDriverService.Builder().usingDriverExecutable(file).usingAnyFreePort().build();
			service.start();
			ChromeOptions options = new ChromeOptions();
			options.addArguments("disable-infobars");
//			options.addArguments("-headless");
			log.info("����chrome������ʽΪ��ͷģʽ");
			log.info(StrUtil.chromePath);
			options.setBinary(new File(StrUtil.chromePath));
			log.info("���ڻص���ͷ��������ڡ�");
			WebDriver driver = new ChromeDriver(options);
			log.info("��ͷchrome������������");
			driver.get(downloadModel.getUrl());
			loginPage(driver, true);
			driver.quit();
			service.stop();
			return true;
		} catch (Exception e) {
			log.info("chrome���������ʧ��");
			return false;
		}
	}

	/**
	 * �ȴ��õ��Ԫ�صļ���
	 */
	public WebElement waitLoad(WebDriver driver, String css) {
		WebElement ele = null;
		while (true) {
			try {
				ele = driver.findElement(By.cssSelector(css));
				return ele;
			} catch (NoSuchElementException e) {
				log.error("ͨ��cssѡ������û�ж�λ��" + css + ",1����ڳ���");
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
		}
	}

	private boolean checkCookie(String pageData) {
		JSONObject json = JSON.parseObject(pageData);
		JSONObject urls = json.getJSONObject("readerInfo2019").getJSONObject("htmlUrls");
		JSONArray jsons = urls.getJSONArray("json");
		String needSize = json.getJSONObject("docInfo2019").getJSONObject("doc_info").getString("page");
		log.info("��ǰ��ץȡ��" + jsons.size() + "ҳ");
		log.info("��ǰ�ĵ�����" + needSize + "ҳ");
		if (jsons.size() >= Integer.valueOf(needSize))
			return true;
		return false;
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
