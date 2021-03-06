package com.swk.wechat.third.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swk.wechat.third.core.config.WechatProperties;
import com.swk.wechat.third.util.crypto.SHA1;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidatorService {
	
	@Autowired
	private WechatProperties wechatProperties;
	
	public boolean checkSignature(String timestamp, String nonce, String signature) {
		try {
			return SHA1.gen(this.wechatProperties.getToken(), timestamp, nonce).equals(signature);
		} catch (Exception e) {
			log.error("Checking signature failed, and the reason is :" + e.getMessage());
			return false;
		}
	}
}
