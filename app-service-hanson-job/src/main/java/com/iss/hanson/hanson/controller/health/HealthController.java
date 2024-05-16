package com.iss.hanson.hanson.controller.health;

import com.hanson.rest.SimpleResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: HansonHu
 * @date: 2023-05-12 16:47
 **/
@Api(value = "健康检查")
@RestController
@Slf4j
public class HealthController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ApiOperation(value = "健康检查-基础")
    @GetMapping("/health/n")
    public SimpleResult<String> normal() {
        return SimpleResult.success();
    }

    @ApiOperation(value = "健康检查-redis")
    @GetMapping("/health/r")
    public SimpleResult<String> redisHealth() {
        try {
            checkRedis();
        } catch (Exception e) {
            log.error("check redis health failed.", e);
            throw new HttpMessageNotReadableException(e.getMessage());
        }
        return SimpleResult.success();
    }

    @ApiOperation(value = "健康检查-mysql")
    @GetMapping("/health/m")
    public SimpleResult<String> mysqlHealth() {
        try {
            checkMySQL();
        } catch (Exception e) {
            log.error("check mysql health failed.", e);
            throw new HttpMessageNotReadableException(e.getMessage());
        }
        return SimpleResult.success();
    }

    @ApiOperation(value = "健康检查-mysql&redis")
    @GetMapping("/health/mar")
    public SimpleResult<String> mysqlRedisHealth() {
        try {
            checkRedis();
            checkMySQL();
        } catch (Exception e) {
            log.error("check mysql and redis health failed.", e);
            throw new HttpMessageNotReadableException(e.getMessage());
        }
        return SimpleResult.success();
    }

    private void checkRedis(){
        redisTemplate.opsForValue().get("t");
    }

    private void checkMySQL(){
        jdbcTemplate.execute("SELECT 1");
    }
}
