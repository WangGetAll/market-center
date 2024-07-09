package com.wjy.marketcenter.service.strategy.rule.chain;

import com.wjy.marketcenter.service.strategy.rule.chain.factory.DefaultChainFactory;

/**
 * 抽奖策略规则责任链接口
 */
public interface ILogicChain extends ILogicChainArmory {

    /**
     * 责任链接口
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 奖品对象
     */
    DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId);

}