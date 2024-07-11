package com.wjy.marketcenter.mapper;

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

    String queryStrategyRuleValue(StrategyRule strategyRule);
}
