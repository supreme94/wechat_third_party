package com.swk.wechat.third.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.swk.wechat.third.core.config.WechatProperties;
import com.swk.wechat.third.core.entities.AuthorizerInfo;
import com.swk.wechat.third.core.entities.BusinessInfo;
import com.swk.wechat.third.core.entities.FuncInfo;
import com.swk.wechat.third.core.repository.AuthorizerInfoRepository;
import com.swk.wechat.third.core.repository.FuncInfoRepository;
import com.swk.wechat.third.pojo.WxTicketXmlMessage;
import com.swk.wechat.third.util.HttpClient;
import com.swk.wechat.third.util.JsonUtil;

/**
 * 用于处理微信第三方平台推送到"授权事件接收URL"的component_verify_ticket协议，取消授权通知，授权成功通知，授权更新通知的授权事件
 * @author liangpeng
 *
 */
@Service
public class ComponentEventService {

	private static final String COMPONENT_VERIFY_TICKET = "component_verify_ticket";

	private static final String UNAUTHORIZED = "unauthorized";

	private static final String AUTHORIZED = "authorized";

	private static final String UPDATEAUTHORIZED = "updateauthorized";
	
	private static final String GET_AUTHORIZER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/component/api_get_authorizer_info";
	
	@Autowired
	@Qualifier("customRedisTemplate")
	private RedisTemplate<String, String>  redisTemplate;
	
	@Autowired
	private WechatProperties wechatProperties;
	
	@Autowired
	private AuthorizerInfoRepository authorizerInfoRepository;
	
	@Autowired
	private FuncInfoRepository funcInfoRepository;
	
	@Autowired
	private HttpClient httpClient;

	public void handle(WxTicketXmlMessage wxTicketXmlMessage) {
		switch (wxTicketXmlMessage.getInfoType()) {
		
			case COMPONENT_VERIFY_TICKET:
				component_verify_ticket(wxTicketXmlMessage.getComponentVerifyTicket());
				break;
			case UNAUTHORIZED:
				unauthorized(wxTicketXmlMessage);
				break;
			case AUTHORIZED:
				authorized(wxTicketXmlMessage);
				break;
			case UPDATEAUTHORIZED:
				updateauthorized(wxTicketXmlMessage);
				break;
	
			default:
				break;
		}
	}

	private void updateauthorized(WxTicketXmlMessage wxTicketXmlMessage) {
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		params.put("authorizer_appid", wxTicketXmlMessage.getAuthorizerAppid());
		String result = httpClient.postForComponent(JsonUtil.objectToString(params), GET_AUTHORIZER_INFO_URL);
		System.out.println(result);
		JsonNode node = JsonUtil.stringToNode(result);
		AuthorizerInfo authorizationInfo = authorizerInfoRepository.findOneByauthorizerAppid(wxTicketXmlMessage.getAuthorizerAppid());
		if(!node.hasNonNull("errcode")) {
			JsonNode authorization_info = node.get("authorization_info");
			
			//先删除授权方所拥有的权限列表
			funcInfoRepository.deleteInBatch(authorizationInfo.getFuncInfo());
			
			Set<FuncInfo> funcInfos = new HashSet<>();
			if(authorization_info.get("func_info").isArray()) {
				for(int i=0;i<authorization_info.get("func_info").size();i++) {
					JsonNode temp = authorization_info.get("func_info").get(i);
					FuncInfo funcInfo = new FuncInfo();
					funcInfo.setFuncId(temp.get("funcscope_category").get("id").asInt());
					funcInfos.add(funcInfo);
				}
			}
			authorizationInfo.setFuncInfo(funcInfos);
			authorizerInfoRepository.save(authorizationInfo);
		}
	}

	private void authorized(WxTicketXmlMessage wxTicketXmlMessage) {
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		params.put("authorizer_appid", wxTicketXmlMessage.getAuthorizerAppid());
		String result = httpClient.postForComponent(JsonUtil.objectToString(params), GET_AUTHORIZER_INFO_URL);
		System.out.println(result);
		JsonNode node = JsonUtil.stringToNode(result);
		if(!node.hasNonNull("errcode")) {
			JsonNode authorizer_info = node.get("authorizer_info");
			JsonNode authorization_info = node.get("authorization_info");
			AuthorizerInfo ai = new AuthorizerInfo();
			ai.setAuthorizerAppid(authorization_info.get("authorizer_appid").asText());
			ai.setAuthorizerRefreshToken(authorization_info.get("authorizer_refresh_token").asText());
			
			ai.setAlias(authorizer_info.get("alias").asText());
			ai.setNickName(authorizer_info.get("nick_name").asText());
			ai.setHeadImg(authorizer_info.get("head_img").asText());
			ai.setQrcodeUrl(authorizer_info.get("qrcode_url").asText());
			ai.setIdc(authorizer_info.get("idc").asInt());
			ai.setPrincipalName(authorizer_info.get("principal_name").asText());
			ai.setServiceTypeInfo(authorizer_info.get("service_type_info").get("id").asInt());
			ai.setVerifyTypeInfo(authorizer_info.get("verify_type_info").get("id").asInt());
			ai.setSignature(authorizer_info.get("signature").asText());
			ai.setUserName(authorizer_info.get("user_name").asText());
			
			BusinessInfo businessInfo = new BusinessInfo();
			businessInfo.setOpenCard(authorizer_info.get("business_info").get("open_card").asInt());
			businessInfo.setOpenPay(authorizer_info.get("business_info").get("open_pay").asInt());
			businessInfo.setOpenScan(authorizer_info.get("business_info").get("open_scan").asInt());
			businessInfo.setOpenShake(authorizer_info.get("business_info").get("open_shake").asInt());
			businessInfo.setOpenStore(authorizer_info.get("business_info").get("open_store").asInt());
			ai.setBusinessInfo(businessInfo);
			
			Set<FuncInfo> funcInfos = new HashSet<>();
			if(authorization_info.get("func_info").isArray()) {
				for(int i=0;i<authorization_info.get("func_info").size();i++) {
					JsonNode temp = authorization_info.get("func_info").get(i);
					FuncInfo funcInfo = new FuncInfo();
					funcInfo.setFuncId(temp.get("funcscope_category").get("id").asInt());
					funcInfos.add(funcInfo);
				}
			}
			ai.setFuncInfo(funcInfos);
			authorizerInfoRepository.save(ai);
		}
		
	}
	
	private void unauthorized(WxTicketXmlMessage wxTicketXmlMessage) {
		authorizerInfoRepository.deleteByauthorizerAppid(wxTicketXmlMessage.getAuthorizerAppid());
	}

	private void component_verify_ticket(String componentVerifyTicket) {
		redisTemplate.opsForValue().set("component_verify_ticket", componentVerifyTicket, 10, TimeUnit.MINUTES);
	}
}
