package com.wjy.marketcenter.repository.activity;


import com.wjy.marketcenter.aggregate.CreateOrderAggregate;
import com.wjy.marketcenter.entity.activity.ActivityCountEntity;
import com.wjy.marketcenter.entity.activity.ActivityEntity;
import com.wjy.marketcenter.entity.activity.ActivitySkuEntity;

/**
 * 活动仓储接口
 */
public interface IActivityRepository {

    ActivitySkuEntity queryActivitySku(Long sku);

    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);
    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

}

