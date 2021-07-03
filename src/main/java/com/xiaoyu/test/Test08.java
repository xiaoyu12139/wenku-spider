package com.xiaoyu.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

public class Test08 {
	public static void main(String[] args) {
		try {
			Image image =Image.getInstance("C:\\Users\\xiaoyu\\Desktop\\wenku-spider\\tmp\\0.png");
			image.setAlignment(Image.ALIGN_CENTER);
			Document document =new Document(new Rectangle(image.getWidth(), image.getHeight())); 
//			document.setPageSize();
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("C:\\Users\\xiaoyu\\Desktop\\wenku-spider\\downloads\\1.pdf"));
			document.open();
//			image.scalePercent(40);//“¿’’±»¿˝Àı∑≈
			document.add(image);
			document.add(image);
			document.add(image);
			document.add(image);
			document.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
