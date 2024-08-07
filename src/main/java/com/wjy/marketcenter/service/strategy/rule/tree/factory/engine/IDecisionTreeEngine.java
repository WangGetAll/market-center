package com.wjy.marketcenter.service.strategy.rule.tree.factory.engine;


import com.wjy.marketcenter.service.strategy.rule.tree.factory.DefaultTreeFactory;

import java.util.Date;

/**
 * 规则树组合接口
 */
public interface IDecisionTreeEngine {

    DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId, Date endDateTime);

}
