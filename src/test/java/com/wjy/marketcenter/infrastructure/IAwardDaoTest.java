package com.wjy.marketcenter.infrastructure;

import com.alibaba.fastjson.JSON;
import com.wjy.marketcenter.po.Award;
import com.wjy.marketcenter.dao.IAwardDao;
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
public class IAwardDaoTest {
    @Resource
    private IAwardDao IAwardDao;

    @Test
    public void test_queryAwardList() {
        List<Award> awards = IAwardDao.queryAwardList();
        Award award = new Award();
        log.info("测试结果：{}", JSON.toJSONString(awards));
    }
}
