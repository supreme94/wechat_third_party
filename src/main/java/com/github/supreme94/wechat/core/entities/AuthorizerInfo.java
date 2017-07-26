package com.github.supreme94.wechat.core.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "authorizer_info")
@Getter
@Setter
@ToString
public class AuthorizerInfo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/** 微信公众号的APPID */
	@Column(name = "authorizer_appid")
	private String authorizerAppid;
	
	/** 授权方的刷新令牌 */
	@Column(name = "authorizer_refresh_token")
	private String authorizerRefreshToken;
	
	/** 授权方昵称 */
	@Column(name = "nick_name")
	private String nickName;
	
	/** 授权方头像 */
	@Column(name = "head_img")
	private String headImg;
	
	/** 二维码图片的URL */
	@Column(name = "qrcode_url")
	private String qrcodeUrl;
	
	/** 授权方公众号类型，0代表订阅号，1代表由历史老帐号升级后的订阅号，2代表服务号 */
	@Column(name = "service_type_info")
	private Integer serviceTypeInfo;
	
	/** 授权方认证类型，-1代表未认证，0代表微信认证，1代表新浪微博认证，2代表腾讯微博认证，3代表已资质认证通过但还未通过名称认证，4代表已资质认证通过、还未通过名称认证，但通过了新浪微博认证，5代表已资质认证通过、还未通过名称认证，但通过了腾讯微博认证 */
	@Column(name = "verify_type_info")
	private Integer verifyTypeInfo;
	
	/** 授权方公众号的原始ID */
	@Column(name = "user_name")
	private String userName;
	
	/** 公众号的主体名称 */
	@Column(name = "principal_name")
	private String principalName;
	
	/** 授权方公众号所设置的微信号，可能为空 */
	@Column(name = "alias")
	private String alias;
	
	/** 授权方公众号帐号介绍 */
	private String signature;
	
	/** 文档并没介绍该字段的用途 */
	private Integer idc;
	
	@OneToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "business_info_id", unique=true, nullable=false, updatable=false)
	private BusinessInfo businessInfo;
	
	@OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="authorizerInfoId",insertable = true, updatable = true)
	private Set<FuncInfo> funcInfo;
	
}
