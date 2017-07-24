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
 * 微信推送过来的消息，xml格式
 * 部分未注释的字段的解释请查阅相关微信开发文档：
 * <a href="http://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140453&token=&lang=zh_CN">接收普通消息</a>
 * <a href="http://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140454&token=&lang=zh_CN">接收事件推送</a>
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


	public static WxTicketXmlMessage fromXml(String xml) {
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
