package com.wjy.marketcenter.repository.activity;


import com.wjy.marketcenter.aggregate.CreatePartakeOrderAggregate;
import com.wjy.marketcenter.aggregate.CreateQuotaOrderAggregate;
import com.wjy.marketcenter.entity.activity.*;
import com.wjy.marketcenter.valobj.activity.ActivitySkuStockKeyVO;

import java.util.Date;

/**
 * 活动仓储接口
 */
public interface IActivityRepository {

    ActivitySkuEntity queryActivitySku(Long sku);

    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);
    void doSaveOrder(CreateQuotaOrderAggregate createOrderAggregate);
    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    ActivitySkuStockKeyVO takeQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

    /**
     * 查询没有使用的抽奖单（创建了抽奖单之后才可以抽奖）
     * @param partakeRaffleActivityEntity
     * @return
     */
    UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    /**
     * 查询用户的在某个活动上的抽奖次数
     * @param userId
     * @param activityId
     * @return
     */
    ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId);

    /**
     * 查询用户在某月某个活动上的抽奖次数
     * @param userId
     * @param activityId
     * @param month
     * @return
     */
    ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month);

    /**
     * 查询用户在某日某个活动上的抽奖次数
     * @param userId
     * @param activityId
     * @param day
     * @return
     */
    ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day);

    /**
     * 用户参与活动，创建订单
     * @param createPartakeOrderAggregate
     */
    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);



}

