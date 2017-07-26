package com.github.supreme94.wechat.pojo;

import java.io.Serializable;

import lombok.Data;

/**
 * jspai signature
 */
@Data
public class WxJsapiSignature implements Serializable {
  private static final long serialVersionUID = -1116808193154384804L;

  private String appId;

  private String nonceStr;

  private long timestamp;

  private String url;

  private String signature;

}
