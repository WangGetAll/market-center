package com.wjy.marketcenter.dao;

import com.wjy.marketcenter.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyRuleMapper {
    List<StrategyRule> queryStrategyRuleList();

    /**
     * 根据strategyId和ruleModel查strategy_rule表，获得此策略配置的此规则的具体信息。
     * @param strategyRuleReq
     * @return
     */
    StrategyRule queryStrategyRule(StrategyRule strategyRuleReq);

    /**
     * 根据策略id、奖品id（如果有）、规则名称查询strategy_rule表，得到规则具体配置
     * @param strategyRule
     * @return
     */
    String queryStrategyRuleValue(StrategyRule strategyRule);
}
