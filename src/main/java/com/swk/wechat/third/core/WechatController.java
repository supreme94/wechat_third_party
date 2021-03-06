package com.swk.wechat.third.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.swk.wechat.third.core.config.WechatProperties;
import com.swk.wechat.third.core.entities.AuthorizerInfo;
import com.swk.wechat.third.core.repository.AuthorizerInfoRepository;
import com.swk.wechat.third.pojo.WxTicketXmlMessage;
import com.swk.wechat.third.service.ComponentEventService;
import com.swk.wechat.third.service.ValidatorService;
import com.swk.wechat.third.service.WeChatService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class WechatController {
	
	@Autowired
	private ValidatorService validatorService;
	
	@Autowired
	private WechatProperties wechatProperties;
	
	@Autowired
	private WeChatService wechatService;
	
	@Autowired
	private ComponentEventService componentEventService;
	
	@Autowired
	private AuthorizerInfoRepository authorizerInfoRepository;
	
	@ResponseBody
	@PostMapping("/ticket")
	public String recevieEventFromWechat(@RequestBody String requestBody,
										@RequestParam("signature") String signature,
										@RequestParam("timestamp") String timestamp,
										@RequestParam("nonce") String nonce,
										@RequestParam(name = "encrypt_type",required = false) String encType,
										@RequestParam(name = "msg_signature",required = false) String msgSignature) {
		
        log.info("\n接收微信请求：[signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                signature, encType, msgSignature, timestamp, nonce, requestBody);
		
        if (!this.validatorService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }
        
    	WxTicketXmlMessage inMessage = WxTicketXmlMessage.fromEncryptedXml(requestBody, this.wechatProperties, timestamp, nonce, msgSignature);
    	componentEventService.handle(inMessage);
        log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
        
		return "success";
	}
	
	@ResponseBody
	@GetMapping("/component_access_token")
	public String getComponentToken() {
		return wechatService.getComponentAccessToken();
	}
	
//	@ResponseBody
//	@DeleteMapping("/{appid}")
//	public void deleteAppid(@PathVariable("appid") String appid) {
//		 authorizerInfoRepository.deleteByauthorizerAppid(appid);
//	}
	
	@ResponseBody
	@GetMapping("/pre_auth_code")
	public String getPreAuthCode() {
		return wechatService.getpreAuthCode();
	}
	
	@ResponseBody
	@GetMapping("/one")
	public AuthorizerInfo findOne() {
		return authorizerInfoRepository.findOne(1L);
	}
	
	@GetMapping("/")
	public String authCodeChangeToken(@RequestParam("auth_code")String auth_code,
									  @RequestParam("expires_in")Integer expires_in) {
		wechatService.codeExChangeAutuorizerToken(auth_code);
		return "redirect:/success.html";
	}
}
