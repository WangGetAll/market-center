package com.wjy.marketcenter.service.rule;


import com.wjy.marketcenter.entity.RuleActionEntity;
import com.wjy.marketcenter.entity.RuleMatterEntity;

/**
 * 抽奖规则过滤接口
 * @param <T>
 */
public interface ILogicFilter<T extends RuleActionEntity.RaffleEntity> {

    /**
     * 根据策略id、奖品id、规则类型去做过滤
     * @param ruleMatterEntity
     * @return
     */
    RuleActionEntity<T> filter(RuleMatterEntity ruleMatterEntity);
}

