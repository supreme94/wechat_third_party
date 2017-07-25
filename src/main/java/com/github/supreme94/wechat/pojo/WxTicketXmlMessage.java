package com.github.supreme94.wechat.pojo;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;

import com.github.supreme94.wechat.core.config.WechatProperties;
import com.github.supreme94.wechat.util.crypto.WxMpCryptUtil;
import com.github.supreme94.wechat.util.xml.XStreamCDataConverter;
import com.github.supreme94.wechat.util.xml.XStreamTransformer;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import lombok.Data;

/**
 * <pre>
 * 微信第三方平台推送过来的授权事件，xml格式
 * </pre>
 *
 * @author chanjarster
 */
@XStreamAlias("xml")
@Data
public class WxTicketXmlMessage implements Serializable {

	private static final long serialVersionUID = 4441807402955450915L;

	@XStreamAlias("CreateTime")
	private Long createTime;

	@XStreamAlias("AppId")
	@XStreamConverter(value = XStreamCDataConverter.class)
	private String appId;

	@XStreamAlias("InfoType")
	@XStreamConverter(value = XStreamCDataConverter.class)
	private String infoType;

	@XStreamAlias("ComponentVerifyTicket")
	@XStreamConverter(value = XStreamCDataConverter.class)
	private String componentVerifyTicket;
	
	@XStreamAlias("AuthorizerAppid")
	@XStreamConverter(value = XStreamCDataConverter.class)
	private String authorizerAppid;
	
	@XStreamAlias("AuthorizationCode")
	@XStreamConverter(value = XStreamCDataConverter.class)
	private String authorizationCode;
	
	@XStreamAlias("AuthorizationCodeExpiredTime")
	private Long authorizationCodeExpiredTime;


	public static WxTicketXmlMessage fromXml(String xml) {
		System.out.println(xml);
		WxTicketXmlMessage wxMpXmlMessage = XStreamTransformer.fromXml(WxTicketXmlMessage.class, xml);
		return wxMpXmlMessage;
	}

	public static WxTicketXmlMessage fromXml(InputStream is) {
		return XStreamTransformer.fromXml(WxTicketXmlMessage.class, is);
	}

	/**
	 * 从加密字符串转换
	 *
	 * @param encryptedXml      密文
	 * @param wechatProperties 配置存储器对象
	 * @param timestamp         时间戳
	 * @param nonce             随机串
	 * @param msgSignature      签名串
	 */
	public static WxTicketXmlMessage fromEncryptedXml(String encryptedXml,
			WechatProperties wechatProperties, String timestamp, String nonce,
			String msgSignature) {
		WxMpCryptUtil cryptUtil = new WxMpCryptUtil(wechatProperties);
		String plainText = cryptUtil.decrypt(msgSignature, timestamp, nonce,
				encryptedXml);
		return fromXml(plainText);
	}

	public static WxTicketXmlMessage fromEncryptedXml(InputStream is,
			WechatProperties wechatProperties, String timestamp, String nonce,
			String msgSignature) {
		try {
			return fromEncryptedXml(IOUtils.toString(is, "UTF-8"), wechatProperties,
					timestamp, nonce, msgSignature);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
