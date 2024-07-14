package com.wjy.marketcenter.service.activity;

import com.wjy.marketcenter.entity.activity.ActivityAccountEntity;
import com.wjy.marketcenter.entity.activity.DeliveryOrderEntity;
import com.wjy.marketcenter.entity.activity.SkuRechargeEntity;

/**
 * 抽奖活动订单接口
 */
public interface IRaffleActivityAccountQuotaService {

    /**
     * 创建 sku 账户充值订单，给用户增加抽奖次数
     * <p>
     * 1. 在【打卡、签到、分享、对话、积分兑换】等行为动作下，创建出活动订单，给用户的活动账户【日、月】充值可用的抽奖次数。
     * 2. 对于用户可获得的抽奖次数，比如首次进来就有一次，则是依赖于运营配置的动作，在前端页面上。用户点击后，可以获得一次抽奖次数。
     *
     * @param skuRechargeEntity 活动商品充值实体对象
     * @return 活动ID
     */
    String createOrder(SkuRechargeEntity skuRechargeEntity);

    /**
     * 根据用户id、活动id查询raffle_activity_account表，得到用户在该活动的抽奖次数
     * 总抽奖次数减去剩余抽奖次数得到已经抽奖次数
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 参与次数
     */
    Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId);


    /**
     * 根据用户id、活动id、day查询用户的日参与次数（总次数-剩余次数）
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 参与次数
     */
    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);

    /**
     *  1. 根据用户id、活动id查询raffle_activity_account表，得到用户在该活动的抽奖次数，不存再记录，创建兜底对象（次数都是0）。
     *  2. 根据userId、activityId、month查raffle_activity_account_month表，获得用户在该活动上某月的抽奖次数。
     *  3. 根据用户id、活动id、day查询raffle_activity_account_day表，得到用户的日次数。
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 账户实体
     */
    ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId);

    /**
     * 订单出货 - 积分充值
     * @param deliveryOrderEntity 出货单实体对象
     */
    void updateOrder(DeliveryOrderEntity deliveryOrderEntity);





}
