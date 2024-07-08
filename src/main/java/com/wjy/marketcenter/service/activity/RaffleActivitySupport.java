package com.wjy.marketcenter.service.activity;

import com.wjy.marketcenter.entity.activity.ActivityCountEntity;
import com.wjy.marketcenter.entity.activity.ActivityEntity;
import com.wjy.marketcenter.entity.activity.ActivitySkuEntity;
import com.wjy.marketcenter.repository.activity.IActivityRepository;
import com.wjy.marketcenter.service.activity.rule.factory.DefaultActivityChainFactory;

/**
 *  抽奖活动的支撑类
 */
public class RaffleActivitySupport {

    protected DefaultActivityChainFactory defaultActivityChainFactory;

    protected IActivityRepository activityRepository;

    public RaffleActivitySupport(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        this.activityRepository = activityRepository;
        this.defaultActivityChainFactory = defaultActivityChainFactory;
    }

    public ActivitySkuEntity queryActivitySku(Long sku) {
        return activityRepository.queryActivitySku(sku);
    }

    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        return activityRepository.queryRaffleActivityByActivityId(activityId);
    }

    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
    }

}
