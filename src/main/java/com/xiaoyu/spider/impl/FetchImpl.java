package com.xiaoyu.spider.impl;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.http.client.ClientProtocolException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
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
			// ????ҳ??
			requestPage();
			if (!isNeedLogin())
				return;
			// ??ȡ????cookie,????cookie
			boolean overdue = readLocalCookie();
			// cookie???????¿???????????????ȡ???û??????????룬???е?¼
			// ??¼?ɹ??󣬽??õ?¼?ɹ???ҳ????cookie??¼?????أ??رյ?ǰ??ʱ??????
			// ???¶?ȡ????cookie????ˢ?µ?ǰҳ??
			if (!overdue)
				if (!reloadCookie())
					return;
			checkTypeAndInvoke();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isNeedLogin() {
		try {
			driver.findElement(By.cssSelector("div[class~='goto-page']"));
			return true;
		} catch (Exception e) {
			System.out.println("?????ڴ?Ԫ??:goto-page");
			mid.title.setText(" ");
			mid.createTime.setText(" ");
			mid.tags.setText(" ");
			mid.type.setText(" ");
			// download
			Document doc = Jsoup.parse(driver.getPageSource());
			Element title = doc.getElementsByTag("title").first();
			Element root = doc.getElementById("reader-container");
			Elements ps = root.getElementsByTag("p");
			try {
				XWPFDocument document = new XWPFDocument();
				String fileName = title.wholeText();
				if (fileName == null)
					fileName = "NB";
				File file = new File(System.getProperty("user.dir") + "\\downloads\\" + fileName + ".docx");
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				if (!file.exists())
					file.createNewFile();
				FileOutputStream out = new FileOutputStream(file);
				int index = 1;
				for (Iterator<Element> i = ps.iterator(); i.hasNext();) {
					Element next = i.next();
					if (!pageDown(next, document))
						document.createParagraph().createRun().setText(next.wholeText());
					mid.downProgress.setText("???ؽ???:  " + index++ + "/" + ps.size());
				}
				log.info("????????");
				document.write(out);
				out.close();
			} catch (Exception ee) {
				ee.printStackTrace();
			}
			return false;
		}
	}

	public boolean pageDown(Element next, XWPFDocument document)
			throws ClientProtocolException, IOException, InvalidFormatException {
		Elements imgs = next.getElementsByTag("img");
		if (imgs.size() > 0) {
			for (Iterator<Element> i = imgs.iterator(); i.hasNext();) {
				try {
					Element img = i.next();
					log.info("????img");
					String url = img.attr("src");
					File temp = new File(System.getProperty("user.dir") + "\\tmp");
					if (!temp.exists())
						temp.mkdir();
					File tempPic = new File(temp.getAbsoluteFile() + "\\tmp.png");
					new FetchDocImpl().downloadPic(url, tempPic);
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					BufferedImage image = ImageIO.read(tempPic);
					ImageIO.write(image, "png", os);
					Double[] wh = new FetchDocImpl().getWH(new Double(image.getWidth()), new Double(image.getHeight()));
					String pic = document.addPictureData(
							IOUtils.toByteArray(new ByteArrayInputStream(os.toByteArray())),
							org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG);
					FetchDocImpl.addPictureToRun(document.createParagraph().createRun(), pic,
							org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG, wh[0].intValue(),
							wh[1].intValue());
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private void requestPage() {
		log.info("?????????ڻص????ɣ?????????ҳ?棺" + downloadModel.getUrl());
		driver.get(downloadModel.getUrl());
		log.info("ҳ?????????ɡ?");
	}

	/**
	 * cookieʧЧ????false
	 * 
	 * @return
	 */
	private boolean readLocalCookie() {
		Cookie cookie = CookieUtil.getCookie();
		if (cookie != null) {
			log.info("??ȡ???ش洢??cookie.");
			WebDriver.Options manage = driver.manage();
			manage.addCookie(cookie);
			log.info("????ˢ??ҳ?档");
			driver.navigate().refresh();
			log.info("ҳ??ˢ?????ɡ?");
		}
		String pageSource = driver.getPageSource();
		String pageData = parseHtml(pageSource);
		log.info("ץȡ??pageData:" + pageData);
		log.info("??ʼУ?? cookie.");
		return checkCookie(pageSource);
	}

	private boolean reloadCookie() {
		String pageSource = driver.getPageSource();
		String pageData = parseHtml(pageSource);
		log.info("cookie ???ڣ??????µ?¼??");
		InputDialog in = new InputDialog();
		log.info("?ȴ??û?????");
		map = in.getMap();
		log.info("???룺" + map.get("pw"));
		log.info("?˺????????????ɡ?");
		if (login(driver)) {
			log.info("??¼?ɹ??????????л?cookie???????ļ???");
			WebDriver.Options manage = driver.manage();
			Cookie c = manage.getCookieNamed("BDUSS");
			CookieUtil.setCookie(c);
			log.info("??¼?ɹ???׼????ʼץȡ??");
			pageData = parseHtml(driver.getPageSource());
			log.info("??ǰpageData:" + pageData);
		} else {
			log.info("??¼ʧ?ܣ??˳?????ץȡ??");
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
			log.info("?????ĵ???Ϣ");
			setExplain(temp);
		}).start();
		log.info("ʵ?????ĵ?ץȡ??-FetchDoc");
		if (docInfoType == DocInfoType.docInfo) {
			JSONObject info = json.getJSONObject("viewBiz").getJSONObject("docInfo");
			String type = info.getString("fileType");
			if (type.equals("txt")) {
				new FetchTxtImpl().run(driver, pageSource, docInfoType);
			} else if (type.equals("word")) {
				new FetchDocImpl().run(driver, json, docInfoType);
			} else if (type.equals("ppt")) {
				new FetchPttImpl().run(driver, json, docInfoType);
			} else if (type.equals("pdf")) {
				new FetchPdfImpl().run(driver, json, docInfoType);
			}
		}
		if (docInfoType == DocInfoType.docInfo2019) {
			JSONObject info = json.getJSONObject("docInfo2019").getJSONObject("doc_info");
			String type = info.getString("typeName");
			if (type.equals("txt")) {
				new FetchTxtImpl().run(driver, pageSource, docInfoType);
			} else if (type.equals("word")) {
				new FetchDocImpl().run(driver, json, docInfoType);
			} else if (type.equals("ppt")) {
				new FetchPttImpl().run(driver, json, docInfoType);
			} else if (type.equals("pdf")) {
				new FetchPdfImpl().run(driver, json, docInfoType);
			}
		}
		log.info("????ץȡ????");
	}

	private void checkDocInfoType(JSONObject json) {
		if (json.getJSONObject("docInfo2019") != null)
			docInfoType = DocInfoType.docInfo2019;
	}

	/**
	 * ????mid????????ʾ?ĸ??ĵ???һЩ??Ϣ
	 */
	private void setExplain(JSONObject json) {
		Mid mid = Mid.getInstance();
		if (docInfoType == DocInfoType.docInfo) {
			JSONObject info = json.getJSONObject("viewBiz").getJSONObject("docInfo");
			mid.title.setText("????:  " + info.getString("title"));
			mid.type.setText("?ĵ?????:  " + info.getString("fileType"));
			String temp = info.getString("showCreateTime");
			mid.createTime.setText("????ʱ??:  " + sdfDate(temp));
			JSONArray tags = json.getJSONObject("viewBiz").getJSONArray("tags");
			setMidTags(mid, tags);
		}
		if (docInfoType == DocInfoType.docInfo2019) {
			JSONObject info = json.getJSONObject("docInfo2019").getJSONObject("doc_info");
			mid.title.setText("????:  " + info.getString("title"));
			String temp = info.getString("create_time");
			mid.createTime.setText("????ʱ??:  " + sdfDate(temp));
			mid.type.setText("?ĵ?????:  " + info.getString("typeName"));
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
			res += "??";
		}
		mid.tags.setText("??ǩ:  " + res);

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
			log.info("???⵽??????Ҫ??֤?????????µ???chrome??ȡcookie");
			return headChromelogin(driver);
		}
		return true;
	}

	public boolean loginPage(WebDriver driver, boolean head) {
		// ?ص????ڣ???ȡ??????username,password
		WebElement nologin = driver.findElement(By.cssSelector("div[class~='user-icon-wrap']"));
		nologin.click();
		log.info("??ʼģ????¼??");
		WebElement qq = waitLoad(driver, "li[class='bd-acc-qzone']");
		qq.click();
		String main = driver.getWindowHandle();
		Set<String> all = driver.getWindowHandles();
		for (String temp : all) {
			if (driver.switchTo().window(temp).getTitle().contains("QQ?ʺ?")) {
				driver.switchTo().window(temp);
				break;
			}
		}
		log.info("??ǰ????Ϊ??" + driver.getTitle());
		driver.switchTo().frame("ptlogin_iframe");
		log.info("??λ?л???ptlogin_iframe");
		WebElement loginByPw = waitLoad(driver, "a#switcher_plogin");
		loginByPw.click();
		WebElement uInput = waitLoad(driver, "div#uinArea input#u");
		uInput.sendKeys(map.get("username"));
		WebElement pInput = driver.findElement(By.cssSelector("div#pwdArea input#p"));
		pInput.sendKeys(map.get("pw"));
		WebElement input = driver.findElement(By.cssSelector("input#login_button"));
		input.click();
		log.info("ģ????¼????ִ?????ϡ?");
		driver.switchTo().window(main);
		log.info("?л????????ڣ?ͬʱ?ж??Ƿ???¼״̬??");
		int wtime = 10;
		if (head)
			wtime = 60;
		int index = 0;
		while (index < wtime) {
			if (checkCookie(driver.getPageSource())) {
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
				log.info("??ѭ??ǰ??¼״̬???Ѿ??ȴ?" + (++index) + "??");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.info("??¼ҳ????ת??ʱ???????飺?˻??????Ƿ???ȷ???????Ƿ??ȶ????豸?Ƿ?????????");
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
			log.info("????chrome??????ʽΪ??ͷģʽ");
			log.info(StrUtil.chromePath);
			options.setBinary(new File(StrUtil.chromePath));
			log.info("???ڻص???ͷ?????????ڡ?");
			WebDriver driver = new ChromeDriver(options);
			log.info("??ͷchrome??????????????");
			driver.get(downloadModel.getUrl());
			loginPage(driver, true);
			driver.quit();
			service.stop();
			return true;
		} catch (Exception e) {
			log.info("chrome??????????ʧ??");
			return false;
		}
	}

	/**
	 * ?ȴ??õ???Ԫ?صļ???
	 */
	public WebElement waitLoad(WebDriver driver, String css) {
		WebElement ele = null;
		while (true) {
			try {
				ele = driver.findElement(By.cssSelector(css));
				return ele;
			} catch (NoSuchElementException e) {
				log.error("ͨ??cssѡ??????û?ж?λ??" + css + ",1?????ڳ???");
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
		}
	}

	// ?ж????Ͻ??Ƿ??е?¼??־??
	private boolean checkCookie(String html) {
		try {
			Document doc = Jsoup.parse(html);
			Elements divs = doc.getElementsByTag("div");
			for(Iterator<Element> i = divs.iterator(); i.hasNext(); ) {
				Element next = i.next();
				String attr = next.attr("class");
				String[] split = attr.split(" ");
				List<String> asList = Arrays.asList(split);
				if(asList.contains("login")) {
					return true;
				}
			}
//			driver.findElement(By.cssSelector("div[class~='login']"));
			return false;
		} catch (Exception e) {
			System.out.println("?????ڴ?Ԫ??");
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
