package com.xiaoyu.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

@Data
public class PageModel {
	private String jsonUrl;
	private String imagUrl;
	private List<JSONObject> needImage = new ArrayList<>();
}
