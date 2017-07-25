package com.github.supreme94.wechat.core.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_info")
@Getter
@Setter
public class BusinessInfo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "open_pay")
	private Integer openPay;
	
	@Column(name = "open_shake")
	private Integer openShake;
	
	@Column(name = "open_scan")
	private Integer openScan;
	
	@Column(name = "open_card")
	private Integer openCard;
	
	@Column(name = "open_store")
	private Integer openStore;
	
//	@OneToOne(optional=false, mappedBy="businessInfo")
//	private AuthorizerInfo authorizerInfo;

}
