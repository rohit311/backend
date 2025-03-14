package com.example.cacheproxy;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

@Command(name = "--clean-cache")
@Slf4j
public class CleanCacheCommand implements Callable<Integer> {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Override
    public Integer call() throws Exception {
        this.redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        return 1;
    }

}