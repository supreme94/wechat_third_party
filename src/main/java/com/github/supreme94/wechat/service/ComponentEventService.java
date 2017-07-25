package com.github.supreme94.wechat.service;

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
import com.github.supreme94.wechat.core.config.WechatProperties;
import com.github.supreme94.wechat.core.entities.AuthorizerInfo;
import com.github.supreme94.wechat.core.entities.BusinessInfo;
import com.github.supreme94.wechat.core.entities.FuncInfo;
import com.github.supreme94.wechat.core.repository.AuthorizerInfoRepository;
import com.github.supreme94.wechat.pojo.WxTicketXmlMessage;
import com.github.supreme94.wechat.util.HttpClient;
import com.github.supreme94.wechat.util.JsonUtil;


@Service
public class ComponentEventService {

	private static final String COMPONENT_VERIFY_TICKET = "component_verify_ticket";

	private static final String UNAUTHORIZED = "unauthorized";

	private static final String AUTHORIZED = "authorized";

	private static final String UPDATEAUTHORIZED = "updateauthorized";
	
	private static final String GET_AUTHORIZER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/component/api_get_authorizer_info?component_access_token={1}";
	
	@Autowired
	@Qualifier("customRedisTemplate")
	private RedisTemplate<String, String>  redisTemplate;
	
	@Autowired
	private WechatProperties wechatProperties;
	
	@Autowired
	private WeChatService weChatService;
	
	@Autowired
	private AuthorizerInfoRepository authorizerInfoRepository;

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
				updateauthorized();
				break;
	
			default:
				break;
		}
	}

	private void updateauthorized() {
		// TODO Auto-generated method stub
		
	}

	private void authorized(WxTicketXmlMessage wxTicketXmlMessage) {
		System.out.println(wechatProperties.getAppId());
		Map<String, String> params = new HashMap<>();
		params.put("component_appid", wechatProperties.getAppId());
		params.put("authorizer_appid", wxTicketXmlMessage.getAuthorizerAppid());
		String cpmponent_access_token = this.weChatService.getComponentToken().getComponentAccessToken();
		String result = HttpClient.postJsonRequest(JsonUtil.objectToString(params), GET_AUTHORIZER_INFO_URL,cpmponent_access_token).getBody();
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
