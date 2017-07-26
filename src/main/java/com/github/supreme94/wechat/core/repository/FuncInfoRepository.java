package com.github.supreme94.wechat.core.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.supreme94.wechat.core.entities.FuncInfo;

@Repository
public interface FuncInfoRepository extends JpaRepository<FuncInfo, Long>{
	
}
