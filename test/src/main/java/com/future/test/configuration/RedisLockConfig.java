package com.future.test.configuration;

import com.future.common.redis.manager.RedisArgs;
import com.future.mall.lock.LockService;
import com.future.mall.lock.redis.RedisLockServiceImpl;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @Author huzuxing
 * @description todo
 **/
@ConditionalOnProperty(value = "redis.lock.enable", havingValue = "true")
@ConfigurationProperties(prefix = "redis.lock")
@Configuration
@Data
public class RedisLockConfig {
    private List<String> hosts;
    private String password;
    private String type;
    private int poolSize;
    private int retryTimes;
    private long retryDelay;
    private long timeoutMillis;

    @Bean("redisLockSerivce")
    public LockService redisLockService() {
        var redisArgs = new RedisArgs();
        redisArgs.setHosts(hosts);
        redisArgs.setPassword(password);
        redisArgs.setPoolSize(poolSize);
        redisArgs.setRetryDelay(retryDelay);
        redisArgs.setRetryTimes(retryTimes);
        redisArgs.setType(type);
        redisArgs.setTimeoutMillis(timeoutMillis);
        LockService redisLockService = new RedisLockServiceImpl(redisArgs);
        return redisLockService;
    }
}
