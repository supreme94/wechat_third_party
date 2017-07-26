package com.swk.wechat.third.core.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swk.wechat.third.core.entities.FuncInfo;

@Repository
public interface FuncInfoRepository extends JpaRepository<FuncInfo, Long>{
	
}
