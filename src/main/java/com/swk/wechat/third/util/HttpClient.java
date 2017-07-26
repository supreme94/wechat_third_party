package com.swk.wechat.third.util;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.swk.wechat.third.service.WeChatService;

@Component
public class HttpClient {

	private static final StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8")); //解决请求返回的中文乱码问题 
	private static final RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build(); 

	@Autowired
	private WeChatService weChatService;
	
	/**
	 * 发送普通get请求
	 * @param url
	 * @return
	 */
	public String get(String url) {
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		return response.getBody();
	}
	
	/**
	 * 用于发送get请求到微信公众平台接口
	 * @param url
	 * @param authorizer_appid 授权方的appid
	 * @return
	 */
	public String getForWechatMP(String url, String authorizer_appid) {
		ResponseEntity<String> response = restTemplate.getForEntity(urlWithAccessToken(url,authorizer_appid), String.class);
		return response.getBody();
	}
	
	/**
	 * 发送普通post请求
	 * @param jsonString
	 * @param url
	 * @return
	 */
	public String post(String jsonString, String url) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<String> httpEntity = new HttpEntity<String>(jsonString, headers);
		return restTemplate.postForEntity(url, httpEntity, String.class).getBody();
	}
	
	/**
	 * 用于发送post请求到第三方平台接口
	 * @param jsonString
	 * @param url
	 * @return
	 */
	public String postForComponent(String jsonString, String url) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<String> httpEntity = new HttpEntity<String>(jsonString, headers);
		return restTemplate.postForEntity(urlWithComponentAccessToken(url), httpEntity, String.class).getBody();
	}
	
	/**
	 * 用于发送post请求到微信公众平台接口
	 * @param jsonString
	 * @param url
	 * @param authorizer_appid 授权方的appid
	 * @return
	 */
	public String postForWechatMP(String jsonString, String url, String authorizer_appid) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<String> httpEntity = new HttpEntity<String>(jsonString, headers);
		return restTemplate.postForEntity(urlWithAccessToken(url,authorizer_appid), httpEntity, String.class).getBody();
	}
	
	/**
	 * 为请求url拼接上access_token参数
	 * @param url
	 * @return
	 */
	private String urlWithAccessToken(String url, String authorizer_appid) {
		if (url.contains("access_token=")) {
			throw new IllegalArgumentException("url参数中不允许有access_token: " + url);
		}
		String componentAccessToken = weChatService.getAuthorizerAccessToken(authorizer_appid);

		String urlWithAccessToken = url + (url.contains("?") ? "&" : "?") + "access_token=" + componentAccessToken;
		return urlWithAccessToken;
	}
	
	/**
	 * 为请求url拼接上component_access_token参数
	 * @param url
	 * @return
	 */
	private String urlWithComponentAccessToken(String url) {
		if (url.contains("component_access_token=")) {
			throw new IllegalArgumentException("url参数中不允许有component_access_token: " + url);
		}
		String componentAccessToken = weChatService.getComponentAccessToken();

		String urlWithComponentAccessToken = url + (url.contains("?") ? "&" : "?") + "component_access_token=" + componentAccessToken;
		return urlWithComponentAccessToken;
	}

}
