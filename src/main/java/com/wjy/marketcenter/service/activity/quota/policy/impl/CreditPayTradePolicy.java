package com.wjy.marketcenter.service.activity.quota.policy.impl;

import com.wjy.marketcenter.aggregate.CreateQuotaOrderAggregate;
import com.wjy.marketcenter.repository.activity.IActivityRepository;
import com.wjy.marketcenter.service.activity.quota.policy.ITradePolicy;
import com.wjy.marketcenter.valobj.activity.OrderStateVO;
import org.springframework.stereotype.Service;

/**
 * 积分兑换，支付类订单
 */
@Service("credit_pay_trade")
public class CreditPayTradePolicy implements ITradePolicy {

    private final IActivityRepository activityRepository;

    public CreditPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        createQuotaOrderAggregate.setOrderState(OrderStateVO.wait_pay);
        activityRepository.doSaveCreditPayOrder(createQuotaOrderAggregate);
    }

}
