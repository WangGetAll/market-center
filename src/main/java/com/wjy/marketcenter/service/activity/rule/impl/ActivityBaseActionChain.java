package com.wjy.marketcenter.service.activity.rule.impl;

import com.wjy.marketcenter.entity.activity.ActivityCountEntity;
import com.wjy.marketcenter.entity.activity.ActivityEntity;
import com.wjy.marketcenter.entity.activity.ActivitySkuEntity;
import com.wjy.marketcenter.service.activity.rule.AbstractActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *  活动规则过滤【日期、状态】
 */
@Slf4j
@Component("activity_base_action")
public class ActivityBaseActionChain extends AbstractActionChain {

    @Override
    public boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {

        log.info("活动责任链-基础信息【有效期、状态】校验开始。");

        return next().action(activitySkuEntity, activityEntity, activityCountEntity);
    }

}
