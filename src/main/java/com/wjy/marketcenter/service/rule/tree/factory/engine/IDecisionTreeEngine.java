package com.wjy.marketcenter.service.rule.tree.factory.engine;


import com.wjy.marketcenter.service.rule.tree.factory.DefaultTreeFactory;

/**
 * 规则树组合接口
 */
public interface IDecisionTreeEngine {

    DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId);

}
