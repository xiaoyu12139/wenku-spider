package com.xiaoyu.spider.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.openqa.selenium.WebDriver;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.xiaoyu.model.DocInfoType;
import com.xiaoyu.spider.FetchPdf;

public class FetchPdfImpl implements FetchPdf{
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
		Document document =new Document(getPageSize(json)); 
		JSONArray jsons = getUrls(json).getJSONArray("json");
		JSONArray pngs = getUrls(json).getJSONArray("png");
		File out = new File(System.getProperty("user.dir") + "\\downloads");
		if(!out.exists())
			out.mkdir();
		out = new File(out.getAbsoluteFile() + "\\" + mid.title.getText().substring(5) + ".pdf");
		if(out.exists())
			out = new File(out.getAbsoluteFile() + "\\" + mid.title.getText().substring(5) + new Date().getTime() + ".pdf");
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(out));
		document.open();
		for(int i = 0; i < jsons.size(); i++) {
			JSONObject url = jsons.getJSONObject(i);
			String index = url.getString("pageIndex");
			String jsonUrl = url.getString("pageLoadUrl");
			pageDown(document, jsonUrl, Integer.valueOf(index), pngs);
			mid.downProgress.setText("下载进度:  " + index + "/" + jsons.size());
		}
		document.close();
	}
	
	private void pageDown(Document document, String jsonUrl, int index, JSONArray pngs) throws MalformedURLException, IOException, DocumentException {
//		String str = EntityUtils.toString(client.execute(new HttpGet(jsonUrl)).getEntity());
//		Pattern p = Pattern.compile("wenku_" + index + "\\((.*)\\)");
//		Matcher m = p.matcher(str);
//		if (m.find())
//			str = m.group(1);
//		JSONObject page = JSON.parseObject(str);
//		JSONArray body = page.getJSONArray("body");
//		for(int i = 0; i < body.size(); i ++) {
//			JSONObject obj = body.getJSONObject(i);
//			
//		}
		String url = pngs.getJSONObject(index - 1).getString("pageLoadUrl");
		File temp = new File(System.getProperty("user.dir") + "\\tmp");
		if (!temp.exists())
			temp.mkdir();
		File tempPic = new File(temp.getAbsoluteFile() + "\\" + index + ".png");
		downPic(url, tempPic);
		String path = tempPic.getAbsolutePath();
		Image image = Image.getInstance(path);
		image.setAlignment(Image.ALIGN_CENTER);
//		image.scalePercent(40);//依照比例缩放
		document.add(image);
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
	
	private JSONObject getUrls(JSONObject json) {
		if (docInfoType == DocInfoType.docInfo2019) {
			return json.getJSONObject("readerInfo2019").getJSONObject("htmlUrls");
		}
		if (docInfoType == DocInfoType.docInfo) {
			return json.getJSONObject("readerInfo").getJSONObject("htmlUrls");
		}
		return null;
	}

	private Rectangle getPageSize(JSONObject json) {
		if (docInfoType == DocInfoType.docInfo2019) {
			return null;
		}
		if (docInfoType == DocInfoType.docInfo) {
			double pageH = Double
					.valueOf(json.getJSONObject("readerInfo").getJSONObject("pageInfo").getString("pageHeight"));
			double pageW = Double
					.valueOf(json.getJSONObject("readerInfo").getJSONObject("pageInfo").getString("pageWidth"));
			return new Rectangle((int)pageW, (int)pageH);
		}
		return null;
	}

}
