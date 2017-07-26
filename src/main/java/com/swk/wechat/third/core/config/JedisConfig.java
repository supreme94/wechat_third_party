package com.swk.wechat.third.core.config;

import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Configuration
public class JedisConfig {
	
	@Value("${spring.redis.database}")
    private int database;
	
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.pool.max-idle}")
    private int maxIdle;
    
    @Value("${spring.redis.pool.min-idle}")
    private int minIdle;
    
    @Value("${spring.redis.pool.max-wait}")
    private long maxWaitMillis;
    
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
    	JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(false);
        
        //以下是jedispool的默认配置
        //Idle时进行连接扫描
        //jedisPoolConfig.setTestWhileIdle(true);
        //表示idle object evitor两次扫描之间要sleep的毫秒数
        //jedisPoolConfig.setTimeBetweenEvictionRunsMillis(30000);
        //表示idle object evitor每次扫描的最多的对象数
        //jedisPoolConfig.setNumTestsPerEvictionRun(-1);
        //表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
        //jedisPoolConfig.setMinEvictableIdleTimeMillis(60000);
        return jedisPoolConfig;
    }
    
    @Bean
    public JedisPool redisPoolFactory() {
    	JedisPool jedisPool = new JedisPool(jedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, null, database);
        return jedisPool;
    }
    
    @Bean
	public RedisConnectionFactory redisConnectionFactory() {
		JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
		redisConnectionFactory.setHostName(host);
		redisConnectionFactory.setPort(port);
		redisConnectionFactory.setUsePool(true);
		redisConnectionFactory.setDatabase(database);
		redisConnectionFactory.setPoolConfig(jedisPoolConfig());
		return redisConnectionFactory;
	}
    
	@Bean("customRedisTemplate")
	public RedisTemplate<String, String> customRedisTemplate() {
		RedisTemplate<String, String> redis = new RedisTemplate<>();
		redis.setConnectionFactory(redisConnectionFactory());
		ObjectMapper om = new ObjectMapper();
		om.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);  
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	    redis.setValueSerializer(new GenericJackson2JsonRedisSerializer(om));  
	    redis.setKeySerializer(new StringRedisSerializer());
	    redis.setHashKeySerializer(new StringRedisSerializer());
	    redis.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(om));
	    redis.afterPropertiesSet();
		return redis;
	}
}
