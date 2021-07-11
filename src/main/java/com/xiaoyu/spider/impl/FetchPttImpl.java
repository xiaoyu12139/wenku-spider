package com.xiaoyu.spider.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.openqa.selenium.WebDriver;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoyu.model.DocInfoType;
import com.xiaoyu.spider.FetchPtt;

public class FetchPttImpl implements FetchPtt {

	DocInfoType docInfoType;

	@Override
	public void run(WebDriver driver, JSONObject json, DocInfoType docInfoType) {
		try {
			this.docInfoType = docInfoType;
			download(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void download(JSONObject json) throws Exception {
		XMLSlideShow ppt = new XMLSlideShow();
		File out = new File(System.getProperty("user.dir") + "\\downloads");
		if(!out.exists())
			out.mkdir();
		File tmp = new File(out.getAbsoluteFile() + "\\" + mid.title.getText().substring(5) + ".pptx");
		if(tmp.exists())
			tmp = new File(out.getAbsoluteFile() + "\\" + mid.title.getText().substring(5) + new Date().getTime() + ".pptx");
		out = tmp;
		JSONArray urls = getUrls(json);
		for (int i = 0; i < urls.size(); i++) {
			String url = urls.getString(i);
			File temp = new File(System.getProperty("user.dir") + "\\tmp");
			if (!temp.exists())
				temp.mkdir();
			File tempPic = new File(temp.getAbsoluteFile() + "\\" + i + ".png");
			downPic(url, tempPic);
			BufferedImage image = ImageIO.read(tempPic);
			ppt.setPageSize(new Dimension(image.getWidth(), image.getHeight()));
			XSLFSlide slide = ppt.createSlide();
			byte[] pictureData = IOUtils.toByteArray(new FileInputStream(tempPic));
			XSLFPictureData pictureIndex = ppt.addPicture(pictureData, PictureType.PNG);
			XSLFPictureShape pictureShape = slide.createPicture(pictureIndex);
			pictureShape.setAnchor(new java.awt.Rectangle(0, 0, ppt.getPageSize().width, ppt.getPageSize().height));
			mid.downProgress.setText("ÏÂÔØ½ø¶È:  " + i + "/" + urls.size());
		}
		ppt.write(new FileOutputStream(out));
	}

	private void downPic(String url, File file) {
		try {
			HttpGet get = new HttpGet(url);
			CloseableHttpResponse res = client.execute(get);
			InputStream in = res.getEntity().getContent();
			FileOutputStream out = new FileOutputStream(file);
			int b = -1;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONArray getUrls(JSONObject json) {
		if (docInfoType == DocInfoType.docInfo2019) {
			return json.getJSONObject("readerInfo2019").getJSONArray("htmlUrls");
		}
		if (docInfoType == DocInfoType.docInfo) {
			return json.getJSONObject("readerInfo").getJSONArray("htmlUrls");
		}
		return null;
	}

}
