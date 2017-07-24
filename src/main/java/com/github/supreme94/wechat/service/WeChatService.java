package com.github.supreme94.wechat.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.supreme94.wechat.core.config.WechatProperties;
import com.github.supreme94.wechat.pojo.WxComponentAccessToken;
import com.github.supreme94.wechat.util.HttpClient;
import com.github.supreme94.wechat.util.JsonUtil;

@Service
public class WeChatService {
	
	private static final String GET_COMPONENT_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/component/api_component_token";
	
	private static final String GET_PRE_AUTH_CODE_URL = "https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?component_access_token={1}";
	
	private static final String GET_AUTHORIZER_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token={1}";
	
	@Autowired
	private WechatProperties wechatProperties;
	
	@Autowired
	@Qualifier("customRedisTemplate")
	private RedisTemplate<String, String>  redisTemplate;
	
	public WxComponentAccessToken getComponentToken() {
		WxComponentAccessToken wxComponentAccessToken = null;
		System.out.println("component_access_token " + redisTemplate.opsForValue().get("component_access_token"));
		if(null != redisTemplate.opsForValue().get("component_access_token")) {
			wxComponentAccessToken = new WxComponentAccessToken(redisTemplate.opsForValue().get("component_access_token").toString(),redisTemplate.getExpire("component_access_token",TimeUnit.SECONDS));
		}else {
			Map<String, String> params = new HashMap<>();
			params.put("component_appid", wechatProperties.getAppId());
			params.put("component_appsecret", wechatProperties.getSecret());
			params.put("component_verify_ticket", redisTemplate.opsForValue().get("component_verify_ticket"));
			String body = HttpClient.postJsonRequest(JsonUtil.objectToString(params), GET_COMPONENT_TOKEN_URL).getBody();
			System.out.println("getComponentToken body " + body);
			wxComponentAccessToken = JsonUtil.stringToObject(body, WxComponentAccessToken.class);
			redisTemplate.opsForValue().set("component_access_token", wxComponentAccessToken.getComponentAccessToken(),7140,TimeUnit.SECONDS);
		}
		
		return wxComponentAccessToken;
	}
	
	public String getpreAuthCode() {
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		String cpmponent_access_token = redisTemplate.opsForValue().get("component_access_token");
		String result = HttpClient.postJsonRequest(JsonUtil.objectToString(params), GET_PRE_AUTH_CODE_URL,cpmponent_access_token).getBody();
		JsonNode node = JsonUtil.stringToNode(result);
		redisTemplate.opsForValue().set("pre_auth_code", node.get("pre_auth_code").asText(), node.get("expires_in").asInt(),TimeUnit.SECONDS);
		return node.get("pre_auth_code").asText();
	}
	
	public String getAutuorizerToken(String authorization_code) {
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		params.put("authorization_code", authorization_code);
		String cpmponent_access_token = redisTemplate.opsForValue().get("component_access_token");
		String result = HttpClient.postJsonRequest(JsonUtil.objectToString(params), GET_AUTHORIZER_TOKEN_URL,cpmponent_access_token).getBody();
		System.out.println(result);
		JsonNode node = JsonUtil.stringToNode(result);
		System.out.println("node " + node);
		redisTemplate.opsForValue().set("authorizer_access_token", node.get("authorization_info").get("authorizer_access_token").asText(), node.get("authorization_info").get("expires_in").asInt(),TimeUnit.SECONDS);
		redisTemplate.opsForValue().set("authorizer_refresh_token", node.get("authorization_info").get("authorizer_refresh_token").asText(), node.get("authorization_info").get("expires_in").asInt(),TimeUnit.SECONDS);
		redisTemplate.opsForValue().set("authorizer_appid", node.get("authorization_info").get("authorizer_appid").asText(), node.get("authorization_info").get("expires_in").asInt(),TimeUnit.SECONDS);
		return node.get("authorization_info").get("authorizer_access_token").asText();
	}
}
