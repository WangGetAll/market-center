package com.wjy.marketcenter.service.strategy.rule.chain;

import com.wjy.marketcenter.service.strategy.rule.chain.factory.DefaultChainFactory;

/**
 * 抽奖策略规则责任链接口
 */
public interface ILogicChain extends ILogicChainArmory {

    /**
     * 1. 参数校验
     * 2. 抽奖前置工作流
     *  2.1. 根据strategyId拿到责任链
     *      2.1.1 根据strategyId查strategy表，获得该策略配置的规则
     *      2.1.2 根据配置的规则，装配责任链返回
     *  2.2. 执行责任链
     *      2.2.1 黑名单处理器
     *        2.2.1.1 根据策略id、规则名称为黑名单查询strategy_rule表，获得具体规则配置
     *        2.2.1.2 解析配置，判断用户是否在黑名单中，如果在则返回兜底奖励，不在放行
     *      2.2.2 积分处理器
     *        2.2.2.1. 根据策略id、规则名称为黑名单查询strategy_rule表，获得具体规则配置
     *        2.2.2.2. 解析配置，如果用户的积分大于等于某个积分值，执行积分值内规定的奖品抽奖，否则放行
     *      2.2.3 默认处理器，执行默认抽奖流程
     * 3. 抽奖后置工作流
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 奖品对象
     */
    DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId);

}
