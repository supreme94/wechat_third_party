package com.github.supreme94.wechat.core.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.github.supreme94.wechat.core.entities.AuthorizerInfo;

@Repository
public interface AuthorizerInfoRepository extends JpaRepository<AuthorizerInfo, Long>{
	
	@Modifying
	@Query("delete from AuthorizerInfo ai where ai.authorizerAppid=?1 ")
	@Transactional
	int deleteByauthorizerAppid(String authorizerAppid);
}
