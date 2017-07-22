package com.github.supreme94.wechat.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * wechat third party properties
 *
 */
@ConfigurationProperties(prefix = "wechat.third")
@Data
@Component
public class WechatProperties {
    /**
     * 设置微信第三方平台的appid
     */
    private String appId;

    /**
     * 设置微信第三方平台的app secret
     */
    private String secret;

    /**
     * 设置微信第三方平台的token
     */
    private String token;

    /**
     * 设置微信第三方平台的EncodingAESKey
     */
    private String aesKey;

}
