package com.xiaoyu.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import com.xiaoyu.spider.DriverServer;
import com.xiaoyu.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LaunchFrame extends JDialog {

	private JWindow win = new JWindow();

	public static WebDriver driver;
	public static ChromeDriverService service;
	public CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(new BasicCookieStore()).build();

	private JPanel top = new JPanel();
	private JPanel bottom = new JPanel();
	private JLabel label = new JLabel("请选择chrome浏览器的安装目录，然后点击启动来启动程序。");
	private JTextField dir = new JTextField("C:\\", 40);
	private JButton browse = new JButton("浏览");
	private JButton yes = new JButton("启动");
	private JButton no = new JButton("取消");
	private String choose;
	private String chromeBack;

	public LaunchFrame() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { 0 };
		layout.columnWeights = new double[] { 1.0 };
		layout.rowHeights = new int[] { 0, 0 };
		layout.rowWeights = new double[] { 0.5, 1.0 };
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();

		top.setLayout(new BorderLayout());
		Box temp = Box.createHorizontalBox();
		temp.add(Box.createHorizontalStrut(30));
		temp.add(label);
		top.add(temp);

		bottom.setLayout(new FlowLayout());
		Box one = Box.createHorizontalBox();
		Box two = Box.createHorizontalBox();
		one.add(dir);
		one.add(Box.createHorizontalStrut(10));
		one.add(browse);
		two.add(Box.createHorizontalStrut(375));
		two.add(Box.createHorizontalGlue());
		two.add(yes);
		two.add(Box.createHorizontalStrut(20));
		two.add(no);
		bottom.add(one);
		bottom.add(two);

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		layout.setConstraints(top, c);
		add(top);

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		layout.setConstraints(bottom, c);
		add(bottom);

		String cp = getChromePath();
		dir.setText(cp);
		StrUtil.chromePath = cp;
		browse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int res = chooser.showDialog(LaunchFrame.this, "选择");
				if (res == JFileChooser.APPROVE_OPTION) {
					LaunchFrame.this.dir.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		yes.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				LaunchFrame.this.choose = LaunchFrame.this.dir.getText();
				synchronized (LaunchFrame.class) {
					log.info("notifyall");
					LaunchFrame.class.notifyAll();
				}
//				LaunchFrame.this.dispose();
				LaunchFrame.this.setVisible(false);
			}
		});

		no.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		setTitle(StrUtil.TITLE);
		setIconImage(new ImageIcon(StrUtil.LOGOPATH).getImage());
		setResizable(false);
		setSize(600, 160);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * 开启一个界面，显示调用信息 返回是否启动成功
	 */
	public boolean runChrome() {
		if (!findChrome(choose)) {
			return false;
		}
		// 显示一张图片，一个文本框
		// 文本框，定向输出流
		return rollMatch(win);
	}

	/**
	 * 根据选择的路径，扫描chrome.exe，然后更新到StrUtil.chromePath
	 * 
	 * @param choose
	 * @return
	 */
	private boolean findChrome(String choose) {
		// 先读取配置文件，看之前选择过的路径是否可用
		File t = new File(choose);
		if (t.exists()) {
			log.info("当前选择路径可用");
			if (t.isFile()) {
				log.info("当前选择路径为文件");
				boolean flag = t.getName().equals("chrome.exe");
				if (flag) {
					log.info("当前路径选择为chrome.exe");
				}
				return flag;
			}
		}
		log.info("正在扫描当前选择文件夹下的chrome.exe");
		File file = new File(choose);
		if (!file.exists()) {
			log.info(choose + "不存在。");
			return false;
		}
		Queue<File> queue = new LinkedList<>();
		queue.offer(file);
		while (!queue.isEmpty()) {
			File poll = queue.poll();
			log.info("扫描路径：" + poll.getAbsolutePath());
			for (File temp : poll.listFiles()) {
				if (temp.isDirectory()) {
					queue.offer(temp);
				} else {
					if (temp.getName().equals("chrome.exe")) {
						StrUtil.chromePath = temp.getAbsolutePath();
						setChromePath(StrUtil.chromePath);
						log.info("扫描到chrome.exe：" + StrUtil.chromePath);
						return true;
					}

				}
			}
		}
		return false;
	}

	/**
	 * 判断该文件的父文件夹是否存在，判断该文件是否存在
	 * 
	 * @param path
	 */
	public void decideOrCreate(String path) {
		try {
			File file = new File(path);
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取配置文件conf/chrome-exe中的路径
	 * 
	 * @return
	 */
	public String getChromePath() {
		String path = System.getProperty("user.dir") + "\\conf\\conf\\chrome-exe";
		decideOrCreate(path);
		return readFile(path);
	}

	public void setChromePath(String path) {
		try {
			File file = new File(System.getProperty("user.dir") + "\\conf\\conf\\chrome-exe");
			Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			w.write(path);
			w.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 滚动匹配，driver目录下的所有driver与当前选中的chrom是否能启动起来 不报错为启动成功，报错测试下一个driver
	 * 都不通过，调用cmd运行该选中的chrome.exe运行一个html文件，报告错误
	 * 报错：当前浏览器的版本为，当前程序支持的浏览器，建议使用版本（完美运行）。
	 */
	public boolean rollMatch(JWindow win) {
		String p = System.getProperty("user.dir") + "\\conf\\conf\\chrome-driver";
		decideOrCreate(p);
		File file = new File(p);
		String p2 = System.getProperty("user.dir") + "\\conf\\driver";
		File base = new File(p2);
		if (!base.exists())
			base.mkdirs();
		if (isNull(file)) {
			File path = downDriver(base);
			boolean flag = launchChrome(path);
//			if(!flag) {
//				return launchChrome4Head(path);
//			}
			return flag;
		} else {
			File t = new File(readFile(file.getAbsolutePath()) + "\\chromedriver.exe");
			if (launchChrome(t)) {
				return true;
			}
			File path = downDriver(base);
			return launchChrome(path);
		}
	}

	public boolean launchChrome(File file) {
		try {
			System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
			// BDUSS
			service = new ChromeDriverService.Builder().usingDriverExecutable(file)
					.usingAnyFreePort().build();
			service.start();
			ChromeOptions options = new ChromeOptions();
			options.addArguments("disable-infobars");
			options.addArguments("-headless");
			log.info("配置chrome启动方式为无头模式");
			options.setBinary(new File(StrUtil.chromePath));
			log.info("正在回调浏览器窗口。");
			driver = new ChromeDriver(options);
			log.info("chrome浏览器调用完成");
			return true;
		} catch (Exception e) {
			log.info("chrome浏览器调用失败");
			return false;
		}
	}
	

	/**
	 * 
	 * @param file要下载到的目录
	 * @return
	 */
	private File downDriver(File file) {
		try {
			DriverServer d = new DriverServer(9999);
			new Thread(() -> {
				chromeBack = null;
				chromeBack = d.server();
			}).start();
			String cmd = "cmd /k \"" + StrUtil.chromePath + "\" " + System.getProperty("user.dir")
					+ "\\conf\\html\\driver.html";
			Runtime.getRuntime().exec(cmd);
			while (true) {
				if (chromeBack != null) {
					d.stop();
					break;
				}
				Thread.currentThread().sleep(1000);
			}
			String browser = chromeBack.split(":")[0];
			String version = chromeBack.split(":")[1].split("\\.")[0];
			log.info("浏览器为：" + browser + ",版本为：" + version);
			File f = hasDriver(version, file);// user.dir + driver\90
			if (f != null)
				return f;
			if (down(version)) {
				log.info("driver:" + version + "下载完成");
				return new File(System.getProperty("user.dir") + "\\conf\\driver\\" + version + "\\chromedriver.exe");
			} else {
				log.info("下载失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean down(String version) {
		try {
			String url = "https://npm.taobao.org/mirrors/chromedriver/";
			String base = "https://npm.taobao.org";
			HttpGet get = new HttpGet(url);
			CloseableHttpResponse res = client.execute(get);
			String html = EntityUtils.toString(res.getEntity());
			Document doc = Jsoup.parse(html);
			Elements eles = doc.select("div.container pre a");
			for (Element ele : eles) {
				String text = ele.wholeText();
				log.info("1页的遍历元素为：" + text);
				if (text.split("\\.")[0].contains(version)) {
					HttpGet getV = new HttpGet(base + ele.attr("href"));
					Document docV = Jsoup.parse(EntityUtils.toString(client.execute(getV).getEntity()));
					Elements elesV = docV.select("div.container pre a");
					for (Element eleV : elesV) {
						log.info("2页的遍历元素为：" + eleV.wholeText());
						if (eleV.wholeText().contains("win32")) {
							log.info("开始下载" + version + "版本的driver");
							String target = base + eleV.attr("href");
							HttpGet d = new HttpGet(target);
							InputStream in = client.execute(d).getEntity().getContent();
							String path = System.getProperty("user.dir") + "\\tmp\\" + version + "\\driverchrome.zip";
							if (!new File(path).getParentFile().exists())
								new File(path).getParentFile().mkdirs();
							FileOutputStream out = new FileOutputStream(new File(path));
							byte[] b = new byte[1024 * 100];
							int len = -1;
							while ((len = in.read(b)) != -1) {
								out.write(b, 0, len);
							}
							out.close();
							in.close();
							log.info("下载完成，下载路径为：" + path);
							// 解压到resource目录
							unZip(new File(path),
									new File(System.getProperty("user.dir") + "\\conf\\driver\\" + version));
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			log.info("可能没有需要driver下载连接");
			e.printStackTrace();
		}
		return false;
	}

	public void unZip(File src, File dest) {
		try {
			log.info("开始解压文件");
			if (!dest.exists())
				dest.mkdirs();
			ZipFile file = new ZipFile(src);
			Enumeration<? extends ZipEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.isDirectory()) {
					String dirPath = dest.getAbsolutePath() + "\\" + entry.getName();
					File dir = new File(dirPath);
					dir.mkdirs();
				} else {
					File targetFile = new File(dest.getAbsoluteFile() + "\\" + entry.getName());
					if (!targetFile.getParentFile().exists()) {
						targetFile.getParentFile().mkdirs();
					}
					targetFile.createNewFile();
					InputStream is = file.getInputStream(entry);
					FileOutputStream fos = new FileOutputStream(targetFile);
					int len;
					byte[] buf = new byte[1024 * 100];
					while ((len = is.read(buf)) != -1) {
						fos.write(buf, 0, len);
					}
					// 关流顺序，先打开的后关闭
					fos.close();
					is.close();
					log.info("解压文件完成");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private File hasDriver(String version, File dir) {
		String path = System.getProperty("user.dir") + "\\conf\\conf\\chrome-driver";
		File t = new File(readFile(path));
		if (t.exists() && t.getName().equals(version)) {
			File t2 = new File(t.getAbsoluteFile() + "\\chromedriver.exe");
			if (t2.exists())
				return t2;
		}
		for (File temp : dir.listFiles()) {
			if (temp.getName().contains(version)) {
				try {
					Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path))));
					w.write(temp.getAbsolutePath());
					w.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return temp;
			}
		}
		return null;
	}

	public boolean isNull(File file) {
		String res = readFile(file.getAbsolutePath());
		return !new File(res).exists();
	}

	public static void main(String[] args) throws InterruptedException {
		LaunchFrame f = new LaunchFrame();
		synchronized (LaunchFrame.class) {
			LaunchFrame.class.wait();
		}
		f.runChrome();
		System.out.println("end");
	}

	public String readFile(String path) {
		String res = "";
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
			String temp = null;
			while ((temp = r.readLine()) != null) {
				res += temp;
			}
			r.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

}
