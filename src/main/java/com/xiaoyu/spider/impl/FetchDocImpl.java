package com.xiaoyu.spider.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.MultidimensionalCounter.Iterator;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoyu.model.DownloadModel;
import com.xiaoyu.model.PageModel;
import com.xiaoyu.spider.FetchDoc;
import com.xiaoyu.ui.panel.Mid;
import com.xiaoyu.utils.CookieUtil;
import com.xiaoyu.utils.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FetchDocImpl implements FetchDoc {

	// BDUSS
	@Override
	public void run(WebDriver driver, JSONObject json) {
		try {
			initModel(json);
			download();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initModel(JSONObject json) {
		JSONObject urls = json.getJSONObject("readerInfo2019").getJSONObject("htmlUrls");
		JSONArray jsons = urls.getJSONArray("json");
		JSONArray pngs = urls.getJSONArray("png");
		//��ʼ��jsonurl
		initJsonUrl(jsons);
		//��ʼ��imgurl
		initImgUrl(jsons, pngs);
		//��ʼ��need
		initNeed();
		//����ÿҳimg
		sortImgs();
	}

	@Override
	public void download() {
		try {
			XWPFDocument document = new XWPFDocument();
			String fileName = mid.title.getText();
			if (fileName == null)
				fileName = "NB";
			File file = new File(System.getProperty("user.dir") + "\\downloads\\" + fileName + ".docx");
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);

			for(int i = 1; i <= docModel.keySet().size(); i++) {
				log.info("���������" + i + "ҳ��ҳ��ṹ");

				log.info("���������" + i + "ҳ��ҳ��ṹ���ظ�ҳ");

				mid.downProgress.setText("���ؽ���:  " + i + "/" + docModel.keySet().size());
			}
			document.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void parseAndDownPage() {
		
	}
	
	
	private void sortImgs() {
		if(docModel.keySet().size() == 0) {
			log.info("�ĵ���0ҳ");
			return;
		}
		for(int i = 1; i <= docModel.keySet().size(); i++) {
			PageModel model = docModel.get(i);
			List<JSONObject> needImage = model.getNeedImage();
			sortImg(needImage);
		}
	}

	private void sortImg(List<JSONObject> needImage) {
		List<JSONObject> res = new ArrayList<>();
		//resΪ�գ�ֱ�ӷ���
		//��Ȼ��resͷ��ʼ�Ƚϣ������ǰͼƬ�ڱ��Ƚϵ�ͼƬ��res�еģ�
		//������Ͳ�������ǰ�棬������뵽����
		//��ǰͼƬ��w = ix + iw; h = iy + ih; ���res_w >= w || res_h >= h
		//��ôres�ڸ�ͼƬ����
		for(java.util.Iterator<JSONObject> i = needImage.iterator(); i.hasNext();) {
			JSONObject next = i.next();
			JSONObject c = next.getJSONObject("c");
			int w = Integer.valueOf(c.getString("ix")) + Integer.valueOf(c.getString("iw"));
			int h = Integer.valueOf(c.getString("iy")) + Integer.valueOf(c.getString("ih"));
			for(java.util.Iterator<JSONObject> j = res.iterator(); j.hasNext();) {
				JSONObject resNext = i.next();
				JSONObject resC = resNext.getJSONObject("c");
				int resW = Integer.valueOf(c.getString("ix")) + Integer.valueOf(c.getString("iw"));
				int resH = Integer.valueOf(c.getString("iy")) + Integer.valueOf(c.getString("ih"));
				
			}
		}
	}

	private void initNeed() {
		for(java.util.Iterator<Integer> i = docModel.keySet().iterator(); i.hasNext();) {
			try {
				PageModel pageModel = docModel.get(i.next());
				String jsonUrl = pageModel.getJsonUrl();
				if(jsonUrl.equals("")) continue;
				String str = EntityUtils.toString(client.execute(new HttpGet(jsonUrl)).getEntity());
				JSONObject page = JSON.parseObject(str);
				JSONArray body = page.getJSONArray("body");
				for(int j = 0; j < body.size(); j++) {
					JSONObject obj = body.getJSONObject(j);
					if (obj.getString("t").equals("pic") && obj.getString("s") != null) {
						String tmp = obj.getJSONObject("s").getString("pic_file");
						Pattern p = Pattern.compile("_([0-9]*)_");
						Matcher m = p.matcher(tmp);
						if (m.find())
							tmp = m.group(1);
						if(docModel.containsKey(Integer.valueOf(tmp)))
							docModel.get(Integer.valueOf(tmp)).getNeedImage().add(obj);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initImgUrl(JSONArray jsons, JSONArray pngs) {
		for (int i = 0; i < pngs.size(); i++) {
			JSONObject o = jsons.getJSONObject(i);
			String url = o.getString("pageLoadUrl");
			String index = o.getString("pageIndex");
			if(docModel.containsKey(Integer.valueOf(index))) {
				docModel.get(Integer.valueOf(index)).setImagUrl(url);
				continue;
			}
			PageModel model = new PageModel();
			model.setImagUrl(url);
			docModel.put(Integer.valueOf(index), model);
		}
	}

	private void initJsonUrl(JSONArray jsons) {
		for (int i = 0; i < jsons.size(); i++) {
			JSONObject o = jsons.getJSONObject(i);
			String url = o.getString("pageLoadUrl");
			String index = o.getString("pageIndex");
			if(docModel.containsKey(Integer.valueOf(index))) {
				docModel.get(Integer.valueOf(index)).setJsonUrl(url);
				continue;
			}
			PageModel model = new PageModel();
			model.setJsonUrl(url);
			docModel.put(Integer.valueOf(index), model);
		}
	}

	//pageIndex
	// ���ݵõ���pageData�������õ��ĵ���Դ��ÿһҳ��urls
	public Map<String, List<String>> getAllPage(JSONObject json) {
		List<String> resJson = new ArrayList<String>();
		List<String> resPng = new ArrayList<String>();
		JSONObject urls = json.getJSONObject("readerInfo2019").getJSONObject("htmlUrls");
		JSONArray jsons = urls.getJSONArray("json");
		JSONArray pngs = urls.getJSONArray("png");
		for (int i = 0; i < jsons.size(); i++) {
			JSONObject o = jsons.getJSONObject(i);
			String url = o.getString("pageLoadUrl");
			resJson.add(url);
		}
		for (int i = 0; i < pngs.size(); i++) {
			JSONObject o = pngs.getJSONObject(i);
			String url = o.getString("pageLoadUrl");
			resPng.add(url);
		}
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.put("json", resJson);
		map.put("png", resPng);
		return map;
	}

	// ����ҳ��ṹ�����ܵ�ҳ������Ҫȫ����ҳ��һ�����

	// �����ĵ�ÿһҳ����Դ
	public void parseAllPage(JSONObject json) {
		try {
			XWPFDocument document = new XWPFDocument();
			String fileName = json.getString("title");
			if (fileName == null)
				fileName = "NB";
			File file = new File(System.getProperty("user.dir") + "\\downloads\\" + fileName + ".docx");
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			Map<String, List<String>> allPage = getAllPage(json);
			List<String> jsonUrls = allPage.get("json");
			List<String> pngUrls = allPage.get("png");

			mid.downProgress.setText("���ؽ���:  " + 0 + "/" + jsonUrls.size());

			for (int index = 0; index < jsonUrls.size(); index++) {
				HttpGet get = new HttpGet(jsonUrls.get(index));
				String sourse = EntityUtils.toString(client.execute(get).getEntity());
				Pattern p = Pattern.compile("wenku_" + (index + 1) + "\\((.*)\\)");
				Matcher m = p.matcher(sourse);
				if (m.find())
					sourse = m.group(1);
				JSONObject page = JSON.parseObject(sourse);
				JSONArray body = page.getJSONArray("body");
				log.info("���������" + (index + 1) + "ҳ��ҳ��ṹ");
				List<JSONObject> struct = parseBodyStruct(body);
				log.info("���������" + (index + 1) + "ҳ��ҳ��ṹ���ظ�ҳ");
				parseBody(struct, pngUrls, document);
				mid.downProgress.setText("���ؽ���:  " + (index + 1) + "/" + jsonUrls.size());
			}
			document.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<JSONObject> parseBodyStruct(JSONArray body) {
		List<JSONObject> listWord = new ArrayList<JSONObject>();// �ų��˿ո�word����pic
		List<JSONObject> listPic = new ArrayList<JSONObject>();// ֻ����url��pic
		for (int i = 0; i < body.size(); i++) {
			JSONObject obj = body.getJSONObject(i);
			String type = obj.getString("t");
			if (type.equals("word")) {
				listWord.add(obj);
			} else {
				if (obj.getString("s") == null) {
					listWord.add(obj);
					continue;
				}
				listPic.add(obj);
			}
		}
		// word - p -y
		// pic - c - iy
		int size = listPic.size();
		for (int i = 0; i < size; i++) {
			JSONObject pic = listPic.get(0);
			int j = 0;
			JSONObject cur = null;
			JSONObject last = null;
			for (; j < listWord.size(); j++) {
				cur = listWord.get(j);
				if (cur.getString("c").equals(" ") || (cur.getString("t").equals("pic") && cur.getString("s") == null))
					continue;
				if (last == null) {
					Double picy = Double.valueOf(pic.getJSONObject("p").getString("y"));
					Double wordy = Double.valueOf(cur.getJSONObject("p").getString("y"));
					if (picy < wordy) {
						listWord.add(j, pic);
						listPic.remove(pic);
						break;
					}
					last = cur;
					continue;
				}
				if (j == listWord.size() - 1) {
					Double picy = Double.valueOf(pic.getJSONObject("p").getString("y"));
					Double wordy = Double.valueOf(cur.getJSONObject("p").getString("y"));
					if (picy > wordy) {
						listWord.add(j, pic);
						listPic.remove(pic);
					}
					last = cur;
					continue;
				}
				Double lastWordy = Double.valueOf(last.getJSONObject("p").getString("y"));
				Double lastWordh = Double.valueOf(last.getJSONObject("p").getString("h"));
				Double picy = Double.valueOf(pic.getJSONObject("p").getString("y"));
//				if(pic.getJSONObject("p").getString("y0") == null)
//					System.out.println("null");
				Double picy0 = getYOrY0(pic);
				Double wordy = Double.valueOf(cur.getJSONObject("p").getString("y"));
				if (last.getString("t").equals("pic") && last.getString("s") != null) {
					Double lastWordy0 = getYOrY0(last);
					if (picy > lastWordy0 && picy0 < wordy) {
						listWord.add(j, pic);
						listPic.remove(pic);
						break;
					}
				}
				if (picy > (lastWordh + lastWordy) && (picy0 < wordy)) {
					listWord.add(j, pic);
					listPic.remove(pic);
					break;
				}
				last = cur;
			}
			if (listPic.contains(pic)) {
				Double picy = Double.valueOf(pic.getJSONObject("p").getString("y"));
				if (last == null) {
					listWord.add(j, pic);
					listPic.remove(pic);
					continue;
				}
				Double wordy = Double.valueOf(last.getJSONObject("p").getString("y"));
				if (picy > wordy) {
					listWord.add(j - 1, pic);
					listPic.remove(pic);
				}
			}
		}
		return listWord;
	}

	public Double getYOrY0(JSONObject pic) {
		if (pic.getJSONObject("p").getString("y0") == null) {
			return Double.valueOf(pic.getJSONObject("p").getString("y"))
					+ Double.valueOf(pic.getJSONObject("p").getString("h"));
		}
		return Double.valueOf(pic.getJSONObject("p").getString("y0"));
	}

	private void parseBody(List<JSONObject> body, List<String> pngs, XWPFDocument doc) {
		try {
			XWPFParagraph p = doc.createParagraph();
			for (int i = 0; i < body.size(); i++) {
				JSONObject c = body.get(i);// ÿ�е�����
				if (c.getString("t").equals("word")) {
					if (c.getString("c").equals(" ")) {
						p = doc.createParagraph();
						continue;
					}
					XWPFRun run = p.createRun();
					run.setText(c.getString("c"));
				}
				if (c.getString("t").equals("pic")) {
					if (c.getJSONObject("s") == null) {
						p = doc.createParagraph();
						continue;
					}
					insert(doc, p, c, pngs);
					p = doc.createParagraph();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insert(XWPFDocument doc, XWPFParagraph p, JSONObject c, List<String> pngs)
			throws InvalidFormatException, IOException {
		String index = c.getJSONObject("s").getString("pic_file");
		Pattern pa = Pattern.compile("_([0-9]*)_");
		Matcher m = pa.matcher(index);
		if (m.find())
			index = m.group(1);
		String url = pngs.get(Integer.valueOf(index) - 1);
		System.out.println(url);
		File temp = new File(System.getProperty("user.dir") + "\\tmp");
		if (!temp.exists())
			temp.mkdir();
		File tempPic = new File(temp.getAbsoluteFile() + "\\" + index + ".png");
		downloadPic(url, tempPic);
		p = doc.createParagraph();
		XWPFRun run = p.createRun();
		// ix,iyΪĿ��ͼƬ�����Ͻ�λ�ã�iw,ihΪ���
		// ͼƬβ�ŵ���body����ı�žͲ���ͼƬ
		// ��������ھͲ��뵽��ǰβ��-���body��������pic������ - 1��λ��
		// ������ת������λ�ô��ڵ�ǰbody��������и�������ֱ�Ӳ���
		JSONObject pngWH = c.getJSONObject("c");// ��ȡpic�ĸߺͿ�,595��842
		Double ix = Double.valueOf(pngWH.getString("ix"));
		Double iy = Double.valueOf(pngWH.getString("iy"));
		Double w = Double.valueOf(pngWH.getString("iw"));
		Double h = Double.valueOf(pngWH.getString("ih"));
		InputStream in = bufferedImageToInputStream(tempPic, ix.intValue(), iy.intValue(), w.intValue(), h.intValue());
		String pic = doc.addPictureData(IOUtils.toByteArray(in),
				org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG);
		Double[] wh = getWH(w, h);
		FetchDocImpl.addPictureToRun(run, pic, org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG,
				wh[0].intValue(), wh[1].intValue());
	}

	public InputStream bufferedImageToInputStream(File file, int x, int y, int w, int h) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BufferedImage image = ImageIO.read(file);
		BufferedImage res = image.getSubimage(x, y, w, h);
		ImageIO.write(res, "png", os);
		return new ByteArrayInputStream(os.toByteArray());
	}

	// h / w = 840 / x
	public Double[] getWH(double w, double h) {
		double r = h / w;
		if (w > h) {
			if (w > 590.0) {
				w = 590.0;
				h = r * w;
			}
		} else {
			if (h > 840) {
				h = 840.0;
				w = h / r;
			}
		}
		if (w > 590.0) {
			w = 590.0;
			h = r * w;
		}
		if (h > 840) {
			h = 840.0;
			w = h / r;
		}
		return new Double[] { w, h };
	}

	public void downloadPic(String url, File file) {
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

	/**
	 * ��POI 3.8�Դ���BUG ������ӽ���ͼƬ����ʾ��ֻ��һ��ͼƬ�򣬽�ͼƬ���Ϊ���������ͼƬ��һ��PNG��ʽ��͸��ͼƬ �����Զ������ͼƬ�ķ���
	 * ��Run�в���ͼƬ(�����word�в���ʾ������)
	 * 
	 * @param run
	 * @param blipId ͼƬ��id
	 * @param id     ͼƬ������
	 * @param width  ͼƬ�Ŀ�
	 * @param height ͼƬ�ĸ�
	 * @author lgj
	 */
	public static void addPictureToRun(XWPFRun run, String blipId, int id, int width, int height) {
		final int EMU = 9525;
		width *= EMU;
		height *= EMU;

		CTInline inline = run.getCTR().addNewDrawing().addNewInline();

		String picXml = "" + "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "         <pic:nvPicPr>" + "            <pic:cNvPr id=\"" + id + "\" name=\"Generated\"/>"
				+ "            <pic:cNvPicPr/>" + "         </pic:nvPicPr>" + "         <pic:blipFill>"
				+ "            <a:blip r:embed=\"" + blipId
				+ "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>"
				+ "            <a:stretch>" + "               <a:fillRect/>" + "            </a:stretch>"
				+ "         </pic:blipFill>" + "         <pic:spPr>" + "            <a:xfrm>"
				+ "               <a:off x=\"0\" y=\"0\"/>" + "               <a:ext cx=\"" + width + "\" cy=\""
				+ height + "\"/>" + "            </a:xfrm>" + "            <a:prstGeom prst=\"rect\">"
				+ "               <a:avLst/>" + "            </a:prstGeom>" + "         </pic:spPr>"
				+ "      </pic:pic>" + "   </a:graphicData>" + "</a:graphic>";

		// CTGraphicalObjectData graphicData =
		// inline.addNewGraphic().addNewGraphicData();
		XmlToken xmlToken = null;
		try {
			xmlToken = XmlToken.Factory.parse(picXml);
		} catch (XmlException xe) {
			xe.printStackTrace();
		}
		inline.set(xmlToken);
		// graphicData.set(xmlToken);

		inline.setDistT(0);
		inline.setDistB(0);
		inline.setDistL(0);
		inline.setDistR(0);

		CTPositiveSize2D extent = inline.addNewExtent();
		extent.setCx(width);
		extent.setCy(height);

		CTNonVisualDrawingProps docPr = inline.addNewDocPr();
		docPr.setId(id);
		docPr.setName("Picture " + id);
		docPr.setDescr("Generated");
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		FetchDocImpl main = new FetchDocImpl();
//		main.run();
	}

}
