package com.xiaoyu.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

/**
* 以为POI  3.8 自带的BUG，导致添加图片无法正确显示，这里重写POI添加图片的方法
* @author 十年饮冰，难凉热血 ！！！
*
*/
public class CustomXWPFDocument extends XWPFDocument {
	
	public static final String filePath = "F:\\demo.docx";
	public static final String imgPath = "F:\\demo.jpg";
	
	public static void main(String[] args) {
		CustomXWPFDocument document = new CustomXWPFDocument();
		try {
			String picId = document.addPictureData(new FileInputStream(imgPath), XWPFDocument.PICTURE_TYPE_PNG);
			document.createPicture(picId, document.getNextPicNameNumber(XWPFDocument.PICTURE_TYPE_PNG), 200, 150);
			
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			document.write(fos);
			fos.close();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

		public CustomXWPFDocument() {
			super();
		}
		
		public CustomXWPFDocument(OPCPackage opcPackage) throws IOException {
			super(opcPackage);
		}
		
	    public CustomXWPFDocument(InputStream in) throws IOException {
	        super(in);
	    }

	    public void createPicture(String blipId,int id, int width, int height) {
	        final int EMU = 9525;
	        width *= EMU;
	        height *= EMU;
	        //String blipId = getAllPictures().get(id).getPackageRelationship().getId();

	        
	        CTInline inline = createParagraph().createRun().getCTR().addNewDrawing().addNewInline();

	        String picXml = "" +
	                "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
	                "   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
	                "      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
	                "         <pic:nvPicPr>" +
	                "            <pic:cNvPr id=\"" + id + "\" name=\"Generated\"/>" +
	                "            <pic:cNvPicPr/>" +
	                "         </pic:nvPicPr>" +
	                "         <pic:blipFill>" +
	                "            <a:blip r:embed=\"" + blipId + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>" +
	                "            <a:stretch>" +
	                "               <a:fillRect/>" +
	                "            </a:stretch>" +
	                "         </pic:blipFill>" +
	                "         <pic:spPr>" +
	                "            <a:xfrm>" +
	                "               <a:off x=\"0\" y=\"0\"/>" +
	                "               <a:ext cx=\"" + width + "\" cy=\"" + height + "\"/>" +
	                "            </a:xfrm>" +
	                "            <a:prstGeom prst=\"rect\">" +
	                "               <a:avLst/>" +
	                "            </a:prstGeom>" +
	                "         </pic:spPr>" +
	                "      </pic:pic>" +
	                "   </a:graphicData>" +
	                "</a:graphic>";

	        //CTGraphicalObjectData graphicData = inline.addNewGraphic().addNewGraphicData();
	        XmlToken xmlToken = null;
	        try {
	            xmlToken = XmlToken.Factory.parse(picXml);
	        } catch(XmlException xe) {
	            xe.printStackTrace();
	        }
	        inline.set(xmlToken);
	        //graphicData.set(xmlToken);

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
	 
}
