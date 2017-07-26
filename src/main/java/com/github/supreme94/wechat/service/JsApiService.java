package com.github.supreme94.wechat.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.supreme94.wechat.pojo.WxJsapiSignature;
import com.github.supreme94.wechat.util.HttpClient;
import com.github.supreme94.wechat.util.JsonUtil;
import com.github.supreme94.wechat.util.RandomUtils;
import com.github.supreme94.wechat.util.crypto.SHA1;


@Service
public class JsApiService {
	/**
	 * 获得jsapi_ticket
	 */
	private static final String GET_JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?type=jsapi";

	public static final String JSAPI_TICKET_PREFIX = "jsapi_ticket:";

	@Autowired
	private HttpClient httpClient;

	@Autowired
	@Qualifier("customRedisTemplate")
	private RedisTemplate<String, String>  redisTemplate;

	public String getJsapiTicket(String authorizer_appid) {
		return getJsapiTicket(authorizer_appid,false);
	}

	public String getJsapiTicket(String authorizer_appid, boolean forceRefresh) {
		if (forceRefresh) {
			return refreshJsapiTicket(authorizer_appid);
		}

		if(redisTemplate.getExpire(JSAPI_TICKET_PREFIX + authorizer_appid) < 60) {
			return refreshJsapiTicket(authorizer_appid);
		}

		return redisTemplate.opsForValue().get(JSAPI_TICKET_PREFIX + authorizer_appid);

	}

	public String refreshJsapiTicket(String authorizer_appid) {
		String result = httpClient.getForWechatMP(GET_JSAPI_TICKET_URL,authorizer_appid);
		JsonNode ticket = JsonUtil.stringToNode(result);
		String jsapiTicket = ticket.path("ticket").textValue();
		int expiresInSeconds = ticket.path("expires_in").intValue();
		redisTemplate.opsForValue().set(JSAPI_TICKET_PREFIX + authorizer_appid,jsapiTicket,expiresInSeconds,TimeUnit.SECONDS);
		return jsapiTicket;
	}

	public WxJsapiSignature createJsapiSignature(String url,String authorizer_appid) {
		long timestamp = System.currentTimeMillis() / 1000;
		String noncestr = RandomUtils.getRandomStr();
		String jsapiTicket = getJsapiTicket(authorizer_appid);
		String signature = SHA1.genWithAmple("jsapi_ticket=" + jsapiTicket,"noncestr=" + noncestr, "timestamp=" + timestamp, "url=" + url);
		WxJsapiSignature jsapiSignature = new WxJsapiSignature();
		jsapiSignature.setAppId(authorizer_appid);
		jsapiSignature.setTimestamp(timestamp);
		jsapiSignature.setNonceStr(noncestr);
		jsapiSignature.setUrl(url);
		jsapiSignature.setSignature(signature);
		return jsapiSignature;
	}
}
