package com.github.supreme94.wechat.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

public class JsonUtil {
	private static ObjectMapper mapper = new ObjectMapper();

	public static Map<String, Object> objectToMap(Object Obj) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			map = mapper.readValue(Obj.toString(), new TypeReference<Map<String, Object>>() {
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static JsonNode objectToNode(Object obj) {
		// ObjectMapper mapper = new ObjectMapper();
		//    JsonNode node = null;
		// String json = mapper.writeValueAsString(obj);
		// node = mapper.readTree(json);
		JsonNode node = mapper.valueToTree(obj);
		return node;
	}

	public static JsonNode stringToNode(String jsonString) {
		JsonNode node = null;
		try {
			node = mapper.readTree(jsonString);
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return node;
	}

	public static String objectToString(Object obj) {
		String jsonStr = null;
		try {
			jsonStr = mapper.writeValueAsString(obj);
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}

	public static <T> T stringToObject(String json,Class<T> clazz) {
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		T t = null;
		try {
			t = mapper.readValue(json, clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return t;
	}

}
