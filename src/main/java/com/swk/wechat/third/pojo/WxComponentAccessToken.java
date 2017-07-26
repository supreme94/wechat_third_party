package com.swk.wechat.third.pojo;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WxComponentAccessToken implements Serializable {
  private static final long serialVersionUID = 8709719312922168909L;

  private String componentAccessToken;

  private long expiresIn = -1;

//  public static WxComponentAccessToken fromJson(String json) {
//    return WxGsonBuilder.create().fromJson(json, WxComponentAccessToken.class);
//  }

}
