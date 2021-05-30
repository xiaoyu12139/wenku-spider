package com.xiaoyu.model;

import lombok.Data;

@Data
public class DownloadModel {
	
	private static DownloadModel downloadModel = new DownloadModel();
	
	private String url;
	private String title;
	private String downProgress;
	private String type;
	private String createTime;
	private String tags;

	private void DownloadModel() {

	}
	
	public static DownloadModel getInstance() {
		return downloadModel;
	}
}
