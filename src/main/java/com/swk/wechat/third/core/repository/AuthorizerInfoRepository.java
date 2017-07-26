package com.swk.wechat.third.core.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.swk.wechat.third.core.entities.AuthorizerInfo;

@Repository
public interface AuthorizerInfoRepository extends JpaRepository<AuthorizerInfo, Long>{
	
	@Transactional
	int deleteByauthorizerAppid(String authorizerAppid);
	
	AuthorizerInfo findOneByauthorizerAppid(String authorizerAppid);
}
