package com.xiaoyu.spider.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openqa.selenium.WebDriver;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoyu.model.DocInfoType;
import com.xiaoyu.model.PageModel;
import com.xiaoyu.spider.FetchDoc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FetchDocImpl implements FetchDoc {
	// key - 页数 value - 当前页的数据模型
	Map<Integer, PageModel> docModel = new HashMap<>();
	public DocInfoType docInfoType;
	public List<Double> virtualPageH = new ArrayList<Double>();
	public List<Double> virtualPageW = new ArrayList<Double>();
	public Boolean isInsert = false;
	public double pageW = 0;
	public double pageH = 0;

	// BDUSS
	@Override
	public void run(WebDriver driver, JSONObject json, DocInfoType docInfoType) {
		try {
			this.docInfoType = docInfoType;
			initModel(json);
			download();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initModel(JSONObject json) {
		JSONObject urls = getUrls(json);
		JSONArray jsons = urls.getJSONArray("json");
		JSONArray pngs = urls.getJSONArray("png");
		// 初始化jsonurl
		initJsonUrl(jsons);
		// 初始化imgurl
		initImgUrl(jsons, pngs);
		// 初始化need
		initNeed();
		// 排序每页img
		sortImgs();
	}

	@Override
	public void download() {
		try {
			XWPFDocument document = new XWPFDocument();
			String fileName = mid.title.getText();
			if (fileName == null)
				fileName = "NB";
			File file = new File(System.getProperty("user.dir") + "\\downloads\\" + fileName.substring(5) + ".docx");
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
			else
				file = new File(System.getProperty("user.dir") + "\\downloads\\" + fileName.substring(5) + new Date().getTime() + ".docx");
			FileOutputStream out = new FileOutputStream(file);

			for (int i = 1; i <= docModel.keySet().size(); i++) {
				virtualPageH.clear();
				virtualPageW.clear();
				parseAndDownPage(document, docModel.get(i), i);
				mid.downProgress.setText("下载进度:  " + i + "/" + docModel.keySet().size());
			}
			document.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void parseAndDownPage(XWPFDocument document, PageModel pageModel, int pageIndex) {
		try {
			String jsonUrl = pageModel.getJsonUrl();
			String imagUrl = pageModel.getImagUrl();
			LinkedList<JSONObject> needImage = pageModel.getNeedImage();
			String source = EntityUtils.toString(client.execute(new HttpGet(jsonUrl)).getEntity());
			Pattern pa = Pattern.compile("wenku_" + pageIndex + "\\((.*)\\)");
			Matcher m = pa.matcher(source);
			if (m.find())
				source = m.group(1);
			JSONArray body = JSON.parseObject(source).getJSONArray("body");
			XWPFParagraph p = document.createParagraph();
			JSONArray tmp = new JSONArray();
			for (int i = 0; i < body.size(); i++) {
				JSONObject obj = body.getJSONObject(i);
				boolean isWord = obj.getString("t").equals("word");
				if (isWord) {
					tmp.add(obj);
				}
			}
			Set<JSONObject> set = new HashSet<>();
			for (int i = 0; i < tmp.size(); i++) {
				JSONObject obj = tmp.getJSONObject(i);
				if (obj.getString("c").equals(" ") && obj.getJSONObject("ps") != null
						&& obj.getJSONObject("ps").getString("_enter") != null
						&& obj.getJSONObject("ps").getString("_enter").equals("1"))
					set.add(obj);
			}
			body = tmp;
			for (int i = 0; i <= body.size(); i++) {
				if (i != body.size()) {
					JSONObject obj = body.getJSONObject(i);
					if (set.contains(obj)) {
						p = document.createParagraph();
						continue;
					}
					if (obj.getString("c").equals(" ")) {
						continue;
					}
				}
				p = insert2Doc4WordOrPic(body, document, i, p, needImage, imagUrl, pageIndex, set);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private XWPFParagraph insert2Doc4WordOrPic(JSONArray body, XWPFDocument document, int cur, XWPFParagraph p,
			LinkedList<JSONObject> needImage, String imagUrl, int pageIndex, Set<JSONObject> set) {
		JSONObject pr = null;
		double pY = 0;
		double pX = 0;
		double pW = 0;
		double pH = 0;
		double pZ = 0;
		for (int i = cur; i >= 0; i--) {
			if (i == 0) {
				pr = null;
				break;
			} else {
				pr = body.getJSONObject(i - 1);
			}
			if (!pr.getString("c").equals(" ")) {
				pY = Double.valueOf(pr.getJSONObject("p").getString("y"));
				pX = Double.valueOf(pr.getJSONObject("p").getString("x"));
				pW = Double.valueOf(pr.getJSONObject("p").getString("w"));
				pH = Double.valueOf(pr.getJSONObject("p").getString("h"));
				pZ = Double.valueOf(pr.getJSONObject("p").getString("z"));
				break;
			}
		}

		JSONObject cu = null;
		JSONObject img = needImage.peek();
		double cY = Double.MAX_VALUE;
		double cW = Double.MAX_VALUE;
		double cZ = Double.MAX_VALUE;
		double cH = Double.MAX_VALUE;
		if (cur != body.size()) {
			cu = body.getJSONObject(cur);
			cY = Double.valueOf(cu.getJSONObject("p").getString("y"));
			cW = Double.valueOf(cu.getJSONObject("p").getString("w"));
			cZ = Double.valueOf(cu.getJSONObject("p").getString("z"));
			cH = Double.valueOf(cu.getJSONObject("p").getString("h"));
		} else if (needImage.size() >= 0) {
			for (Iterator<JSONObject> i = needImage.iterator(); i.hasNext();) {
				img = i.next();
				p = document.createParagraph();
				insert(document, p, imagUrl, img, pageIndex);
				p = document.createParagraph();
			}
			return p;
		}
		if (img == null && cu != null) {
			XWPFRun run = p.createRun();
			run.setText(cu.getString("c"));
			return p;
		}
		double imgY = Double.valueOf(img.getJSONObject("p").getString("y"));
		double imgW = Double.valueOf(img.getJSONObject("p").getString("w"));
		double imgH = Double.valueOf(img.getJSONObject("p").getString("h"));
		double imgX = Double.valueOf(img.getJSONObject("p").getString("x"));
		double imgZ = Double.valueOf(img.getJSONObject("p").getString("z"));
		int size = needImage.size();

		if (pY + pH <= imgY && imgY <= cY + cH) {
			if(cY + cH - pY - pH >= imgH) {
				p = document.createParagraph();
				insert(document, p, imagUrl, img, pageIndex);
				needImage.poll();
			}
		} else if(pY + pH <= imgY && imgY <= cY) {
			if (cY - pY - pH >= imgH) {
				p = document.createParagraph();
				insert(document, p, imagUrl, img, pageIndex);
				needImage.poll();
			}
		}

		if (size != needImage.size()) {
			body.add(cur, img);
			p = document.createParagraph();
		} else {
			if (cu != null) {
				XWPFRun run = p.createRun();
				run.setText(cu.getString("c"));
			}
		}
		return p;
	}

	public void insert(XWPFDocument document, XWPFParagraph p, String imagUrl, JSONObject peek, int pageIndex) {
		try {
			File temp = new File(System.getProperty("user.dir") + "\\tmp");
			if (!temp.exists())
				temp.mkdir();
			File tempPic = new File(temp.getAbsoluteFile() + "\\" + pageIndex + ".png");
			downloadPic(imagUrl, tempPic);
			p = document.createParagraph();
			XWPFRun run = p.createRun();
			// ix,iy为目标图片的左上角位置，iw,ih为宽度
			// 图片尾号等于body下面的编号就插入图片
			// 如果不等于就插入到当前尾号-这个body里面所有pic的数量 - 1的位置
			// 如果这个转换出的位置大于当前body下面的所有个数，就直接插入
			JSONObject pngWH = peek.getJSONObject("c");// 获取pic的高和宽,595×842
			Double ix = Double.valueOf(pngWH.getString("ix"));
			Double iy = Double.valueOf(pngWH.getString("iy"));
			Double w = Double.valueOf(pngWH.getString("iw"));
			Double h = Double.valueOf(pngWH.getString("ih"));
			InputStream in = bufferedImageToInputStream(tempPic, ix.intValue(), iy.intValue(), w.intValue(),
					h.intValue());
			String pic = document.addPictureData(IOUtils.toByteArray(in),
					org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG);
			Double[] wh = getWH(w, h);
			FetchDocImpl.addPictureToRun(run, pic, org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG,
					wh[0].intValue(), wh[1].intValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public InputStream bufferedImageToInputStream(File file, int x, int y, int w, int h) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BufferedImage image = ImageIO.read(file);
		int height = image.getHeight();
		int width = image.getWidth();
		if (h > height)
			h = height;
		if (w > width)
			w = width;
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

	private void removeInNeedImage(JSONObject obj, LinkedList<JSONObject> needImage) {
		String target = obj.getJSONObject("s").getString("pic_file");
		for (Iterator<JSONObject> i = needImage.iterator(); i.hasNext();) {
			JSONObject next = i.next();
			String str = next.getJSONObject("s").getString("pic_file");
			if (target.equals(str)) {
				i.remove();
			}
		}
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

	private void sortImgs() {
		if (docModel.keySet().size() == 0) {
			log.info("文档有0页");
			return;
		}
		for (int i = 1; i <= docModel.keySet().size(); i++) {
			PageModel model = docModel.get(i);
			List<JSONObject> needImage = model.getNeedImage();
			sortImg(needImage);
		}
	}

	private void sortImg(List<JSONObject> needImage) {
		List<JSONObject> res = new LinkedList<>();
		for (java.util.Iterator<JSONObject> i = needImage.iterator(); i.hasNext();) {
			JSONObject next = i.next();
			if (res.isEmpty())
				res.add(next);
			JSONObject c = next.getJSONObject("c");
			int w = Integer.valueOf(c.getString("ix")) + Integer.valueOf(c.getString("iw"));
			int h = Integer.valueOf(c.getString("iy")) + Integer.valueOf(c.getString("ih"));
			for (int j = 0; j < res.size(); j++) {
				JSONObject resPre = null;
				JSONObject resNext = null;
				if (j == 0) {
					resNext = res.get(j);
					JSONObject resC = resNext.getJSONObject("c");
					int resW = Integer.valueOf(c.getString("ix"));
					int resH = Integer.valueOf(c.getString("iy"));
					if (resW >= w || resH >= h) {
						res.add(j, next);
						break;
					}
				} else if (j == res.size() - 1) {
					resNext = res.get(j);
					JSONObject resC = resNext.getJSONObject("c");
					int resW = Integer.valueOf(c.getString("ix"));
					int resH = Integer.valueOf(c.getString("iy"));
					if (resW >= w || resH >= h) {
						res.add(j, next);
					} else {
						res.add(next);
					}
					break;
				} else {
					resPre = res.get(j - 1);
					resNext = res.get(j);
					JSONObject resC = resNext.getJSONObject("c");
					int resW = Integer.valueOf(c.getString("ix"));
					int resH = Integer.valueOf(c.getString("iy"));
					boolean p = false;
					if (resW >= w || resH >= h) {
						p = true;
					}
					resC = resPre.getJSONObject("c");
					w = Integer.valueOf(c.getString("ix"));
					h = Integer.valueOf(c.getString("iy"));
					resW = Integer.valueOf(c.getString("ix")) + Integer.valueOf(c.getString("iw"));
					resH = Integer.valueOf(c.getString("iy")) + Integer.valueOf(c.getString("ih"));
					boolean n = false;
					if (!(resW >= w || resH >= h)) {
						n = true;
					}
					if (p && n) {
						res.add(j, next);
						break;
					}
				}

			}
		}
	}

	private void initNeed() {
		for (java.util.Iterator<Integer> i = docModel.keySet().iterator(); i.hasNext();) {
			try {
				int index = i.next();
				PageModel pageModel = docModel.get(index);
				String jsonUrl = pageModel.getJsonUrl();
				if (jsonUrl.equals(""))
					continue;
				String str = EntityUtils.toString(client.execute(new HttpGet(jsonUrl)).getEntity());
				Pattern p = Pattern.compile("wenku_" + index + "\\((.*)\\)");
				Matcher m = p.matcher(str);
				if (m.find())
					str = m.group(1);
				JSONObject page = JSON.parseObject(str);
				JSONArray body = page.getJSONArray("body");
				for (int j = 0; j < body.size(); j++) {
					JSONObject obj = body.getJSONObject(j);
					if (obj.getString("t").equals("pic") && obj.getString("s") != null) {
						String tmp = obj.getJSONObject("s").getString("pic_file");
						p = Pattern.compile("_([0-9]*)_");
						m = p.matcher(tmp);
						if (m.find())
							tmp = m.group(1);
						if (docModel.containsKey(Integer.valueOf(tmp)))
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
			JSONObject o = pngs.getJSONObject(i);
			String url = o.getString("pageLoadUrl");
			String index = o.getString("pageIndex");
			if (docModel.containsKey(Integer.valueOf(index))) {
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
			if (docModel.containsKey(Integer.valueOf(index))) {
				docModel.get(Integer.valueOf(index)).setJsonUrl(url);
				continue;
			}
			PageModel model = new PageModel();
			model.setJsonUrl(url);
			docModel.put(Integer.valueOf(index), model);
		}
	}

	private JSONObject getUrls(JSONObject json) {
		if (docInfoType == DocInfoType.docInfo2019) {
			return json.getJSONObject("readerInfo2019").getJSONObject("htmlUrls");
		}
		if (docInfoType == DocInfoType.docInfo) {
			this.pageH = Double
					.valueOf(json.getJSONObject("readerInfo").getJSONObject("pageInfo").getString("pageHeight"));
			this.pageW = Double
					.valueOf(json.getJSONObject("readerInfo").getJSONObject("pageInfo").getString("pageWidth"));
			return json.getJSONObject("readerInfo").getJSONObject("htmlUrls");
		}
		return null;
	}

	/**
	 * 因POI 3.8自带的BUG 导致添加进的图片不显示，只有一个图片框，将图片另存为发现里面的图片是一个PNG格式的透明图片 这里自定义添加图片的方法
	 * 往Run中插入图片(解决在word中不显示的问题)
	 * 
	 * @param run
	 * @param blipId 图片的id
	 * @param id     图片的类型
	 * @param width  图片的宽
	 * @param height 图片的高
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
