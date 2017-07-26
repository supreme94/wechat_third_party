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

	public static final String AUTHORIZER_ACCESS_TOKEN_PREFIX = "authorizer_access_token:";
	public static final String AUTHORIZER_REFRESH_TOKEN_PREFIX = "authorizer_refresh_token:";

	private static final String GET_COMPONENT_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/component/api_component_token";

	private static final String GET_PRE_AUTH_CODE_URL = "https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode";

	private static final String GET_AUTHORIZER_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/component/api_query_auth";

	private static final String REFRESH_AUTHORIZER_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/component/api_authorizer_token";

	@Autowired
	private WechatProperties wechatProperties;

	@Autowired
	@Qualifier("customRedisTemplate")
	private RedisTemplate<String, String>  redisTemplate;
	
	@Autowired
	private HttpClient httpClient;
	
	/**
	 * 获取第三方平台component_access_token
	 * @return
	 */
	public String getComponentAccessToken() {
		return getComponentAccessToken(false);
	}

	/**
	 * 获取第三方平台component_access_token
	 * @param forceRefresh 是否强制刷新
	 * @return
	 */
	public String getComponentAccessToken(boolean forceRefresh) {
		if(forceRefresh) {
			return refreshComponentAccessToken();
		}

		if(redisTemplate.getExpire("component_access_token") < 60) {
			return refreshComponentAccessToken();
		}

		return redisTemplate.opsForValue().get("component_access_token");
	}

	/**
	 * 刷新compoment_access_token
	 * @return
	 */
	public String refreshComponentAccessToken() {
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		params.put("component_appsecret", wechatProperties.getSecret());
		params.put("component_verify_ticket", redisTemplate.opsForValue().get("component_verify_ticket"));
		String body = httpClient.post(JsonUtil.objectToString(params), GET_COMPONENT_TOKEN_URL);
		WxComponentAccessToken wxComponentAccessToken = JsonUtil.stringToObject(body, WxComponentAccessToken.class);
		redisTemplate.opsForValue().set("component_access_token", wxComponentAccessToken.getComponentAccessToken(),wxComponentAccessToken.getExpiresIn(),TimeUnit.SECONDS);
		return wxComponentAccessToken.getComponentAccessToken();
	}

	/**
	 * 获取预授权码。预授权码用于公众号或小程序授权时的第三方平台方安全验证。此code只用于页面授权
	 * @return 预授权码
	 */
	public String getpreAuthCode() {
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		String result = httpClient.postForComponent(JsonUtil.objectToString(params), GET_PRE_AUTH_CODE_URL);
		JsonNode node = JsonUtil.stringToNode(result);
		redisTemplate.opsForValue().set("pre_auth_code", node.get("pre_auth_code").asText(), node.get("expires_in").asInt(),TimeUnit.SECONDS);
		return node.get("pre_auth_code").asText();
	}

	/**
	 * 使用授权码换取授权公众号或小程序的授权信息，并换取authorizer_access_token和authorizer_refresh_token
	 * @param authorization_code 授权码
	 * @return
	 */
	public String codeExChangeAutuorizerToken(String authorization_code) {
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		params.put("authorization_code", authorization_code);
		String result = httpClient.postForComponent(JsonUtil.objectToString(params), GET_AUTHORIZER_TOKEN_URL);
		JsonNode node = JsonUtil.stringToNode(result);
		JsonNode authorization_info = node.path("authorization_info");
		String authorizer_access_token = authorization_info.path("authorizer_access_token").textValue();
		String authorizer_refresh_token = authorization_info.path("authorizer_refresh_token").textValue();
		String authorizer_appid = authorization_info.path("authorizer_appid").textValue();
		long expires_in = authorization_info.path("expires_in").longValue();
		redisTemplate.opsForValue().set(AUTHORIZER_ACCESS_TOKEN_PREFIX + authorizer_appid,authorizer_access_token,expires_in,TimeUnit.SECONDS);
		redisTemplate.opsForValue().set(AUTHORIZER_REFRESH_TOKEN_PREFIX + authorizer_appid,authorizer_refresh_token);
		return authorizer_access_token;
	}
	
	/**
	 * 用authorizer_appid获取授权方令牌
	 * @param authorizer_appid 授权方的APPID
	 * @return
	 */
	public String getAuthorizerAccessToken(String authorizer_appid) {
		return getAuthorizerAccessToken(authorizer_appid, false);
	}
	
	/**
	 * 用authorizer_appid获取授权方令牌
	 * @param authorizer_appid 授权方的APPID
	 * @param forceRefresh 是否强制刷新
	 * @return
	 */
	
	public String getAuthorizerAccessToken(String authorizer_appid,boolean forceRefresh) {
		if(forceRefresh) {
			return refreshAuthorizerAccessToken(authorizer_appid);
		}

		if(redisTemplate.getExpire(AUTHORIZER_ACCESS_TOKEN_PREFIX + authorizer_appid) < 60) {
			return refreshAuthorizerAccessToken(authorizer_appid);
		}

		return redisTemplate.opsForValue().get(AUTHORIZER_ACCESS_TOKEN_PREFIX + authorizer_appid);
	}
	
	/**
	 * 用于刷新授权方令牌（authorizer_access_token）失效时，可用刷新令牌（authorizer_refresh_token）获取新的令牌。请注意，此处token是2小时刷新一次，开发者需要自行进行token的缓存，避免token的获取次数达到每日的限定额度
	 * @param authorizer_appid 授权方的APPID
	 * @return
	 */
	public String refreshAuthorizerAccessToken(String authorizer_appid) {
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		params.put("authorizer_appid", authorizer_appid);
		params.put("authorizer_refresh_token", redisTemplate.opsForValue().get(AUTHORIZER_REFRESH_TOKEN_PREFIX + authorizer_appid));
		String result = httpClient.postForComponent(JsonUtil.objectToString(params), REFRESH_AUTHORIZER_TOKEN_URL);
		System.out.println(result);
		JsonNode node = JsonUtil.stringToNode(result);
		String authorizer_access_token = node.path("authorizer_access_token").textValue();
		redisTemplate.opsForValue().set(AUTHORIZER_ACCESS_TOKEN_PREFIX + authorizer_appid,authorizer_access_token,node.path("expires_in").longValue(),TimeUnit.SECONDS);
		return authorizer_access_token;
	}

}
