package com.future.common.redis.manager;

import lombok.Data;

import java.util.List;

/**
 * @Author huzuxing
 * @description todo
 **/
@Data
public class RedisArgs {
    private List<String> hosts;
    private String password;
    private String type;
    private int poolSize;
    private int retryTimes;
    private long retryDelay;
    private long timeoutMillis;
}
