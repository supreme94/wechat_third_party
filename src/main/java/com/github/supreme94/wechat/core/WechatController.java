package com.github.supreme94.wechat.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.supreme94.wechat.core.config.WechatProperties;
import com.github.supreme94.wechat.pojo.WxMpXmlMessage;
import com.github.supreme94.wechat.service.ValidatorService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class WechatController {
	
	@Autowired
	private ValidatorService validatorService;
	
	@Autowired
	private WechatProperties wechatProperties;
	
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
        
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            System.out.println(inMessage);
        } else if ("aes".equals(encType)) {
            // aes加密的消息
            Object inMessage = WxMpXmlMessage.fromEncryptedXml(
                    requestBody, this.wechatProperties, timestamp,
                    nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
        }
        
		return "success";
	}
}
