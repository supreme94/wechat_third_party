package com.github.supreme94.wechat.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.supreme94.wechat.service.JsApiService;


@RestController
public class JsApiResource {
	
    @Autowired
    private JsApiService jsApiService;
    
	@GetMapping("/jsapi_ticket")
    public String ticket(@RequestParam("appId")String appId) {
        return this.jsApiService.getJsapiTicket(appId);
    }
	
	@GetMapping("/jsapi_signature")
    public Object wxSign(@RequestParam("url") String url,@RequestParam("appId")String appId) {
        return this.jsApiService.createJsapiSignature(url,appId);
    }
}
