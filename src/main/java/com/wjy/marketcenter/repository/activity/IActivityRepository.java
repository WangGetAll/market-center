package com.wjy.marketcenter.repository.activity;


import com.wjy.marketcenter.aggregate.CreatePartakeOrderAggregate;
import com.wjy.marketcenter.aggregate.CreateQuotaOrderAggregate;
import com.wjy.marketcenter.entity.activity.*;
import com.wjy.marketcenter.valobj.activity.ActivitySkuStockKeyVO;

import java.util.Date;
import java.util.List;

/**
 * 活动仓储接口
 */
public interface IActivityRepository {

    ActivitySkuEntity queryActivitySku(Long sku);

    /**
     * 根据activityId查询活动的信息。优先走缓存，未命中，写缓存。key为activityId，value为ActivityEntity
     * @param activityId
     * @return
     */
    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    /**
     * 根据activityCountId，查询次数增长策略。优先走缓存，未命中，写缓存。key为activityCountId，value为ActivityCountEntity对象
     * @param activityCountId
     * @return
     */
    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);
    void doSaveNoPayOrder(CreateQuotaOrderAggregate createOrderAggregate);

    void doSaveCreditPayOrder(CreateQuotaOrderAggregate createQuotaOrderAggregate);

    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    ActivitySkuStockKeyVO takeQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

    /**
     * 根据userid和activityid查询用户处于create状态的抽奖单，没有返回null
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
     * 1. 根据userId、activityId、总剩余抽奖次数大于0更新raffle_activity_account表，将总剩余抽奖次数都减一。
     * 2. 更新或新建月次数记录
     *  2.1. 存在月次数记录。
     *      2.1.1. 根据userId、activityId、month、month_count_surplus>0更新raffle_activity_account_month表，将表中的month_count_surplus减一
     *      2.1.2  根据根据userId、activityId、month_count_surplus>0更新raffle_activity_account表，将表中的month_count_surplus减一
     *  2.2. 不存在月次数记录。
     *      2.2.1. 新建月次数记录，月总次数为总次数表中的月总次数，月剩余次为总次数表中的月剩余次数减一。
     *      2.2.2. 根据userId、activityId更新raffle_activity_account表，将月剩余次数更新为之前查出来的月剩余次数减一。
     * 3. 更新或新建日次数记录（同月次数处理逻辑）
     * 4. 新建抽奖单记录
     * @param createPartakeOrderAggregate
     */
    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);

    /**
     * 根据activityId查询，此活动的所有sku。（给这个活动配置了什么增长次数规则）
     * @param activityId
     * @return
     */
    List<ActivitySkuEntity> queryActivitySkuListByActivityId(Long activityId);

    /**
     * 根据用户id、活动id、day查询用户的日参与次数（总次数-剩余次数）
     * @param activityId
     * @param userId
     * @return
     */
    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);

    /**
     *  1. 根据用户id、活动id查询raffle_activity_account表，得到用户在该活动的抽奖次数，不存再记录，创建兜底对象（次数都是0）。
     *  2. 根据userId、activityId、month查raffle_activity_account_month表，获得用户在该活动上某月的抽奖次数。
     *  3. 根据用户id、活动id、day查询raffle_activity_account_day表，得到用户的日次数。
     * @param activityId
     * @param userId
     * @return
     */
    ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId);

    /**
     * 根据用户id、活动id查询raffle_activity_account表，得到用户在该活动的抽奖次数
     * 总抽奖次数减去剩余抽奖次数得到已经抽奖次数
     * @param activityId
     * @param userId
     * @return
     */
    Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId);

    void updateOrder(DeliveryOrderEntity deliveryOrderEntity);

}

