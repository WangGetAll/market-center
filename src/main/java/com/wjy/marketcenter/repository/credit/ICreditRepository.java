package com.wjy.marketcenter.repository.credit;

import com.wjy.marketcenter.aggregate.credit.TradeAggregate;

/**
 *  用户积分仓储
 */
public interface ICreditRepository {

    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

}
