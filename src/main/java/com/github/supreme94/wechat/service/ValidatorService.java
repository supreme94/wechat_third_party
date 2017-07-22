package com.github.supreme94.wechat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.supreme94.wechat.core.config.WechatProperties;
import com.github.supreme94.wechat.util.crypto.SHA1;

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
