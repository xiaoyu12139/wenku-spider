package com.xiaoyu.model;

import java.util.LinkedList;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

@Data
public class PageModel {
	private String jsonUrl;
	private String imagUrl;
	private LinkedList<JSONObject> needImage = new LinkedList<>();
}
