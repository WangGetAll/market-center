package com.wjy.marketcenter.infrastructure;

import com.alibaba.fastjson.JSON;
import com.wjy.marketcenter.entity.Award;
import com.wjy.marketcenter.mapper.AwardMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AwardMapperTest {
    @Resource
    private AwardMapper awardMapper;

    @Test
    public void test_queryAwardList() {
        List<Award> awards = awardMapper.queryAwardList();
        Award award = new Award();
        log.info("测试结果：{}", JSON.toJSONString(awards));
    }
}
