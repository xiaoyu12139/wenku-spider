package com.xiaoyu.spider.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

import com.xiaoyu.model.DocInfoType;
import com.xiaoyu.spider.FetchTxt;

public class FetchTxtImpl implements FetchTxt {

	@Override
	public void run(WebDriver driver, String html, DocInfoType docInfoType) {
		try {
			download(html);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void download(String html) throws Exception {
		String fileName = mid.title.getText();
		if (fileName == null)
			fileName = "NB";
		File file = new File(System.getProperty("user.dir") + "\\downloads\\" + fileName.substring(5) + ".txt");
		if(file.exists())
			file = new File(System.getProperty("user.dir") + "\\downloads\\" + fileName.substring(5) + new Date().getTime() + ".txt");
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		if (!file.exists())
			file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);
		Document doc = Jsoup.parse(html);
		Elements container = doc.getElementById("reader-container").children();
		int pageIndex = 1;
		for(Iterator<Element> i = container.iterator(); i.hasNext();) {
			Element next = i.next();
			pageDownload(next, out);
			mid.downProgress.setText("ÏÂÔØ½ø¶È:  " + pageIndex++ + "/" + container.size());
		}
		out.close();
	}

	private void pageDownload(Element page, FileOutputStream out) throws IOException {
		Elements ps = page.getElementsByTag("p");
		for(Iterator<Element> i = ps.iterator(); i.hasNext();) {
			Element next = i.next();
			String text = next.wholeText();
			out.write(text.getBytes());
		}
	}

}
