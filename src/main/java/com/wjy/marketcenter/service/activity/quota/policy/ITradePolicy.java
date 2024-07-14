package com.wjy.marketcenter.service.activity.quota.policy;

import com.wjy.marketcenter.aggregate.CreateQuotaOrderAggregate;

/**
 *  交易策略接口，包括；返利兑换（不用支付），积分订单（需要支付）
 */
public interface ITradePolicy {

    void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate);

}
