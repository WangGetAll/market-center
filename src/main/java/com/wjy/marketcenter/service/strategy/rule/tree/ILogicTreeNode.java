package com.wjy.marketcenter.service.strategy.rule.tree;

import com.wjy.marketcenter.service.strategy.rule.tree.factory.DefaultTreeFactory;

/**
 * 规则树结点接口，定义处理方法
 */
public interface ILogicTreeNode {
    /**
     * 根据用户id、策略id、奖品id。对抽奖结果进行处理
     *
     * @param userId
     * @param strategyId
     * @param awardId
     * @return
     */
    DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue);
}

