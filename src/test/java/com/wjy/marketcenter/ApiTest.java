package com.wjy.marketcenter;

import com.wjy.marketcenter.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {
    @Autowired
    private RedisService redisService;

    @Test
    public void test() {
        RMap<Object, Object> map = redisService.getMap("strategy_id_100001");
        map.put(1,101);
        map.put(2,102);
        map.put(3,103);
        map.put(4,104);
        map.put(5,105);
        map.put(6,106);
        log.info("{}",redisService.getFromMap("strategy_id_100001", 1).toString());

    }


}
