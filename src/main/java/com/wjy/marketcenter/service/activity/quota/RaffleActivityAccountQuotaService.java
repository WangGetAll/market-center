package com.wjy.marketcenter.service.activity.quota;

import com.wjy.marketcenter.aggregate.CreateQuotaOrderAggregate;
import com.wjy.marketcenter.entity.activity.*;
import com.wjy.marketcenter.repository.activity.IActivityRepository;
import com.wjy.marketcenter.service.activity.IRaffleActivitySkuStockService;
import com.wjy.marketcenter.service.activity.quota.policy.ITradePolicy;
import com.wjy.marketcenter.service.activity.quota.rule.factory.DefaultActivityChainFactory;
import com.wjy.marketcenter.valobj.activity.ActivitySkuStockKeyVO;
import com.wjy.marketcenter.valobj.activity.OrderStateVO;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * 抽奖活动服务
 */
@Service
public class RaffleActivityAccountQuotaService extends AbstractRaffleActivityAccountQuota implements IRaffleActivitySkuStockService {

    public RaffleActivityAccountQuotaService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory, Map<String, ITradePolicy> tradePolicyGroup) {
        super(activityRepository, defaultActivityChainFactory, tradePolicyGroup);
    }


    @Override
    protected CreateQuotaOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        // 订单实体对象
        ActivityOrderEntity activityOrderEntity = new ActivityOrderEntity();
        activityOrderEntity.setUserId(skuRechargeEntity.getUserId());
        activityOrderEntity.setSku(skuRechargeEntity.getSku());
        activityOrderEntity.setActivityId(activityEntity.getActivityId());
        activityOrderEntity.setActivityName(activityEntity.getActivityName());
        activityOrderEntity.setStrategyId(activityEntity.getStrategyId());
        // 公司里一般会有专门的雪花算法UUID服务，我们这里直接生成个12位就可以了。
        activityOrderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
        activityOrderEntity.setOrderTime(new Date());
        activityOrderEntity.setTotalCount(activityCountEntity.getTotalCount());
        activityOrderEntity.setDayCount(activityCountEntity.getDayCount());
        activityOrderEntity.setMonthCount(activityCountEntity.getMonthCount());
        activityOrderEntity.setPayAmount(activitySkuEntity.getProductAmount());
        activityOrderEntity.setOutBusinessNo(skuRechargeEntity.getOutBusinessNo());

        // 构建聚合对象
        return CreateQuotaOrderAggregate.builder()
                .userId(skuRechargeEntity.getUserId())
                .activityId(activitySkuEntity.getActivityId())
                .totalCount(activityCountEntity.getTotalCount())
                .dayCount(activityCountEntity.getDayCount())
                .monthCount(activityCountEntity.getMonthCount())
                .activityOrderEntity(activityOrderEntity)
                .build();
    }


    @Override
    public void updateOrder(DeliveryOrderEntity deliveryOrderEntity) {
        activityRepository.updateOrder(deliveryOrderEntity);
    }


    @Override
    public ActivitySkuStockKeyVO takeQueueValue() throws InterruptedException {
        return activityRepository.takeQueueValue();
    }

    @Override
    public void clearQueueValue() {
        activityRepository.clearQueueValue();
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        activityRepository.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        activityRepository.clearActivitySkuStock(sku);
    }

    /**
     * 根据用户id、活动id查询raffle_activity_account表，得到用户在该活动的抽奖次数
     * 总抽奖次数减去剩余抽奖次数得到已经抽奖次数
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return
     */
    @Override
    public Integer queryRaffleActivityAccountPartakeCount(Long activityId, String userId) {
        return activityRepository.queryRaffleActivityAccountPartakeCount(activityId, userId);
    }


    /**
     * 根据用户id、活动id、day查询用户的日参与次数（总次数-剩余次数）
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return
     */
    @Override
    public Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId) {
        return activityRepository.queryRaffleActivityAccountDayPartakeCount(activityId, userId);
    }

    /**
     * 1. 根据用户id、活动id查询raffle_activity_account表，得到用户在该活动的抽奖次数，不存再记录，创建兜底对象（次数都是0）。
     * 2. 根据userId、activityId、month查raffle_activity_account_month表，获得用户在该活动上某月的抽奖次数，不存再记录，创建兜底对象（次数都是0）。
     * 3. 根据用户id、活动id、day查询raffle_activity_account_day表，得到用户的日次数，不存再记录，创建兜底对象（次数都是0）。
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return
     */
    @Override
    public ActivityAccountEntity queryActivityAccountEntity(Long activityId, String userId) {
        return activityRepository.queryActivityAccountEntity(activityId, userId);
    }


}

