package com.github.supreme94.wechat.core.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.github.supreme94.wechat.core.entities.AuthorizerInfo;

@Repository
@Transactional
public interface AuthorizerInfoRepository extends JpaRepository<AuthorizerInfo, Long>{
	
	void deleteByauthorizerAppid(String authorizerAppid);
}
