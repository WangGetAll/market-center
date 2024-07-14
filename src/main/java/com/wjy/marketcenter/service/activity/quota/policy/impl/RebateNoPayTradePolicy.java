package com.wjy.marketcenter.service.activity.quota.policy.impl;

import com.wjy.marketcenter.aggregate.CreateQuotaOrderAggregate;
import com.wjy.marketcenter.repository.activity.IActivityRepository;
import com.wjy.marketcenter.service.activity.quota.policy.ITradePolicy;
import com.wjy.marketcenter.valobj.activity.OrderStateVO;
import com.wjy.marketcenter.valobj.activity.OrderTradeTypeVO;
import com.wjy.marketcenter.valobj.credit.TradeTypeVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 返利无支付交易订单，直接充值到账
 */
@Service("rebate_no_pay_trade")
public class RebateNoPayTradePolicy implements ITradePolicy {

    private final IActivityRepository activityRepository;

    public RebateNoPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        // 不需要支付则修改订单金额为0，状态为完成，直接给用户账户充值
        createQuotaOrderAggregate.setOrderState(OrderStateVO.completed);
        createQuotaOrderAggregate.getActivityOrderEntity().setPayAmount(BigDecimal.ZERO);
        activityRepository.doSaveNoPayOrder(createQuotaOrderAggregate);
    }

}
